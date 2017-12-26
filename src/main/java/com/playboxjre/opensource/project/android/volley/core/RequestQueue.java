package com.playboxjre.opensource.project.android.volley.core;

import com.playboxjre.opensource.project.android.volley.VolleyLog;
import com.playboxjre.opensource.project.android.volley.interfaces.ExecutorDelivery;
import com.playboxjre.opensource.project.android.volley.interfaces.ResponseDelivery;
import com.playboxjre.opensource.project.android.volley.interfaces.network.Network;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestQueue {


    /** Callback interface for completed requests. */
    public interface RequestFinishedListener<T> {
        /** Called when a request has finished processing. */
        void onRequestFinished(Request<T> request);
    }
    /**用于生成请求的单调递增序列号。*/
    private final AtomicInteger mSequenceGenerator = new AtomicInteger();

    private final Set<Request<?>> currentRequests = new HashSet<>();

    private final PriorityBlockingQueue<Request<?>> cacheQueue = new PriorityBlockingQueue<>();

    private final PriorityBlockingQueue<Request<?>> networkQueue = new PriorityBlockingQueue<>();

    private static final int DEFAULT_NETWORK_THREAD_TOOL_SIZE = 4;

    private final Cache cache;

    private final Network network;

    private final ResponseDelivery responseDelivery;

    private final NetworkDispatcher[] networkDispatcher;

    private CacheDispatcher cacheDispatcher;

    private List<RequestFinishedListener> finishedListenerList = new ArrayList<>();

    public RequestQueue(Cache cache,Network network,int threadPoolSize, ResponseDelivery responseDelivery){
        this.cache = cache;
        this.network = network;
        this.networkDispatcher = new NetworkDispatcher[threadPoolSize];
        this.responseDelivery = responseDelivery;
    }

    /**
     * Creates the worker pool. Processing will not begin until {@link #start()} is called.
     *
     * @param cache A Cache to use for persisting responses to disk
     * @param network A Network interface for performing HTTP requests
     * @param threadPoolSize Number of network dispatcher threads to create
     */
    public RequestQueue(Cache cache, Network network, int threadPoolSize) {
        this(cache, network, threadPoolSize,
                new ExecutorDelivery());//
    }

    public RequestQueue(Cache cache, Network network){
        this(cache,network,DEFAULT_NETWORK_THREAD_TOOL_SIZE);
    }


    /**
     * Starts the dispatchers in this queue.
     */

    public void start(){
        stop();
        cacheDispatcher = new CacheDispatcher(cacheQueue,networkQueue,cache,responseDelivery);
        cacheDispatcher.start();

        for(int i = 0 ;i < networkDispatcher.length;i++){
            NetworkDispatcher networkDispatcher = new NetworkDispatcher(networkQueue,network,cache,responseDelivery);
            this.networkDispatcher[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    public void stop(){
        if(cacheDispatcher != null){
            cacheDispatcher.quit();
        }
        for (final NetworkDispatcher networkDispatcher : networkDispatcher){
            if(networkDispatcher!=null){
                networkDispatcher.quit();
            }
        }
    }

    public int getSequennceNumber(){
        return mSequenceGenerator.incrementAndGet();
    }

    public Cache getCache(){
        return cache;
    }

    /**
     * A simple predicate or filter interface for Requests, for use by
     * {@link RequestQueue#cancelAll(RequestFilter)}.
     */
    public interface RequestFilter {
        boolean apply(Request<?> request);
    }

    public void cancelAll(RequestFilter filter){
        Objects.requireNonNull(filter);
        synchronized (currentRequests){
            for (Request<?> request: currentRequests) {
                if(filter.apply(request)){
                    request.cancel();
                }
            }
        }
    }


    public void cancelAll(Object tag){
        Objects.requireNonNull(tag,"Cannot cancelAll with a null tag");
        cancelAll(new RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return request.getTag() == null;
            }
        });
    }


    public <T> Request<T> add(Request<T> request){
        //将请求标记为属于此队列，并将其添加到当前请求集中。
        request.setRequestQueue(this);
        synchronized (currentRequests){
            currentRequests.add(request);
        }

        //Process requests in the order they are added.
        //按添加的顺序处理请求。
        request.setSequence(getSequennceNumber());
        request.addMarker("add-to-queue");

        //cache
        if(!request.shouldCache()){
            networkQueue.add(request);
            return request;
        }
        cacheQueue.add(request);
        return request;
    }



    public <T> void finish(Request<T> request) {
        synchronized (currentRequests){
            currentRequests.remove(request);
        }
        synchronized (finishedListenerList){
            for (RequestFinishedListener<T> listener : finishedListenerList) {
                listener.onRequestFinished(request);
            }
        }
    }

    public  <T> void addRequestFinishedListener(RequestFinishedListener<T> listener) {
        synchronized (finishedListenerList) {
            finishedListenerList.add(listener);
        }
    }

    /**
     * Remove a RequestFinishedListener. Has no effect if listener was not previously added.
     */
    public  <T> void removeRequestFinishedListener(RequestFinishedListener<T> listener) {
        synchronized (finishedListenerList) {
            finishedListenerList.remove(listener);
        }
    }
}
