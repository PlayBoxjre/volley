package com.playboxjre.opensource.project.android.volley.core;

import com.playboxjre.opensource.project.android.volley.VolleyLog;
import com.playboxjre.opensource.project.android.volley.interfaces.ResponseDelivery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * 提供用于对请求队列执行缓存分类的线程
 * Requests added to the specified cache queue are resolved from cache.
 * Any deliverable response is posted back to the caller via a
 * {@link ResponseDelivery}.  Cache misses and responses that require
 * refresh are enqueued on the specified network queue for processing
 * by a {@link NetworkDispatcher}
 */
public class CacheDispatcher extends Thread {
    private static final boolean DEBUG = VolleyLog.DEBUG;
    /** The queue of requests coming in for triage.
     *  用于分类的请求队列。
     * */
    private final BlockingQueue<Request<?>> cacheQueue;

    /** The queue of requests going out to the network.
     * 发送到网络的请求队列
     * */
    private final BlockingQueue<Request<?>> networkQueue;

    /** The cache to read from 要读取的缓存 */
    private final Cache cache;

    /**用于发布响应。For posting response */
    private final ResponseDelivery delivery;

    /**用来告诉我们去死。 Used for telling us to die*/
    private volatile boolean mQuit = false;

    /** Manage list of waiting requests and de-duplicate requests with same cache key.
     * 管理具有相同缓存键的等待请求和反重复请求的列表。
     * */
    private final WaitingRequestManager mWaitingRequestManager;

    /**
     * Creates a new cache triage dispatcher thread.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param cacheQueue Queue of incoming requests for triage
     * @param networkQueue Queue to post requests that require network to
     * @param cache Cache interface to use for resolution
     * @param delivery Delivery interface to use for posting responses
     */
    public CacheDispatcher(
            BlockingQueue<Request<?>> cacheQueue, BlockingQueue<Request<?>> networkQueue,
            Cache cache, ResponseDelivery delivery) {
        this.cacheQueue = cacheQueue;
        this.networkQueue = networkQueue;
        this.cache = cache;
        this.delivery = delivery;
        mWaitingRequestManager = new WaitingRequestManager(this);
    }

