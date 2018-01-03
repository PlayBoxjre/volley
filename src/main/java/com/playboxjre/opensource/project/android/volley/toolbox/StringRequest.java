package com.playboxjre.opensource.project.android.volley.toolbox;

import com.playboxjre.opensource.project.android.volley.core.NetworkResponse;
import com.playboxjre.opensource.project.android.volley.core.Request;
import com.playboxjre.opensource.project.android.volley.core.Response;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * PROJECT     :   opensource-project
 * AUTHOR      :   Kong Xiang&Aaron
 * CREATEDAT   :   2017/12/27 21:59 星期三
 * EMAIL       :   playboxjre@Gmail.com
 * DESCRIPTION :
 */
public class StringRequest extends Request<String> {

    private Map<String,String> params ;
    private final Object lock = new Object();

    private Response.Listener<String> listener;

    public StringRequest(int method, String url, Map<String,String> params,Response.Listener<String> listener, Response.ErrorListener errorListener){
        this(method,url,listener,errorListener);
        this.params = params;
    }

    public StringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener){
        super(method,url,errorListener);
        this.listener = listener;
    }

    public StringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener){
        this(Method.GET,url,listener,errorListener);
    }

    @Override
    public void cancel() {
        super.cancel();
        synchronized (lock){
                listener = null;
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try{
            parsed = new String(response.data,HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        Response<String> success = Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
        //System.out.println("successs to "+ success);
        return Response.success(parsed,HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public void deliverResponse(String response) {
        Response.Listener<String> listener;
        synchronized (lock){
            listener = this.listener;
        }
        if(listener!=null)
            listener.onResponse(response);
    }

    @Override
    public Map<String, String> getParams() {
        return Objects.isNull(params)? Collections.emptyMap():params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
