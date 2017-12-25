package com.playboxjre.opensource.project.android.volley.interfaces;

import com.playboxjre.opensource.project.android.volley.core.Request;
import com.playboxjre.opensource.project.android.volley.core.Response;
import com.playboxjre.opensource.project.android.volley.exception.VolleyError;

public interface ResponseDelivery {
    /**
     * Parses a response from the network or cache and delivers it.
     */
    void postResponse(Request<?> request, Response<?> response);

    /**
     * Parses a response from the network or cache and delivers it. The provided
     * Runnable will be executed after delivery.
     */
    void postResponse(Request<?> request, Response<?> response, Runnable runnable);

    /**
     * Posts an error for the given request.
     */
    void postError(Request<?> request, VolleyError error);
}
