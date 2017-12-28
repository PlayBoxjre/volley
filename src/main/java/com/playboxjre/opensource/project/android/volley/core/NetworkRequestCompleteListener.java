package com.playboxjre.opensource.project.android.volley.core;

/**
 * Callback to notify when the network request returns.
 *  请求结果完成后，通知结果缓存和等待请求集合如何处理
 */
public interface NetworkRequestCompleteListener {

    /** Callback when a network response has been received.接收到网络响应时的回调 */
    void onResponseReceived(Request<?> request, Response<?> response);

    /** Callback when a network response has been received.请求从网络返回时没有有效响应时的回调 */
    void onNoUsableResponseReceived(Request<?> request);
}