    public void quit(){
        this.mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        if (DEBUG) VolleyLog.v("start new dispatcher");
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        cache.initialize();

        while(true){
            try{
                processRequest();
            }catch (InterruptedException e){
                if(mQuit){
                    return;
                }
            }
        }
    }
    // Extracted to its own method to ensure locals have a constrained liveness scope by the GC.
    // This is needed to avoid keeping previous request references alive for an indeterminate amount
    // of time. Update consumer-proguard-rules.pro when modifying this. See also
    // https://github.com/google/volley/issues/114
    private void processRequest() throws InterruptedException {
        // Get a request from the cache triage queue, blocking until
        // at least one is available.
        final Request<?> request = cacheQueue.take();
        request.addMarker("cache-queue-take");

        // If the request has been canceled, don't bother dispatching it.
        if(request.isCanceled()){
            request.finish("cache-discard-canceled");
            return;
        }

        Cache.Entry entry = cache.get(request.getCacheKey());
        if(entry == null){
            request.addMarker("cache-miss");
            // Cache miss; send off to the network dispatcher.
            if(!mWaitingRequestManager.maybeAddToWaitingRequests(request)){
                networkQueue.put(request);
            }
            return;
        }
        // If it is completely expired, just send it to the network.
        if(entry.isExpired()){
            request.addMarker("cache-hit-expired");
            request.setCacheEntry(entry);
            if(!mWaitingRequestManager.maybeAddToWaitingRequests(request)){
                networkQueue.put(request);
            }
            return;
        }
        // We have a cache hit; parse its data for delivery back to the request.
        request.addMarker("cache-hit");
        //实际去网络去请求数据了，返回数据 这是主方法
        Response<?> response = request.parseNetworkResponse(new NetworkResponse(entry.data, entry.responseHeaders));

        request.addMarker("cache-hit-parsed");

        if(!entry.refreshNeeded()){
            //Completely unexpired cache hit. Just deliver the response.
            delivery.postResponse(request,response);
        }else {
            // Soft-expired cache hit. We can deliver the cached response,
            // but we need to also send the request to the network for
            // refreshing.
            request.addMarker("cache-hit-refresh-needed");
            request.setCacheEntry(entry);
            //mark response as immediate
            response.intermediate = true;
            if (!mWaitingRequestManager.maybeAddToWaitingRequests(request)) {
                // Post the intermediate response back to the user and have
                // the delivery then forward the request along to the network.
                delivery.postResponse(request, response, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //再次放入网络请求队列，等待请求
                            networkQueue.put(request);
                        } catch (InterruptedException e) {
                            // Restore the interrupted status
                            Thread.currentThread().interrupt();
                        }
                    }
                });
            }else {
                // request has been added to list of waiting requests
                //已将请求添加到等待请求的列表中，以便在网络响应返回后从第一个请求接收网络响应
                // to receive the network response from the first request once it returns.
                delivery.postResponse(request,response);
            }
        }
    }



    private static class WaitingRequestManager implements NetworkRequestCompleteListener {

        private final Map<String, List<Request<?>>> mWaitingRequests = new HashMap<>();
        private final CacheDispatcher mCacheDispatcher;

        WaitingRequestManager(CacheDispatcher cacheDispatcher) {
            mCacheDispatcher = cacheDispatcher;
        }
        /** Request received a valid response that can be used by other waiting requests. */
        @Override
        public void onResponseReceived(Request<?> request, Response<?> response) {
            if(response.cacheEntry == null|| response.cacheEntry.isExpired()){
                onNoUsableResponseReceived(request);
                return;
            }

            String cacheKey = request.getCacheKey();
            List<Request<?>> waitingRequests;
            synchronized (this){
                waitingRequests = mWaitingRequests.remove(cacheKey);
            }
            if(waitingRequests != null){
                if(VolleyLog.DEBUG){
                    VolleyLog.v("Releasing %d waiting requests for cacheKey=%s.",
                            waitingRequests.size(), cacheKey);
                }
                // Process all queued up requests.处理所有排队请求
                for(Request<?> waiting:waitingRequests){
                    mCacheDispatcher.delivery.postResponse(waiting,response);
                }
            }


        }

        @Override
        public synchronized void onNoUsableResponseReceived(Request<?> request) {
            String cacheKey = request.getCacheKey();
            List<Request<?>> watingRequests = mWaitingRequests.remove(cacheKey);
            if(watingRequests!=null && !watingRequests.isEmpty()){
                if(VolleyLog.DEBUG){
                    VolleyLog.v("%d waiting requests for cacheKey=%s; resend to network",
                            watingRequests.size(), cacheKey);
                }
                Request<?> nextInLine = watingRequests.remove(0);
                mWaitingRequests.put(cacheKey,watingRequests);
                nextInLine.setNetworkRequestCompleteListener(this);
                try {
                    mCacheDispatcher.networkQueue.put(nextInLine);
                } catch (InterruptedException e) {
                    VolleyLog.e("Couldn't add request to queue. %s", e.toString());
                    // Restore the interrupted status of the calling thread (i.e. NetworkDispatcher)
                    Thread.currentThread().interrupt();
                    // Quit the current CacheDispatcher thread.
                    mCacheDispatcher.quit();
                }
            }
        }

        /**
         * 请求是否排队。
         * 如果为false，我们应该继续在网络上发出请求*。
         * 如果为真，则应在飞行请求完成时将请求挂起处理。
         * @param request
         * @return
         */
        public synchronized boolean maybeAddToWaitingRequests(Request<?> request){
            String cacheKey = request.getCacheKey();

            if(mWaitingRequests.containsKey(cacheKey)){
                List<Request<?>> stagedRequests = mWaitingRequests.get(cacheKey);
                if(stagedRequests == null)
                    stagedRequests = new ArrayList<>();
                request.addMarker("waiting-for-response");
                stagedRequests.add(request);

                mWaitingRequests.put(cacheKey,stagedRequests);
                if (VolleyLog.DEBUG) {
                    VolleyLog.d("Request for cacheKey=%s is in flight, putting on hold.", cacheKey);
                }
                return true;
            }else {
                mWaitingRequests.put(cacheKey,null);
                // 设置网络请求完成监听事件
                request.setNetworkRequestCompleteListener(this);
                if (VolleyLog.DEBUG) {
                    VolleyLog.d("new request, sending to network %s", cacheKey);
                }
                return false;
            }
        }
    }


}