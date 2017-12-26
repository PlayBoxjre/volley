package com.playboxjre.opensource.project.android.volley.interfaces;

import com.playboxjre.opensource.project.android.volley.core.Request;
import com.playboxjre.opensource.project.android.volley.core.Response;
import com.playboxjre.opensource.project.android.volley.exception.VolleyError;

import java.util.concurrent.Executor;
/**
 * Delivers responses and errors.
 */
public class ExecutorDelivery implements ResponseDelivery{

    private final Executor responseExecutor;

    public ExecutorDelivery(){
        // Make an Executor that just wraps the handler.
        responseExecutor = new Executor() {
            @Override
            public void execute(Runnable command) {
               // handler.post(command);
                command.run();
            }
        };
    }


    public ExecutorDelivery(Executor executor){
        this.responseExecutor = executor;
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response) {
        postResponse(request,response,null);
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response, Runnable runnable) {
        request.markDelivered();
        request.addMarker("post-response");
        responseExecutor.execute(new ResponseDeliveryRunnable(request,response,runnable));
    }

    @Override
    public void postError(Request<?> request, VolleyError error) {
        request.addMarker("post-error");
        Response<?> response = Response.error(error);
        responseExecutor.execute(new ResponseDeliveryRunnable(request,response,null));
    }


    /**
     * A Runnable used for delivering network responses to a listener on the
     * main thread.
     */
    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable implements Runnable {
        private final Request request;
        private final Response response;
        private final Runnable runnable;

        public ResponseDeliveryRunnable(Request request, Response response, Runnable runnable) {
            this.request = request;
            this.response = response;
            this.runnable = runnable;
        }


        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            // NOTE: If cancel() is called off the thread that we're currently running in (by
            // default, the main thread), we cannot guarantee that deliverResponse()/deliverError()
            // won't be called, since it may be canceled after we check isCanceled() but before we
            // deliver the response. Apps concerned about this guarantee must either call cancel()
            // from the same thread or implement their own guarantee about not invoking their
            // listener after cancel() has been called.
            // If this request has canceled, finish it and don't deliver.
            if (request.isCanceled()) {
                request.finish("canceled-at-delivery");
                return;
            }

            // Deliver a normal response or error, depending.
            if (response.isSuccess()) {
                request.deliverResponse(response.result);
            } else {
                request.deliverError(response.volleyError);
            }


            // If this is an intermediate response, add a marker, otherwise we're done
            // and the request can be finished.
            if (response.intermediate) {
                request.addMarker("intermediate-response");
            } else {
                request.finish("done");
            }

            if (runnable!=null)
                runnable.run();
        }
    }

}
