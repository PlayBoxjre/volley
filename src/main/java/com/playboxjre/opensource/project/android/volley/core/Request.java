package com.playboxjre.opensource.project.android.volley.core;

import com.playboxjre.opensource.project.android.volley.VolleyLog;
import com.playboxjre.opensource.project.android.volley.exception.AuthFailureError;
import com.playboxjre.opensource.project.android.volley.exception.VolleyError;
import com.playboxjre.opensource.project.android.volley.interfaces.cache.Cache;
import com.playboxjre.opensource.project.android.volley.interfaces.retry.DefaultRetryPolicy;
import com.playboxjre.opensource.project.android.volley.interfaces.retry.RetryPolicy;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for all network requests.
 *
 * @param <T> The type of parsed response this request expects.
 */
public abstract class Request<T> implements Comparable<Request<T>>{

    /**
     * Default encoding for POST or PUT parameters. See {@link #getParamsEncoding()}.
     */
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    public interface Method{
        int DEPRECATED_GET_OR_POST = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    private final VolleyLog.MarkerLog markerLog = VolleyLog.MarkerLog.ENABLED?new VolleyLog.MarkerLog():null;

    private final int method;
    private final String url;
    /**Default tag for程序流量状态*/
    private final int defaultTrafficStatsTag;

    private final Object lock = new Object();

    private Response.ErrorListener errorListener;

    /**此请求的序列号，用于强制FIFO排序*/
    private Integer sequence;

    /** The request queue this request is associated with. */
    private RequestQueue requestQueue;

    private boolean shouldCache =true;

    private boolean canceled = false;

    /**是否已经为这个请求发送了响应*/
    private boolean responseDelivered = false;

    /**在发生http 5xx(服务器)错误时是否应重新尝试请求*/
    private boolean shouldRetryServerErrors = false;

    /** 重试策略 */
    private RetryPolicy retryPolicy;

    /**
     * 网络请求未修改状态直接使用，防止缓冲中被丢弃
     * 当从缓存中检索到请求，但必须从*网络刷新请求时
     * ，缓存条目将被存储在此处，以便在*a“未修改”响应
     * 的情况下，我们可以确保它没有从缓存中逐出
     */
    private Cache.Entry cacheEntry = null;

    /**标记此请求的不透明标记；用于批量取消*/
    private Object tag;

    private NetworkRequestCompleteListener networkRequestCompleteListener;

    @Deprecated
    public Request(String url, Response.ErrorListener listener){
        this(Method.DEPRECATED_GET_OR_POST,url,listener);
    }

    public Request(int method, String url, Response.ErrorListener listener){
        this.method = method;
        this.url = url;
        this.errorListener = listener;
        setRetryPolicy(new DefaultRetryPolicy());
        this.defaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }

    public int getMethod(){
        return this.method;
    }

    public Request<?> setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public Object getTag() {
        return tag;
    }

    public Response.ErrorListener getErrorListener() {
        return errorListener;
    }

    public int getTrafficStatsTag() {
        return defaultTrafficStatsTag;
    }

    private static int findDefaultTrafficStatsTag(String url){
        if(!Objects.isNull(url)&& url.length() > 0){
            URI uri = URI.create(url);
            if(uri!=null){
                String host = uri.getHost();
                if(host!=null){
                    return host.hashCode();
                }
            }
        }
        return 0;
    }

    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    public void addMarker(String tag){
        if(VolleyLog.MarkerLog.ENABLED){
            markerLog.add(tag,Thread.currentThread().getId());
        }
    }

    /**
     * Notifies the request queue that this request has finished (successfully or with error).
     *
     * <p>Also dumps all events from this request's event log; for debugging.</p>
     */
    public void finish(String tag){
        if(requestQueue !=null) {
            requestQueue.finish(this);
        }
        if(VolleyLog.MarkerLog.ENABLED){
            final long threadId = Thread.currentThread().getId();
            markerLog.add(tag, threadId);
            markerLog.finish(this.toString());
        }

    }

    public Request<?> setRequestQueue(RequestQueue requestQueue){
        this.requestQueue = requestQueue;
        return this;
    }

    public Request<?> setSequence(Integer mSequence) {
        this.sequence = mSequence;
        return this;
    }

    public final int getSequence() {
        if(sequence == null)
            throw new IllegalStateException("getSequence called before setSequence");
        return sequence;
    }

    public String getUrl() {
        return url;
    }

    public String getCacheKey(){
        return getUrl();
    }

    public Request<?> setCacheEntry(Cache.Entry cacheEntry) {
        this.cacheEntry = cacheEntry;
        return this;
    }

    public Cache.Entry getCacheEntry() {
        return cacheEntry;
    }

    public void cancel(){
        synchronized (lock){
            this.canceled = true;
            errorListener = null;
        }
    }

    public boolean isCanceled() {
        synchronized (lock) {
            return canceled;
        }
    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        return Collections.emptyMap();
    }

    protected Map<String, String> getParams() throws AuthFailureError {
        return getHeaders();
    }

    protected String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public String getBodyContentType(){
        return "application/x-www-form-urlencoded; charset="+getParamsEncoding();
    }

    public byte[] getBody() throws AuthFailureError {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    private byte[] encodeParameters(Map<String,String> params,String paramsEncoding){
        StringBuilder encodeParmas = new StringBuilder();
        try {
            for(Map.Entry<String,String> entry: params.entrySet()){
                encodeParmas.append(URLEncoder.encode(entry.getKey(),paramsEncoding));
                encodeParmas.append('=');
                encodeParmas.append(URLEncoder.encode(entry.getValue(),paramsEncoding));
                encodeParmas.append('&');
            }
            return encodeParmas.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, e);
        }
    }

    public final Request<?> setShouldCache(boolean shouldCache) {
        this.shouldCache = shouldCache;
        return this;
    }

    public final boolean shouldCache() {
        return shouldCache;
    }

    public final Request<?> setShouldRetryServerErrors(boolean shouldRetryServerErrors) {
        this.shouldRetryServerErrors = shouldRetryServerErrors;
        return this;
    }

    public final boolean shouldRetryServerErrors() {
        return shouldRetryServerErrors;
    }

    public enum Priority{
        LOW,NORMAL,HIGH,IMMEDIATE
    }

    public Priority getPriority(){
        return Priority.NORMAL;
    }

    public int getTimeoutMs(){
        return this.retryPolicy.getCurrentTimeout();
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void markDelivered(){
        synchronized (lock){
            this.responseDelivered = true;
        }
    }

    public boolean hasHadResponseDelivered(){
        synchronized (lock){
            return responseDelivered;
        }
    }



    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);


    protected VolleyError parseNetworkError(VolleyError error){
        return error;
    }

    protected abstract void deliverResponse(T response);

    public void deliverError(VolleyError error){
        Response.ErrorListener listener ;
        synchronized (lock){
            listener = errorListener;
        }
        if(listener != null)
            listener.onErrorResponse(error);
    }

    public void setNetworkRequestCompleteListener(NetworkRequestCompleteListener networkRequestCompleteListener) {
        synchronized (lock) {
            this.networkRequestCompleteListener = networkRequestCompleteListener;
        }
    }

    public void notifyListenerResponseReceived(Response<?> response){
        NetworkRequestCompleteListener requestCompleteListener;
        synchronized (lock){
            requestCompleteListener = networkRequestCompleteListener;
        }
        if(requestCompleteListener!=null)
            requestCompleteListener.onResponseReceived(this,response);
    }

    public void notifyListenerResponseNotUsable(){
        NetworkRequestCompleteListener requestCompleteListener;
        synchronized (lock){
            requestCompleteListener = networkRequestCompleteListener;
        }
        if(requestCompleteListener!=null)
            requestCompleteListener.onNoUsableResponseReceived(this);
    }

    @Override
    public int compareTo(Request<T> o) {
        Priority left = this.getPriority();
        Priority right = o.getPriority();
        return left == right?
                this.sequence - o.sequence:
                right.ordinal() - left.ordinal();
    }


    @Override
    public String toString() {
        String trafficStatsTag = "0x" + Integer.toHexString(getTrafficStatsTag());
        return (canceled ? "[X] " : "[ ] ") + getUrl() + " " + trafficStatsTag + " "
                + getPriority() + " " + sequence;
    }
}
