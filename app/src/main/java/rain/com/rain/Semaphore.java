package rain.com.rain;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Semaphore {
    private static final String TAG = "Semaphore";
    private final BlockingQueue<String> mSemaphore = new LinkedBlockingQueue<String>();

    public void sem_open() {
        try {
            mSemaphore.clear();
        } catch (Exception e) {
            android.util.Log.e(TAG, e.getMessage());
        }
    }

    public void sem_wait() {
        try {
            android.util.Log.i(TAG, "sem_wait()");
            mSemaphore.take();
        } catch (InterruptedException e) {
            android.util.Log.e(TAG, e.getMessage());
        }
    }

    public void sem_post() {
        try {
            android.util.Log.i(TAG, "sem_post()");
            mSemaphore.put(" ");
        } catch (Exception e) {
            android.util.Log.e(TAG, e.getMessage());
        }
    }
}