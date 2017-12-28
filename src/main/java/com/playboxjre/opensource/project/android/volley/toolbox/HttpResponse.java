package com.playboxjre.opensource.project.android.volley.toolbox;

import com.playboxjre.opensource.project.android.volley.core.Header;

import java.io.InputStream;
import java.util.List;

public final class HttpResponse {
    private int statusCode;
    private List<Header> headers;
    private int contentLenght;
    private InputStream inputStream;

    public HttpResponse(int statusCode ,List<Header> headers){}


}
