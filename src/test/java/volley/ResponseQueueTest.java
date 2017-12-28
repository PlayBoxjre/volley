package volley;

import com.playboxjre.opensource.project.android.volley.core.*;
import com.playboxjre.opensource.project.android.volley.exception.VolleyError;
import com.playboxjre.opensource.project.android.volley.interfaces.cache.Cache;
import com.playboxjre.opensource.project.android.volley.interfaces.network.Network;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
        addRequest("h");
        Thread.sleep(3000);
        for(int i = 0;i< 100;i++){
            addRequest("h");
            Thread.sleep(100);
        }
        int count = 0;
        while (count++ < 5){
            Thread.sleep(1000);
        }

        Assert.assertEquals(cou,101);
    }

    int cou = 0;
    private void addRequest(String s) {

        queue.add(new Request<String>(Request.Method.DEPRECATED_GET_OR_POST, s, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error " + error.toString());
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Cache.Entry entry = new Cache.Entry();
                entry.data = response.data;
                entry.responseHeaders = response.headers;
                entry.ttl = System.currentTimeMillis()+ 72 * 3600;
                entry.softTtl = entry.ttl;
                entry.lastModified = 0;

                return response == null ? Response.error(new VolleyError("error")): Response.success(new String(response.data, Charset.forName("utf-8")),entry );
            }

            @Override
            public void deliverResponse(String response) {
                if(!Objects.isNull(response))
                    cou++;
                System.out.println("deliver response " + response);
            }
        });
    }

    static class mNetwork implements Network{

        @Override
        public NetworkResponse performRequest(Request<?> request) throws VolleyError {
            return new NetworkResponse("我是谁".getBytes());
        }
    }

   static class mCache implements Cache{
        private ConcurrentHashMap<String,Entry> map = new ConcurrentHashMap<>();
        @Override
        public Entry get(String key) {
            return map.get(key);
        }

        @Override
        public void put(String key, Entry entry) {
            map.put(key,entry);
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
