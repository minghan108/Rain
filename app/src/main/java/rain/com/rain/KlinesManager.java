package rain.com.rain;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static rain.com.rain.MainActivity.localToGMTOffset;
import static rain.com.rain.MainActivity.limit;


/**
 * Created by MSI\mliu on 06/04/18.
 */

public class KlinesManager {
    private String TAG = "KlinesManager";
    private Parser parser = new Parser();

    public void sendPriceRequest(final CurrentPriceListener currentPriceListener) {
        SafeThread sendGetAllPlayableContentRequestThread = new SafeThread("sendGetAllPlayableContentRequestThread") {
            @Override
            protected void runSafe() {
                CurrentPriceListener listener = currentPriceListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        handleGetCurrentPriceResponseFromServer(response, currentPriceListener);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendGetAllPlayableContentRequest onFailure");
                        handleOnFailure(response, currentPriceListener);
                        sem.sem_post();
                    }
                };
                (new OkHttpConnection()).getResponse(getGetPriceUrl(), httpListener, "", "GET");
                sem.sem_wait();
            }
        };

        sendGetAllPlayableContentRequestThread.start();
    }

    public void sendKlinesRequest(final KlinesListener klinesListener, final long utcTimestampOffset) {
        SafeThread sendGetAllPlayableContentRequestThread = new SafeThread("sendGetAllPlayableContentRequestThread") {
            @Override
            protected void runSafe() {
                KlinesListener listener = klinesListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        handleGetKlinesResponseFromServer(response, klinesListener);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendGetAllPlayableContentRequest onFailure");
                        handleOnFailure(response, klinesListener);
                        sem.sem_post();
                    }
                };
                String symbol = "EOSETH";
                (new OkHttpConnection()).getResponse(getGetKlinesUrl(symbol, utcTimestampOffset), httpListener, "", "GET");
                sem.sem_wait();
            }
        };

        sendGetAllPlayableContentRequestThread.start();
    }

    private void handleGetKlinesResponseFromServer(String response, KlinesListener klinesListener) {
        parser.parseKlinesJsonResponse(response, klinesListener);
    }

    private String getGetKlinesUrl(String symbol, long utcTimestampOffset) {
        //return "https://api.binance.com/api/v1/klines?symbol=" + symbol + "&interval=30m&startTime=1523664000";
        return "https://api.binance.com/api/v1/klines?symbol=" + symbol + "&interval=30m&startTime=" + String.valueOf(utcTimestampOffset);
    }

    private String getGetPriceUrl() {
        return "https://api.binance.com/api/v3/ticker/price?";
    }

    private void handleGetCurrentPriceResponseFromServer(String response, CurrentPriceListener currentPriceListener) {
        SortedMap sortedMap;
        try {
            sortedMap = parser.parseCurrentPriceJsonResponse(response);
            currentPriceListener.onSuccess(sortedMap.getSortedMap(), sortedMap.getReverse_sorted_map());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleOnFailure(String response, CurrentPriceListener currentPriceListener) {
        Log.d(TAG, "handleOnFailure" + response);
        currentPriceListener.onFailure(response);
    }

    public void handleOnFailure(String response, KlinesListener klinesListener) {
        Log.d(TAG, "handleOnFailure" + response);
        klinesListener.onFailure(response);
    }

    public void handleOnFailure(String response, PriceCalculationListener priceCalculationListener) {
        Log.d(TAG, "handleOnFailure" + response);
        priceCalculationListener.onFailure(response);
    }

    public void sendDefaultKlinesRequest(final PriceCalculationListener priceCalculationListener, final String symbol) {
        SafeThread sendGetAllPlayableContentRequestThread = new SafeThread("sendGetAllPlayableContentRequestThread") {
            @Override
            protected void runSafe() {
                PriceCalculationListener listener = priceCalculationListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        Log.d(TAG, "sendGetAllPlayableContentRequest onSuccess");
                        handleGetDefaultKlinesResponseFromServer(response, priceCalculationListener, symbol);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendGetAllPlayableContentRequest onFailure");
                        handleOnFailure(response, priceCalculationListener);
                        sem.sem_post();
                    }
                };
                (new OkHttpConnection()).getResponse(getGetDefaultKlinesUrl(symbol), httpListener, "", "GET");
                sem.sem_wait();
            }
        };

        sendGetAllPlayableContentRequestThread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handleGetDefaultKlinesResponseFromServer(String response, PriceCalculationListener priceCalculationListener, String symbol) {
        parser.parseDefaultKlinesJsonResponse(response, priceCalculationListener, symbol);
    }

    private String getGetDefaultKlinesUrl(String symbol) {
//        return "https://api.binance.com/api/v1/klines?symbol=" + symbol + "&interval=30m&limit=105";
//        return "https://api.binance.com/api/v1/klines?symbol=" + symbol + "&interval=30m&limit=50";
        return "https://api.binance.com/api/v1/klines?symbol=" + symbol + "&interval=30m&limit=" + limit;

    }

    public void sendGetSymbolsRequest(final SymbolsListener symbolsListener) {
        SafeThread sendGetAllPlayableContentRequestThread = new SafeThread("sendGetAllPlayableContentRequestThread") {
            @Override
            protected void runSafe() {
                SymbolsListener listener = symbolsListener;
                final Semaphore sem = new Semaphore();
                sem.sem_open();
                OnOkhttpProcessFinish httpListener = new OnOkhttpProcessFinish() {
                    @Override
                    public void onHttpEvent(String response) {
                        handleGetSymbolsResponseFromServer(response, symbolsListener);
                        sem.sem_post();
                    }

                    @Override
                    public void onHttpFailure(String response) {
                        Log.d(TAG, "sendGetAllPlayableContentRequest onFailure");
                        symbolsListener.onFailure(response);
                        sem.sem_post();
                    }
                };
                (new OkHttpConnection()).getResponse(getGetPriceUrl(), httpListener, "", "GET");
                sem.sem_wait();
            }
        };

        sendGetAllPlayableContentRequestThread.start();
    }

    private void handleGetSymbolsResponseFromServer(String response, SymbolsListener symbolsListener) {
        List<String> symbols = new ArrayList<>();

        symbols = parser.parseSymbolsJsonResponse(response);
        symbolsListener.onSuccess(symbols);
    }
}
