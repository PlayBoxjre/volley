package com.playboxjre.opensource.project.android.volley.core;

/**
 * Callback to notify when the network request returns.
 * 回调以通知网络请求何时返回
 */
public interface NetworkRequestCompleteListener {

    /** Callback when a network response has been received.接收到网络响应时的回调 */
    void onResponseReceived(Request<?> request, Response<?> response);

    /** Callback when a network response has been received.请求从网络返回时没有有效响应时的回调 */
    void onNoUsableResponseReceived(Request<?> request);
}
