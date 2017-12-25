package com.playboxjre.opensource.project.android.volley.interfaces.retry;

import com.playboxjre.opensource.project.android.volley.exception.VolleyError;

/**
 * Retry policy for a request.
 * 请求的重试策略
 */
public interface RetryPolicy {

    /**
     * Returns the current timeout (used for logging).
     */
    int getCurrentTimeout();

    /**
     * Returns the current retry count (used for logging).
     */
    int getCurrentRetryCount();

    /**
     * Prepares for the next retry by applying a backoff to the timeout.
     * 通过对超时应用回退来为下一次重试做准备
     * @param error The error code of the last attempt.
     * @throws VolleyError In the event that the retry could not be performed (for example if we
     * ran out of attempts), the passed in error is thrown.
     */
    void retry(VolleyError error) throws VolleyError;
}
