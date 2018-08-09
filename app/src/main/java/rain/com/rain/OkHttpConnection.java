package rain.com.rain;


import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Semaphore;


public class OkHttpConnection {
    private final static String TAG = " [OkHttpConnection] ";
    private String responseData = "";
    private OnOkhttpProcessFinish mListener;
    private final static int MAX_CONCURRENCY_HTTP_CONNECTION = 3;
    private static Semaphore httpRequestThreadSemaphore = new Semaphore(MAX_CONCURRENCY_HTTP_CONNECTION);
    private String apiKey = "FZc5Yvjaq046dssf83NHnciEV7vadftqTH4CsFyxof2LGYfC4kEaItLlZzwixGnh";



    public void getResponse(final String Url, OnOkhttpProcessFinish onOkhttpProcessFinishListener, String httpHeader, String httpCmd){
        try {
            httpRequestThreadSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mListener = onOkhttpProcessFinishListener;
        Log.d(TAG, " Command sent to dongle:" + Url);

        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(Url);
            urlConnection = (HttpURLConnection) url.openConnection();
            if (httpHeader != ""){
                urlConnection.setRequestProperty(httpHeader, apiKey);
            }
            urlConnection.setRequestMethod(httpCmd);
            urlConnection.setDefaultUseCaches(false);
            urlConnection.setUseCaches(false);

            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            int responseCode = urlConnection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                responseData = readStream(urlConnection.getInputStream());
//                try {
//                    getLogger().info(TAG + "get http response" + responseData);
//                } catch(Exception e) {
//                    getLogger().info(TAG + "output response due to the format issue");
//                }
                onResponse(responseData);
            } else {
                Log.d(TAG, "getResponse onFailure: ");
                onFailure(responseCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "getResponse onFailure IOException: ");
            onFailure(-1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getResponse onFailure Exception: ");
            onFailure(-1);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }


    private void onFailure(int httpErrorCode) {
        httpRequestThreadSemaphore.release();
        Log.d(TAG, "Call On Failure " + httpErrorCode);
        if (mListener != null) {
            mListener.onHttpFailure(String.valueOf(httpErrorCode));
        }
    }


    private void onResponse(String responseData) {
        httpRequestThreadSemaphore.release();
        if (mListener != null) {
            mListener.onHttpEvent(responseData);
        }
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}