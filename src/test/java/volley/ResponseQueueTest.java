package volley;

import com.playboxjre.opensource.project.android.volley.core.*;
import com.playboxjre.opensource.project.android.volley.exception.VolleyError;
import com.playboxjre.opensource.project.android.volley.interfaces.network.Network;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;

public class ResponseQueueTest {
    RequestQueue queue;
    @Before
    public void before(){
          queue = new RequestQueue(new mCache(),new mNetwork());
        queue.start();
    }
    @Test
    public void test() throws InterruptedException {
        System.out.println("start");
        Thread.sleep(1000);
        queue.add(new Request<String>(Request.Method.DEPRECATED_GET_OR_POST, "hh", new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error " + error.toString());
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return response == null ? Response.error(new VolleyError("error")): Response.success(new String(response.data,Charset.forName("utf-8")),null);
            }

            @Override
            public void deliverResponse(String response) {
                System.out.println("deliver response " + response);
            }
        });


        int count = 0;
        while (count++ < 5){
            Thread.sleep(1000);
        }
    }

    static class mNetwork implements Network{

        @Override
        public NetworkResponse performRequest(Request<?> request) throws VolleyError {
            return new NetworkResponse("我是谁".getBytes());
        }
    }

   static class mCache implements Cache{

        @Override
        public Entry get(String key) {
            return null;
        }

        @Override
        public void put(String key, Entry entry) {

        }

        @Override
        public void initialize() {

        }

        @Override
        public void invalidate(String key, boolean fullExpire) {

        }

        @Override
        public void remove(String key) {

        }

        @Override
        public void clear() {

        }
    }
}
