package com.playboxjre.opensource.project.android.volley.core;


import com.playboxjre.opensource.project.android.volley.exception.VolleyError;

/**
 * Encapsulates a parsed response for delivery.
 *
 * @param <T> Parsed type of this response
 */
public class Response<T> {

    public interface Listener<T>{
        /** Called when a response is received.
         * 在收到响应时调用*/
        void onResponse(T response);
    }

    /** Callback interface for delivering error responses. */
    public interface ErrorListener{
        void onErrorResponse(VolleyError error);
    }

    public static <T> Response<T>  success(T result, Cache.Entry cacheEntry){
        return new Response<>(result,cacheEntry);
    }

    public static <T> Response<T> error(VolleyError err){
        return new Response<>(err);
    }


    public final T result;

    public final Cache.Entry cacheEntry;

    public final VolleyError volleyError;

    /**如果这个响应是软过期的，那么第二个可能会到来。*/
    public boolean intermediate = false;

    public boolean isSuccess(){
        return volleyError == null;
    }

    private Response(T result, Cache.Entry cacheEntry) {
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.volleyError = null;
    }

    private Response(VolleyError error){
        this.result = null;
        this.cacheEntry = null;
        this.volleyError = error;
    }
}
