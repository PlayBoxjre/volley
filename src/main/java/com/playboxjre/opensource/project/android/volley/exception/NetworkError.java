package com.playboxjre.opensource.project.android.volley.exception;

import com.playboxjre.opensource.project.android.volley.core.NetworkResponse;

/**
 * Indicates that there was a network error when performing a Volley request.
 * 执行请求时，发生了网络错误
 */
@SuppressWarnings("serial")
public class NetworkError extends VolleyError {
    public NetworkError() {
        super();
    }

    public NetworkError(Throwable cause) {
        super(cause);
    }

    public NetworkError(NetworkResponse networkResponse) {
        super(networkResponse);
    }
}
