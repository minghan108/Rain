package rain.com.rain;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static rain.com.rain.SimpleMovingAverageExample.outputTextUptrend;
import static rain.com.rain.SimpleMovingAverageExample.outputTextDowntrend;

public class MainActivity extends AppCompatActivity {
    KlinesManager klinesManager = new KlinesManager();
    BuySellManager buySellManager = new BuySellManager();
    TextView rateTextView;
    TextView reverseRateTextView;
    String TAG = "MainActivity";
    Timer timer;
    private List<String> symbolsList = new ArrayList<>();
    private String[] symbolsArray = {"BTCUSDT", "LTCUSDT", "BNBUSDT", "ETHUSDT", "BCCUSDT", "ADAUSDT", "QTUMUSDT", "NEOUSDT"};
    private int symbolsIndex = 0;

    private static enum InitialDiState{

    }

    public static enum SellRemainderState{
        NOT_IN_SELL_STATE,
        SELL_50,
        SELL_75,
        SELL_85,
        SELL_100
    }

    public static enum CurrentDiState{
        CURRENT_PLUSDI_GREATER,
        CURRENT_MINUSDI_GREATER
    }

    public static enum BuyState{
        IN_BUY_STATE,
        IN_SELL_STATE,
        IN_POST_SELL_50_STATE,
        IN_POST_SELL_75_STATE,
        IN_POST_SELL_85_STATE
    }
    public static boolean isMinusDiGreater = false;
    public static boolean isFirstLaunch = true;
    public static boolean canBuy = false;
    public static InitialDiState initialDiState;
    public static CurrentDiState currentDiState;
    public static SellRemainderState sellRemainderState = SellRemainderState.NOT_IN_SELL_STATE;
    public static BuyState buyState = BuyState.IN_BUY_STATE;
    public static Double startMoney = 100.0;
    public static Double startCoin = 0.0;
    public static double maxDiDiff = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rateTextView = (TextView)this.findViewById(R.id.RatesTextView);
        reverseRateTextView = (TextView)this.findViewById(R.id.ReverseRatesTextView);
        Log.d(TAG, "timestamp: " + localToGMT());
        rateTextView.setMovementMethod(new ScrollingMovementMethod());
        reverseRateTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    public static long localToGMT() {
        return new Date().getTime()/1000;
    }

    public static long localToGMTOffset() {
        return (new Date().getTime() - 8674560000L);
//        return (new Date().getTime() - 8760960000L);
    }

    @Override
    public void onResume(){
        super.onResume();
        //sendGetSymbolsRequest();
//        sendDefKlinesRequest(symbolsArray[symbolsIndex]);
        //sendDefKlinesRequest("ETHUSDT");
        //sendCurrentPriceRequest();
        //resendKlinesRequest();
        //sendKlinesRequest(localToGMTOffset());
        //sendDefKlinesRequest(symbol);
        sendKlinesRequestTimer();
    }

//    private void sendDefKlinesRequest(String symbol) {
//        final KlinesListener klinesListener = new KlinesListener() {
//            @Override
//            public void onSuccess() {
//                symbolsIndex += 1;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        rateTextView.setText(outputTextUptrend);
//                        reverseRateTextView.setText(outputTextDowntrend);
//                    }
//                });
//
////                if (symbolsIndex < symbolsList.size()) {
////                    sendDefKlinesRequest(symbolsList.get(symbolsIndex));
////                } else {
////                    symbolsIndex = 0;
////                    sendDefKlinesRequest(symbolsList.get(symbolsIndex));
////                }
//
//                if (symbolsIndex < symbolsArray.length) {
//                    sendDefKlinesRequest(symbolsArray[symbolsIndex]);
//                } else {
//                    symbolsIndex = 0;
//                    sendDefKlinesRequest(symbolsArray[symbolsIndex]);
//                }
//            }
//
//            @Override
//            public void onFailure(String response) {
//                displayFailMessage(response);
//            }
//
//            @Override
//            public void repeatKlinesRequest(long utcTimestamp) {
//            }
//        };
//
//        klinesManager.sendDefaultKlinesRequest(klinesListener, symbol);
//    }

