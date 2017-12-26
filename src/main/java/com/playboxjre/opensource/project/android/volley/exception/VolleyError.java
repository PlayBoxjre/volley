package com.playboxjre.opensource.project.android.volley.exception;

import com.playboxjre.opensource.project.android.volley.core.NetworkResponse;

/**
 * Exception style class encapsulating Volley errors
 */
@SuppressWarnings("serial")
public class VolleyError extends Exception {

    public final NetworkResponse networkResponse;
    private long networkTimeMs;

    public VolleyError(){
        networkResponse = null;
    }

    public VolleyError(NetworkResponse networkResponse){
        this.networkResponse = networkResponse;
    }

    public VolleyError(String exceptionMessage){
        super(exceptionMessage);
        networkResponse = null;
    }

    public VolleyError(String exceptionMessage,Throwable throwable){
        super(exceptionMessage,throwable);
        networkResponse = null;
    }

    public VolleyError(Throwable cause){
        super(cause);
        networkResponse = null;
    }

    /*package*/
    public  void setNetworkTimeMs(long networkTimeMs){
        this.networkTimeMs = networkTimeMs;
    }

    public long getNetworkTimeMs() {
        return networkTimeMs;
    }
}
