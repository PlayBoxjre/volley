package com.playboxjre.opensource.project.android.volley.interfaces.retry;

import com.playboxjre.opensource.project.android.volley.exception.VolleyError;

/**
 * Default retry policy for requests.
 */
public class DefaultRetryPolicy implements RetryPolicy {
    /**
     * 当前超时毫秒
     */
    private int currentTimeoutMs;

    /** The current retry count */
    private int currentRetryCount;

    /** The maximum number of attempts .*/
    private final int maxCountRetries;

    /**The backoff multiplier for the policy 策略的退避乘数*/
    private final float backoffMutiplier;

    /** The default socket timeout in milliseconds */
    public static final int DEFAULT_TIMEOUT_MS = 2500;

    /** The default number of retries */
    public static final int DEFAULT_MAX_RETRIES = 1;

    /** The default backoff multiplier */
    public static final float DEFAULT_BACKOFF_MULTIPLIER = 1F;

    public DefaultRetryPolicy(){
        this(DEFAULT_TIMEOUT_MS,DEFAULT_MAX_RETRIES,DEFAULT_BACKOFF_MULTIPLIER);
    }

    /**
     * Constructs a new retry policy.
     * @param initialTimeout The initial timeout for the policy.
     * @param maxNumRetires The maximum number of retries.
     * @param backoffMutiplier Backoff multiplier for the policy.
     */
    public DefaultRetryPolicy(int initialTimeout,int maxNumRetires,float backoffMutiplier){
        this.currentTimeoutMs = initialTimeout;
        this.maxCountRetries = maxNumRetires;
        this.backoffMutiplier = backoffMutiplier;
    }

    @Override
    public int getCurrentTimeout() {
        return currentTimeoutMs;
    }

    @Override
    public int getCurrentRetryCount() {
        return currentRetryCount;
    }


    /**
     * Returns the backoff multiplier for the policy.
     */
    public float getBackoffMultiplier() {
        return backoffMutiplier;
    }


    @Override
    public void retry(VolleyError error) throws VolleyError {
        currentRetryCount++;
        currentTimeoutMs += (currentTimeoutMs * backoffMutiplier);
        if(!hasAttemptRemaining()){
            throw error;
        }
    }

    protected boolean hasAttemptRemaining(){
        return currentRetryCount <= maxCountRetries;
    }
}
