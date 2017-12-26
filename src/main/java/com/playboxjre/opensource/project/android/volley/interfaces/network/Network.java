package com.playboxjre.opensource.project.android.volley.interfaces.network;

import com.playboxjre.opensource.project.android.volley.core.NetworkResponse;
import com.playboxjre.opensource.project.android.volley.core.Request;
import com.playboxjre.opensource.project.android.volley.exception.VolleyError;

/**
 * An interface for performing requests.
 */
public interface Network {
    /**
     * Performs the specified request.
     * @param request Request to process
     * @return A {@link NetworkResponse} with data and caching metadata; will never be null
     * @throws VolleyError on errors
     */
    NetworkResponse performRequest(Request<?> request) throws VolleyError;
}
