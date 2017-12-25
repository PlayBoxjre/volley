package com.playboxjre.opensource.project.android.volley.exception;

/**
 * Error indicating that there was an authentication failure when performing a Request.
 * 表示执行请求时存在身份验证失败的错误。
 */
@SuppressWarnings("serial")
public class AuthFailureError extends VolleyError {
    /*可用于解决此异常的意图。（调出密码对话框。）*/
    //private Intent mResolutionIntent;

    public AuthFailureError() { }

   /* public AuthFailureError(Intent intent) {
        mResolutionIntent = intent;
    }*/
}
