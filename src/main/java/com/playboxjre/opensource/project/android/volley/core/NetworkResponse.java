package com.playboxjre.opensource.project.android.volley.core;

import com.playboxjre.opensource.project.android.volley.interfaces.network.Network;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * Data and headers returned from {@link Network#performRequest(Request)}.
 */
public class NetworkResponse {


    /** The HTTP status code. */
    public final int statusCode;

    /** Raw data from this response. */
    public final byte[] data;

    /**
     * Response headers.
     *
     * <p>This map is case-insensitive. It should not be mutated directly.
     *
     * <p>Note that if the server returns two headers with the same (case-insensitive) name, this
     * map will only contain the last one. Use {@link #allHeaders} to inspect all headers returned
     * by the server.
     */
    public final Map<String, String> headers;

    /** All response headers. Must not be mutated directly. */
    public final List<Header> allHeaders;

    /** True if the server returned a 304 (Not Modified). */
    public final boolean notModified;

    /** Network roundtrip time in milliseconds. */
    public final long networkTimeMs;


    /**
     * Creates a new network response.
     * @param statusCode the HTTP status code
     * @param data Response body
     * @param headers Headers returned with this response, or null for none
     * @param notModified True if the server returned a 304 and the data was already in cache
     * @param networkTimeMs Round-trip network time to receive network response
     * @deprecated see {@link #NetworkResponse(int, byte[], boolean, long, List)}. This constructor
     *             cannot handle server responses containing multiple headers with the same name.
     *             This constructor may be removed in a future release of Volley.
     */
    @Deprecated
    public NetworkResponse(int statusCode, byte[] data, Map<String,String> headers, boolean notModified, long networkTimeMs){
        this(statusCode,data,headers,toAllHeaderList(headers),notModified,networkTimeMs);
    }

    /**
     * Creates a new network response.
     * @param statusCode the HTTP status code
     * @param data Response body
     * @param notModified True if the server returned a 304 and the data was already in cache
     * @param networkTimeMs Round-trip network time to receive network response
     * @param allHeaders All headers returned with this response, or null for none
     */
    public NetworkResponse(int statusCode, byte[] data, boolean notModified, long networkTimeMs,
                           List<Header> allHeaders){
        this(statusCode,data,toHeaderMap(allHeaders),allHeaders,notModified,networkTimeMs);
    }
    /**
     * Creates a new network response.
     * @param statusCode the HTTP status code
     * @param data Response body
     * @param headers Headers returned with this response, or null for none
     * @param notModified True if the server returned a 304 and the data was already in cache
     * @deprecated see {@link #NetworkResponse(int, byte[], boolean, long, List)}. This constructor
     *             cannot handle server responses containing multiple headers with the same name.
     *             This constructor may be removed in a future release of Volley.
     */
    @Deprecated
    public NetworkResponse(int statusCode, byte[] data, Map<String,String> headers, boolean notModified){
        this(statusCode,data,headers,notModified,0);
    }

    /**
     * 创建一个没有header 的响应
     * @param data
     */
    public NetworkResponse(byte[] data){
        this(HttpURLConnection.HTTP_OK,data,false,0,Collections.emptyList());
    }

    @Deprecated
    public NetworkResponse(byte[] data, Map<String,String> headers){
        this(HttpURLConnection.HTTP_OK,data,headers,false);
    }


    private NetworkResponse(int statusCode, byte[] data, Map<String, String> headers, List<Header> allHeaders, boolean notModified, long networkTimeMs) {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
        if(allHeaders == null){
            this.allHeaders = null;
        }else{
            this.allHeaders = Collections.unmodifiableList(allHeaders);
        }
        this.notModified = notModified;
        this.networkTimeMs = networkTimeMs;
    }


    private static Map<String,String> toHeaderMap(List<Header> allHeaders){
        if(allHeaders == null){
            return null;
        }
        if (allHeaders == null){
            return Collections.emptyMap();
        }
        Map<String,String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        // Later elements in the list take precedence.
        //列表后面的元素优先。
        for (Header header :
                allHeaders) {
            headers.put(header.getName(),header.getValue());
        }
        return headers;
    }

    private static List<Header> toAllHeaderList(Map<String,String> headers){
        if(headers== null)
            return  null;
        if(headers.isEmpty())
            Collections.emptyList();
        List<Header> allHeaders = new ArrayList<>(headers.size());
        for (Map.Entry<String,String > h:headers.entrySet()){
            allHeaders.add(new Header(h.getKey(),h.getValue()));
        }
        return allHeaders;
    }
}
