package com.playboxjre.opensource.project.android.volley.exception;

import com.playboxjre.opensource.project.android.volley.core.NetworkResponse;

/**
 * Indicates that the server responded with an error response indicating that the client has erred.
 * 指示服务器响应错误响应表明客户有错
 * For backwards compatibility, extends ServerError which used to be thrown for all server errors,
 * including 4xx error codes indicating a client error.
 * 为了向后兼容，扩展用于被用于所有服务器错误servererror，
 * 包括4xx错误代码指示客户端错误。
 */
@SuppressWarnings("serial")
public class ClientError extends VolleyError {
    public ClientError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public ClientError() {
        super();
    }
}
