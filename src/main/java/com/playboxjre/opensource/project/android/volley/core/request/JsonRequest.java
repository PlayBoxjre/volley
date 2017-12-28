package com.playboxjre.opensource.project.android.volley.core.request;

import com.playboxjre.opensource.project.android.volley.VolleyLog;
import com.playboxjre.opensource.project.android.volley.core.NetworkResponse;
import com.playboxjre.opensource.project.android.volley.core.Request;
import com.playboxjre.opensource.project.android.volley.core.Response;

import java.io.UnsupportedEncodingException;

public abstract class JsonRequest<T> extends Request<T> {
    /** Default charset for JSON request. */
    protected static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    /** Lock to guard mListener as it is cleared on cancel() and read on delivery. */
    private final Object mLock = new Object();

    // @GuardedBy("mLock")
    private Response.Listener<T> mListener;
    private final String mRequestBody;

    public JsonRequest(int method, String url, String requestBody, Response.Listener<T> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mRequestBody = requestBody;
    }

    @Override
    public void cancel() {
        super.cancel();
        synchronized (mLock) {
            mListener = null;
        }
    }

    @Override
    protected void deliverResponse(T response) {
        Response.Listener<T> listener;
        synchronized (mLock) {
            listener = mListener;
        }
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    @Override
    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

}
