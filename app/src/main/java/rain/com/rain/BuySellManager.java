package rain.com.rain;

import android.util.Log;

public class BuySellManager {
    private String TAG = "BuySellManager";
    private Parser parser = new Parser();


    public void sendBuyRequest(final BuyListener buyListener) {
        SafeThread sendSendBuyRequestThread = new SafeThread("sendGetAllPlayableContentRequestThread") {
            @Override
            protected void runSafe() {
                BuyListener listener = buyListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        handleSendBuyResponseFromServer(response, buyListener);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendGetAllPlayableContentRequest onFailure");
                        buyListener.onFailure(response);
                        sem.sem_post();
                    }
                };
                (new OkHttpConnection()).getResponse(getBuyOrderUrl(), httpListener, "");
                sem.sem_wait();
            }
        };

        sendSendBuyRequestThread.start();
    }

    private String getBuyOrderUrl() {
        return "https://api.binance.com/api/v1/depth?symbol=ETHUSDT&limit=5";
    }

    private void handleSendBuyResponseFromServer(String response, BuyListener buyListener) {
        parser.parseSendBuyResponse(response, buyListener);
    }

    public void sendSellRequest(final BuyListener buyListener) {
        SafeThread sendSendSellRequestThread = new SafeThread("sendGetAllPlayableContentRequestThread") {
            @Override
            protected void runSafe() {
                BuyListener listener = buyListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        handleSendSellResponseFromServer(response, buyListener);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendGetAllPlayableContentRequest onFailure");
                        buyListener.onFailure(response);
                        sem.sem_post();
                    }
                };
                (new OkHttpConnection()).getResponse(getBuyOrderUrl(), httpListener, "");
                sem.sem_wait();
            }
        };

        sendSendSellRequestThread.start();
    }

    private void handleSendSellResponseFromServer(String response, BuyListener buyListener) {
        parser.parseSendSellResponse(response, buyListener);

    }

}
