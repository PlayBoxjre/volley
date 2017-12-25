package volley;

import com.playboxjre.opensource.project.android.volley.VolleyLog;
import org.junit.Test;

public class VolleyLogTest {

    @Test
    public void testLog(){
        VolleyLog.v(" %d %s %s \n",1,"hello ","world");
        VolleyLog.e(" %d %s %s \n",1,"hello ","world");
        VolleyLog.wtf(" %d %s %s \n",1,"hello ","world");
    }

    @Test
    public void testLogMaker(){
         VolleyLog.MarkerLog markerLog = new VolleyLog.MarkerLog();
        markerLog.add("1",Thread.currentThread().getId());
        markerLog.add("2",Thread.currentThread().getId());
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        markerLog.add("3",Thread.currentThread().getId());
        markerLog.add("4",Thread.currentThread().getId());
        markerLog.add("5",Thread.currentThread().getId());
        markerLog.add("6",Thread.currentThread().getId());
        markerLog.add("7",Thread.currentThread().getId());

        markerLog.finish("header");
    }
}
