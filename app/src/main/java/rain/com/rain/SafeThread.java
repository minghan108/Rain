package rain.com.rain;

import android.util.Log;

public class SafeThread extends Thread {
    private static final String TAG = "thread";
    public SafeThread(String name) {
        super(name);
    }

    public SafeThread(Runnable runnable, String name) {
        super(runnable, name);
    }

    protected void runSafe() throws Exception {
        super.run();
    }

    @Override
    public final void run() {
        try {
            runSafe();
        } catch (Throwable e) {
            Log.e(TAG, "Critical:" + getName(), e);
        }
    }
}