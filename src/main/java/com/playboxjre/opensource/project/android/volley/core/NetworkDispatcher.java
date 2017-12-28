package com.playboxjre.opensource.project.android.volley.core;

import com.playboxjre.opensource.project.android.volley.VolleyLog;
import com.playboxjre.opensource.project.android.volley.exception.VolleyError;
import com.playboxjre.opensource.project.android.volley.interfaces.ResponseDelivery;
import com.playboxjre.opensource.project.android.volley.interfaces.cache.Cache;
import com.playboxjre.opensource.project.android.volley.interfaces.network.Network;

import java.util.concurrent.BlockingQueue;

/**
 * 从网络请求队列中获取请求对象，执行网络请求，解析，和分发返回结果等完成流程。*
  *Requests added to the specified queue are processed from the network via a
 * specified {@link Network} interface. Responses are committed to cache, if
 * eligible, using a specified {@link Cache} interface. Valid responses and
 * errors are posted back to the caller via a {@link ResponseDelivery}.
 */
public class NetworkDispatcher extends Thread{
    /** The queue of requests to service.
     * 服务请求的队列。*/
    private final BlockingQueue<Request<?>> queue;
    /** The network interface for processing requests.用于处理请求的网络接口 */
    private final Network network;
    /** The cache to write to. 要写入的缓存*/
    private final Cache cache;
    /** For posting responses and errors. */
    private final ResponseDelivery responseDelivery;

    private volatile boolean quit = false;

    public NetworkDispatcher(BlockingQueue<Request<?>> queue, Network network, Cache cache, ResponseDelivery responseDelivery) {
        this.queue = queue;
        this.network = network;
        this.cache = cache;
        this.responseDelivery = responseDelivery;
    }

    public void quit(){
        quit =true;
        interrupt();
    }



    @SuppressWarnings({"SingleStatementInBlock"})
    @Override
    public void run() {
        VolleyLog.d("start network dispatcher thread : " + Thread.currentThread().getId());
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        while (true){
            try {
                processRequest();
            }catch (InterruptedException e){
                if(quit){
                    return;
                }
            }
        }
    }

    private void processRequest() throws InterruptedException{
        long startTime = System.currentTimeMillis();
        Request<?> request = queue.take();
        try {
            request.addMarker("network-queue-take");
            // If the request was cancelled already, do not perform the
            // network request.
            if(request.isCanceled()){
                request.finish("network-discard-cancelled");
                request.notifyListenerResponseNotUsable();
                return;
            }

            request.addMarker("trafficstat = "+ request.getTrafficStatsTag());

            //perform network request
            NetworkResponse networkResponse = network.performRequest(request);
            request.addMarker("network-http-completed");

            //如果服务器返回304，我们已经发送了一个响应
            //我们结束了--别再给出一个相同的回应了。
            if(networkResponse.notModified && request.hasHadResponseDelivered()) {
                request.finish("not-modified");
                request.notifyListenerResponseNotUsable();
                return;
            }

            // Parse the response here on the worker thread.
            Response<?> response = request.parseNetworkResponse(networkResponse);
            request.addMarker("network-parse-complete");

            //write to cache if applicable
            // TODO: Only update cache metadata instead of entire record for 304
            // TODO: 只更新缓存元数据，而不是304的整个记录
            if(request.shouldCache() && response.cacheEntry !=null){
                cache.put(request.getCacheKey(),response.cacheEntry);
                request.addMarker("request-cache-written");

            }

            //post response back
            request.markDelivered();
            responseDelivery.postResponse(request,response);
            request.notifyListenerResponseReceived(response);
        } catch (VolleyError error) {
            error.setNetworkTimeMs(System.currentTimeMillis() - startTime);
            parseAndDeliverNetworkError(request,error);
            request.notifyListenerResponseNotUsable();
        }catch (Exception e){
            VolleyLog.e(e, "Unhandled exception %s", e.toString());
            VolleyError volleyError = new VolleyError(e);
            volleyError.setNetworkTimeMs(System.currentTimeMillis() - startTime);
            responseDelivery.postError(request, volleyError);
            request.notifyListenerResponseNotUsable();
        }
    }
    private void parseAndDeliverNetworkError(Request<?> request, VolleyError error) {
        error = request.parseNetworkError(error);
        responseDelivery.postError(request, error);
    }
}
