package com.playboxjre.opensource.project.android.volley.exception;

import com.playboxjre.opensource.project.android.volley.core.NetworkResponse;

/**
 * indicates that the server responsed with an error response
 */
@SuppressWarnings("serial")
public class ServerError extends VolleyError {
    public ServerError(NetworkResponse networkResponse){
        super(networkResponse);
    }

    public ServerError(){
        super();
    }
}
