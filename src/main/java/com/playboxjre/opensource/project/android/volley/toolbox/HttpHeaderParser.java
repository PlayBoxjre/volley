package com.playboxjre.opensource.project.android.volley.toolbox;

import com.playboxjre.opensource.project.android.volley.VolleyLog;
import com.playboxjre.opensource.project.android.volley.core.Cache;
import com.playboxjre.opensource.project.android.volley.core.Header;
import com.playboxjre.opensource.project.android.volley.core.NetworkResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * PROJECT     :   opensource-project
 * AUTHOR      :   Kong Xiang&Aaron
 * CREATEDAT   :   2017/12/27 22:10 星期三
 * EMAIL       :   playboxjre@Gmail.com
 * DESCRIPTION :
 */
public class HttpHeaderParser {
    static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String DEFAULT_CONTENT_CHARSET = "ISO-8859-1";
    private static final String RFC1123_FORMAT = "EEE, dd MMM yyyy HH:mm::ss zzz";

    public static Cache.Entry parseCacheHeaders(NetworkResponse response){
        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;

        long serverDate = 0;
        long lastModified = 0;
        long serverExpire = 0;
        long softExpire = 0;
        long finalExpire = 0;
        long maxAge = 0;
        long staleWhileRevalidate = 0;
        boolean hasCacheControl = false;
        boolean mustRevalidate = false;

        String serverTag = null;
        String headerValue ;

        headerValue = headers.get("date");
        if(Objects.nonNull(headerValue))
            serverDate = parseDateAsEpoch(headerValue);

        headerValue = headers.get("cache-control");
        if(Objects.nonNull(headerValue)){
            hasCacheControl = true;
            String[] tokens = headerValue.split(",");
            for(int i = 0;i < tokens.length; i++){
                String token = tokens[i].trim();
                if(Objects.equals("no-cache",token)||
                        Objects.equals("no-store",token)) {
                    return null;
                }else if(token.startsWith("max-age=")){
                    try {
                        maxAge = Long.parseLong(token.substring(8));
                    }catch (Exception e){
                    }
                }else if(token.startsWith("stale-while-revalidate=")){
                    try{
                        staleWhileRevalidate = Long.parseLong(token.substring(23));
                    }catch (Exception e){
                    }
                }else if(Objects.equals("must-revalidate",token) || Objects.equals("proxy-revalidate",token)){
                    mustRevalidate = true;
                }
            }
        }

        headerValue = headers.get("Expires");
        if(Objects.nonNull(headerValue)){
            serverExpire = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Last-Modified");
        if(Objects.nonNull(headerValue)){
            lastModified = parseDateAsEpoch(headerValue);
        }

        serverTag = headers.get("ETag");

        if(hasCacheControl){
            // Cache-Control takes precedence over(要高于) an Expires header, even if both exist and Expires is more restrictive.
            softExpire = now + maxAge * 1000;
            finalExpire = mustRevalidate?softExpire:softExpire+staleWhileRevalidate*1000;
        }else if(serverDate > 0 && serverExpire >=serverDate){
            //Default semantic for Expire header in HTTP specification is softExpire.
            //HTTP规范中过期标头的默认语义是软过期。
            softExpire = now +(serverExpire - serverDate);
            finalExpire = softExpire;
        }

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverTag;
        entry.softTtl = softExpire;
        entry.ttl = finalExpire;
        entry.serverDate = serverDate;
        entry.lastModified = lastModified;
        entry.responseHeaders = headers;
        entry.allResponseHeaders = response.allHeaders;
        return entry;
    }

    /**
     *  Parse date in RFC1123 format, and return its value as epoch
     * 解析rfc1123格式的日期，作为时代的返回值
     */
    public static long parseDateAsEpoch(String dateStr) {
        try {
            return newRfc1123Formatter().parse(dateStr).getTime();
        }catch (ParseException e){
            // Date in invalid format, fallback to 0
            VolleyLog.e(e, "Unable to parse dateStr: %s, falling back to 0", dateStr);
            return 0;
        }
    }

    /** Format an epoch date in RFC1123 format. */
    static String formatEpochAsRfc1123(long epoch) {
        return newRfc1123Formatter().format(new Date(epoch));
    }

    private static SimpleDateFormat newRfc1123Formatter() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RFC1123_FORMAT, Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat;
    }

    public static String parseCharset(Map<String,String> headers,String defaultCharset){
        String contentType = headers.get(HEADER_CONTENT_TYPE);
        if(contentType!=null){
            String[] params = contentType.split(";");
            for(int i = 0;i < params.length; i++){
                String[] pair = params[i].trim().split("=");
                if(pair.length == 2){
                    if(pair[0].equals("charset")){
                        return pair[1];
                    }
                }
            }
        }
        return defaultCharset;
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    public static String parseCharset(Map<String,String> headers){
       return parseCharset(headers,DEFAULT_CONTENT_CHARSET);
    }

    public Map<String,String> toHeaderMap(List<Header> allHeaders){
        Objects.requireNonNull(allHeaders);
        Map<String,String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for(Header header: allHeaders){
            headers.put(header.getName(),header.getValue());
        }
        return headers;
    }

    public List<Header> toAllHeaderList(Map<String,String> headers){
        Objects.requireNonNull(headers);
        List<Header> allHeaders = new ArrayList<>();
        for(Map.Entry<String,String> entry:headers.entrySet()){
            allHeaders.add(new Header(entry.getKey(),entry.getValue()));
        }
        return allHeaders;
    }
}