    private void sendDefKlinesRequest(String symbol) {
        AdxListener adxListener = new AdxListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String response) {

            }

            @Override
            public void onBuy() {
                handleBuyOrder();
            }

            @Override
            public void onSell() {
                handleSellOrder();
            }
        };
        klinesManager.sendDefaultKlinesRequest(adxListener, symbol);
    }

    private void sendKlinesRequestTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                sendDefKlinesRequest("ETHUSDT");
            }

        }, 0, 3000);
    }

    private void handleSellOrder() {
        BuyListener buyListener = new BuyListener() {
            @Override
            public void onSuccess(Double price) {
                Log.d(TAG, "askPrice: " + price);
                switch(sellRemainderState){
                    case SELL_50:
                        startMoney = startCoin * 0.5 * price;
                        startCoin = startCoin * 0.5;
                        break;
                    case SELL_75:
                        startMoney = startMoney + (startCoin * 0.75 * price);
                        startCoin = startCoin * 0.25;
                        break;
                    case SELL_85:
                        startMoney = startMoney + (startCoin * 0.85 * price);
                        startCoin = startCoin * 0.15;
                        break;
                    case SELL_100:
                        startMoney = startMoney + (startCoin * price);
                        startCoin = 0.0;
                        sellRemainderState = SellRemainderState.NOT_IN_SELL_STATE;
                        Log.d(TAG, "fiat after sell all order: " + startMoney);
                       break;
                }


                Log.d(TAG, "fiat after sell order: " + startMoney);
            }

            @Override
            public void onFailure(String response) {
                Log.d(TAG, "onFailure: " + response);
            }
        };
        buySellManager.sendSellRequest(buyListener);
    }

    private void handleBuyOrder() {
        BuyListener buyListener = new BuyListener() {
            @Override
            public void onSuccess(Double price) {
                Log.d(TAG, "askPrice: " + price);
                startCoin = startMoney/price;
                startMoney = 0.0;
                Log.d(TAG, "coin after buy order: " + startCoin);
            }

            @Override
            public void onFailure(String response) {
                Log.d(TAG, "onFailure: " + response);
            }
        };
        buySellManager.sendBuyRequest(buyListener);
    }

    private void sendKlinesRequest(long utcTimestampOffset) {
        final KlinesListener klinesListener = new KlinesListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String response) {

            }

            @Override
            public void repeatKlinesRequest(long utcTimestamp) {
                sendKlinesRequest(utcTimestamp);
            }
        };

        klinesManager.sendKlinesRequest(klinesListener, utcTimestampOffset);

    }

    private void sendGetSymbolsRequest() {
        final SymbolsListener symbolsListener = new SymbolsListener() {

            @Override
            public void onSuccess(List<String> symbols) {
                symbolsList = symbols;

                sendDefKlinesRequest(symbolsList.get(symbolsIndex));

            }

            @Override
            public void onFailure(String response) {

            }
        };

        klinesManager.sendGetSymbolsRequest(symbolsListener);

    }

    private void sendCurrentPriceRequest() {
        CurrentPriceListener currentPriceListener = new CurrentPriceListener(){

            @Override
            public void onSuccess(final TreeMap<String, Double> sorted_map, final TreeMap<String, Double> reverse_sorted_map) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rateTextView.setText(sorted_map.toString());
                        reverseRateTextView.setText(reverse_sorted_map.toString());
                    }
                });
            }

            @Override
            public void onFailure(String failMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rateTextView.setText("Initializing");
                        reverseRateTextView.setText("Initializing");
                    }
                });
            }

            @Override
            public void repeatKlinesRequest(long utcTimestamp) {

            }
        };

        klinesManager.sendPriceRequest(currentPriceListener);

    }

    private void resendKlinesRequest() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                sendCurrentPriceRequest();
            }

        }, 0, 10000);
    }

    public void displayFailMessage(final String failMessage){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setMessage(failMessage);
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Dismiss",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });

    }
}
