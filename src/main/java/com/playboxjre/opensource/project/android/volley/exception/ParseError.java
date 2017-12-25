package com.playboxjre.opensource.project.android.volley.exception;

import com.playboxjre.opensource.project.android.volley.core.NetworkResponse;

/**
 * Indicates that the server's response could not be parsed.
 * 指示服务器的响应无法解析。
 */
@SuppressWarnings("serial")
public class ParseError extends VolleyError{
    public ParseError(){}

    public ParseError(NetworkResponse response){
        super(response);
    }

    public ParseError(Throwable cause){
        super(cause);
    }
}
