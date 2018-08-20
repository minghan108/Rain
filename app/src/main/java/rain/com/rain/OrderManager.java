package rain.com.rain;

import android.accounts.Account;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;

import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class OrderManager {
    String TAG = "OrderManager";

    private Parser parser = new Parser();
    private Resource resource = new Resource();
    public HashMap<String, Balance> balanceHashMap = new HashMap<>();


    public synchronized void sendServerTimeRequest(final ServerTimeListener serverTimeListener){
        SafeThread sendAddBookingRequestThread = new SafeThread("sendAddBookingRequestThread") {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void runSafe() {
                ServerTimeListener listener = serverTimeListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        handleServerTimeResponseFromServer(response, serverTimeListener);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendAddBookingRequest onFailure");
                        handleOnFailure(response, serverTimeListener);
                        sem.sem_post();
                    }
                };
                (new OkHttpConnection()).getResponse(getServerTimeUrl(), httpListener, "", "GET");
                sem.sem_wait();
            }
        };

        sendAddBookingRequestThread.start();

    }

    private void handleServerTimeResponseFromServer(String response, ServerTimeListener serverTimeListener) {
        Long serverTime = parser.parseServerTimeResponse(response, serverTimeListener);
        Log.d(TAG, "serverTime: " + serverTime);
        resource.setServerTime(serverTime);
        serverTimeListener.onSuccess(serverTime);

    }

    public static String encode(String secretKey, String queryString) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return new String(Hex.encodeHex(sha256_HMAC.doFinal(queryString.getBytes("UTF-8"))));
    }

    private String getServerTimeUrl() {
        return "https://api.binance.com/api/v1/time";
    }

    private void handleOnFailure(String response, ServerTimeListener serverTimeListener) {

    }

    public synchronized void sendBuyOrderRequest(final BuyOrderListener buyOrderListener, final String queryStrSignature){
        SafeThread sendAddBookingRequestThread = new SafeThread("sendAddBookingRequestThread") {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void runSafe() {
                BuyOrderListener listener = buyOrderListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        handleBuyOrderResponseFromServer(response, buyOrderListener);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendAddBookingRequest onFailure");
                        handleOnFailure(response, buyOrderListener);
                        sem.sem_post();
                    }
                };
                (new OkHttpConnection()).getResponse("https://api.binance.com/api/v3/order?" + queryStrSignature, httpListener, "X-MBX-APIKEY", "POST");
                sem.sem_wait();
            }
        };

        sendAddBookingRequestThread.start();

    }

    private void handleBuyOrderResponseFromServer(String response, BuyOrderListener buyOrderListener) {
        Log.d(TAG, "buyOrderResponse: " + response);
        Log.d(TAG, "handleBuyOrderResponseFromServer");
    }

    private void handleOnFailure(String response, BuyOrderListener buyOrderListener){
        Log.d(TAG, "BuyOrder handleOnFailure");
    }

    public synchronized void sendAccountInfoRequest(final AccountInfoListener accountInfoListener, final String queryStrSignature){
        SafeThread sendAddBookingRequestThread = new SafeThread("sendAddBookingRequestThread") {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void runSafe() {
                AccountInfoListener listener = accountInfoListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        handleAccountInfoResponseFromServer(response, accountInfoListener);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendAddBookingRequest onFailure");
                        handleOnFailure(response, accountInfoListener);
                        sem.sem_post();
                    }
                };
                (new OkHttpConnection()).getResponse("https://api.binance.com/api/v3/account?" + queryStrSignature, httpListener, "X-MBX-APIKEY", "GET");
                sem.sem_wait();
            }
        };

        sendAddBookingRequestThread.start();

    }

    private void handleAccountInfoResponseFromServer(String response, AccountInfoListener accountInfoListener){
        balanceHashMap = parser.parseAccountInfoJsonResponse(response, accountInfoListener);
        accountInfoListener.onSuccess(balanceHashMap);

    }

    private void handleOnFailure(String response, AccountInfoListener accountInfoListener) {
        Log.d(TAG, "AccountInfo onFailure" + response);
    }

    public void sendCheckOpenOrderRequest(final OpenOrderListener openOrderListener, final String queryStrSignature) {
        SafeThread sendAddBookingRequestThread = new SafeThread("sendCheckOpenOrderRequestThread") {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void runSafe() {
                OpenOrderListener listener = openOrderListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        handleCheckOpenOrderResponseFromServer(response, openOrderListener);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendCheckOpenOrderRequest onFailure");
                        handleOnFailure(response, openOrderListener);
                        sem.sem_post();
                    }
                };
                (new OkHttpConnection()).getResponse("https://api.binance.com/api/v3/openOrders?" + queryStrSignature, httpListener, "X-MBX-APIKEY", "GET");
                sem.sem_wait();
            }
        };

        sendAddBookingRequestThread.start();
    }

    private void handleCheckOpenOrderResponseFromServer(String response, OpenOrderListener openOrderListener){
        Log.d(TAG, "handleCheckOpenOrderResponseFromServer");
        openOrderListener.onSuccess(parser.parseCheckOpenOrderResponse(response, openOrderListener));

    }

    private void handleOnFailure(String response, OpenOrderListener openOrderListener) {
        Log.d(TAG, "CheckOpenOrder handleOnFailure" + response);
    }


    public void sendCancelBuyOrderRequest(final CancelBuyOrderListener cancelBuyOrderListener, final String queryStrSignature) {
        SafeThread sendAddBookingRequestThread = new SafeThread("sendCancelBuyOrderRequestThread") {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void runSafe() {
                CancelBuyOrderListener listener = cancelBuyOrderListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        handleCancelBuyOrderResponseFromServer(response, cancelBuyOrderListener);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendCheckOpenOrderRequest onFailure");
                        handleOnFailure(response, cancelBuyOrderListener);
                        sem.sem_post();
                    }
                };
                (new OkHttpConnection()).getResponse("https://api.binance.com/api/v3/order?" + queryStrSignature, httpListener, "X-MBX-APIKEY", "DELETE");
                sem.sem_wait();
            }
        };

        sendAddBookingRequestThread.start();
    }

    private void handleOnFailure(String response, CancelBuyOrderListener cancelBuyOrderListener) {
        Log.d(TAG, "CancelBuyOrder onFailure" + response);
    }

    private void handleCancelBuyOrderResponseFromServer(String response, CancelBuyOrderListener cancelBuyOrderListener) {
        Log.d(TAG, "handleCancelBuyOrderResponseFromServer");
        cancelBuyOrderListener.onSuccess();
    }
}
