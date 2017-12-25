package com.playboxjre.opensource.project.android.volley.exception;

/**
 * Error indicating that no connection could be established when performing a Volley request.
 * 执行截击请求时不能建立任何连接的错误。
 */
@SuppressWarnings("serial")
public class NoConnectionError extends VolleyError{
    public NoConnectionError() {
        super();
    }

    public NoConnectionError(Throwable reason) {
        super(reason);
    }
}
