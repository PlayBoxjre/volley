package volley;

import com.playboxjre.opensource.project.android.volley.core.*;
import com.playboxjre.opensource.project.android.volley.exception.VolleyError;
import com.playboxjre.opensource.project.android.volley.interfaces.network.Network;
import org.junit.Test;

public class ResponseQueueTest {

    @Test
    public void test() throws InterruptedException {
        RequestQueue queue = new RequestQueue(new mCache(),new mNetwork());
        queue.start();
        System.out.println("start");
        Thread.sleep(100);
        queue.add(new Request<String>(Request.Method.DEPRECATED_GET_OR_POST, "hh", new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error " + error.toString());
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return response == null ? Response.error(new VolleyError("error")): Response.success("hello",null);
            }

            @Override
            public void deliverResponse(String response) {
                System.out.println("deliver response " + response);
            }
        });
    }

    static class mNetwork implements Network{

        @Override
        public NetworkResponse performRequest(Request<?> request) throws VolleyError {
            return null;
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
