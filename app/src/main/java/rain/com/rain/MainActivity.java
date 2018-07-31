package rain.com.rain;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    KlinesManager klinesManager = new KlinesManager();
    BuySellManager buySellManager = new BuySellManager();
    OrderManager orderManager = new OrderManager();
    TextView rateTextView;
    TextView reverseRateTextView;
    TextView breakoutTextView;
    String TAG = "MainActivity";
    Timer timer;
    private String breakoutTextViewString = "";
    private List<String> symbolsList = new ArrayList<>();
    private List<Double> closePriceList = new ArrayList<>();
    private String[] symbolsArray = {"BTCUSDT", "LTCUSDT", "BNBUSDT", "ETHUSDT", "BCCUSDT", "ADAUSDT", "QTUMUSDT", "NEOUSDT"};
    private int symbolsIndex = 0;
    private double buyPrice = 0.0;
    private String secretKey = "";
    private double buyQuantity = 0.0;
    private double usdtCoin = 0.0;
    private int cancelBuyOrderIdListLength = 0;
    private int cancelBuyOrderOnSuccessCount = 0;
    private double buyOrderTier1 = 0.0;
    private double buyOrderTier2 = 0.0;
    private double buyOrderTier3 = 0.0;
    private double buyOrderTier4 = 0.0;
    private List<Double> buyOrderTierList = new ArrayList<>();
    private List<Double> buyOrderQuantityPercentList = new ArrayList<>();


    private static enum InitialDiState{

    }

    public static enum SellRemainderState{
        NOT_IN_SELL_STATE,
        SELL_50,
        SELL_70,
        SELL_85,
        SELL_100
    }

    public static enum CurrentDiState{
        CURRENT_PLUSDI_GREATER,
        CURRENT_MINUSDI_GREATER
    }

    public static enum BuyState{
        IN_BUY_STATE,
        IN_SELL_STATE
    }
    public static HashMap<String, Boolean> symbolBreakoutMap = new HashMap<>();
    public static HashMap<String, Balance> balanceHashMap = new HashMap<>();
    public static boolean isFirstScanComplete = false;
    public static boolean isMinusDiGreater = false;
    public static boolean isFirstLaunch = true;
    public static boolean canBuy = false;
    public static InitialDiState initialDiState;
    public static CurrentDiState currentDiState;
    public static SellRemainderState sellRemainderState = SellRemainderState.NOT_IN_SELL_STATE;
    public static BuyState buyState = BuyState.IN_BUY_STATE;
    public static Double startMoney = 100.0;
    public static Double startCoin = 0.0;
    public double maxDiDiff = 0.0;
    public static int limit  = 45;
    public static Long serverTime = 0L;
    public static String symbol = "TUSDUSDT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        rateTextView = (TextView)this.findViewById(R.id.RatesTextView);
//        reverseRateTextView = (TextView)this.findViewById(R.id.ReverseRatesTextView);
        breakoutTextView = (TextView)this.findViewById(R.id.BreakoutTextView);
        Log.d(TAG, "timestamp: " + localToGMT());
//        rateTextView.setMovementMethod(new ScrollingMovementMethod());
//        reverseRateTextView.setMovementMethod(new ScrollingMovementMethod());
        breakoutTextView.setMovementMethod(new ScrollingMovementMethod());
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
//        sendGetSymbolsRequest();

//        sendDefKlinesRequest(symbolsArray[symbolsIndex]);
        sendDefKlinesRequest(symbol);
        //sendCurrentPriceRequest();
        //resendKlinesRequest();
        //sendKlinesRequest(localToGMTOffset());
        //sendDefKlinesRequest(symbol);
//        sendKlinesRequestTimer();
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
//        AdxListener adxListener = new AdxListener() {
//            @Override
//            public void onSuccess(String displayString) {
//                breakoutTextViewString += displayString;
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        breakoutTextView.setText(breakoutTextViewString);
//                    }
//                });
//
//                symbolsIndex += 1;
//
////                if (symbolsIndex >= symbolsList.size()) {
////                    isFirstScanComplete = true;
////                    symbolsIndex = 0;
////                }
////
////                sendDefKlinesRequest(symbolsList.get(symbolsIndex));
//
//
//                if (symbolsIndex < symbolsList.size()){
//                    sendDefKlinesRequest(symbolsList.get(symbolsIndex));
//                }
//
//            }
//
//            @Override
//            public void onFailure(String response) {
//
//            }
//
//            @Override
//            public void onBuy() {
//                handleBuyOrder();
//            }
//
//            @Override
//            public void onSell() {
//                handleSellOrder();
//            }
//        };

        final BuyOrderListener buyOrderListener = new BuyOrderListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        final AccountInfoListener accountInfoListener = new AccountInfoListener() {
            @Override
            public void onSuccess(HashMap<String, Balance> balanceHashMap) {
                MainActivity.balanceHashMap = balanceHashMap;
                Balance usdtBalance = balanceHashMap.get("USDT");
                usdtCoin = usdtBalance.getFreeCoin();
                Log.d(TAG, "usdtCoin: " + usdtCoin);

                int placeOrderIndex = 0;
                buyOrderQuantityPercentList.clear();

                if ((usdtCoin * 0.1) >= 10.2){
                    Log.d(TAG, "(usdtCoin * 0.1) >= 10.2");
                    placeOrderIndex = 4;
                    buyOrderQuantityPercentList.add(0.3);
                    buyOrderQuantityPercentList.add(0.4);
                    buyOrderQuantityPercentList.add(0.2);
                    buyOrderQuantityPercentList.add(0.1);
                } else if ((usdtCoin * 0.22) >= 10.2){
                    Log.d(TAG, "(usdtCoin * 0.22) >= 10.2");
                    placeOrderIndex = 3;
                    buyOrderQuantityPercentList.add(0.335);
                    buyOrderQuantityPercentList.add(0.445);
                    buyOrderQuantityPercentList.add(0.22);
                } else if ((usdtCoin * 0.435) >= 10.2){
                    Log.d(TAG, "(usdtCoin * 0.435) >= 10.2");
                    placeOrderIndex = 2;
                    buyOrderQuantityPercentList.add(0.435);
                    buyOrderQuantityPercentList.add(0.565);
                } else if ((usdtCoin >= 10.2)){
                    Log.d(TAG, "usdtCoin >= 10.2");
                    placeOrderIndex = 1;
                    buyOrderQuantityPercentList.add(1.0);
                } else {
                    Log.d(TAG, "Not Enough usdt, Do Nothing");
                    placeOrderIndex = 0;
                    buyOrderQuantityPercentList.add(0.0);
                }

                for (int j = 0; j < placeOrderIndex; j++) {
                    String signature = "";
                    buyQuantity = 0.0;
                    buyQuantity = roundDouble(((BigDecimal.valueOf(usdtCoin).multiply(BigDecimal.valueOf(buyOrderQuantityPercentList.get(j)))).divide(BigDecimal.valueOf(buyOrderTierList.get(j)), 2, RoundingMode.HALF_DOWN)).doubleValue(), 1);
                    Log.d(TAG, "buyQuantity: " + buyQuantity);
                    String buyQueryString = getBuyQueryString(buyOrderTierList.get(j), buyQuantity);
                    String queryStrSignature = "";
                    try {
                        signature = orderManager.encode(secretKey, buyQueryString);
                        queryStrSignature = buyQueryString + "&signature=" + signature;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (buyPrice != 0.0 || queryStrSignature != "") {
                        orderManager.sendBuyOrderRequest(buyOrderListener, queryStrSignature);
                    } else {
                        Log.d(TAG, "buyOrder buyPrice != 0.0 || queryStrSignature != \"\"");
                    }
                }


                //buyQuantity = usdtCoin/buyPrice;


//                String signature = "";
//                String openOrderQueryString = getOpenOrderQueryString();
//                String queryStrSignature = "";
//                try {
//                    signature = orderManager.encode(secretKey, openOrderQueryString);
//                    queryStrSignature = openOrderQueryString + "&signature=" + signature;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                orderManager.sendCheckOpenOrderRequest(openOrderListener, queryStrSignature);
            }

            @Override
            public void onFailure(String failureMsg) {
                Log.d(TAG, "accountInfo onFailure" + failureMsg);
            }
        };

        final ServerTimeListener serverTimeListener2 = new ServerTimeListener() {
            @Override
            public void onSuccess(Long serverTime) {
                MainActivity.serverTime = serverTime;
                String signature = "";
                String accountInfoQueryString = "";
                String queryStrSignature = "";

                try {
                    accountInfoQueryString = getAccountInfoQueryString();
                } catch (CustomException e) {
                    e.printStackTrace();
                }
                try {
                    signature = orderManager.encode(secretKey, accountInfoQueryString);
                    queryStrSignature = accountInfoQueryString + "&signature=" + signature;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                orderManager.sendAccountInfoRequest(accountInfoListener, queryStrSignature);
            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        final CancelBuyOrderListener cancelBuyOrderListener = new CancelBuyOrderListener() {
            @Override
            public void onSuccess() {
                cancelBuyOrderOnSuccessCount += 1;
                if (cancelBuyOrderOnSuccessCount >= cancelBuyOrderIdListLength){
                    orderManager.sendServerTimeRequest(serverTimeListener2);
                }
            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        final OpenOrderListener openOrderListener = new OpenOrderListener() {
            @Override
            public void onSuccess(List<Long> cancelBuyOrderIdList) {
                cancelBuyOrderOnSuccessCount = 0;
                cancelBuyOrderIdListLength = cancelBuyOrderIdList.size();

                if (cancelBuyOrderIdListLength > 0) {
                    Log.d(TAG, "cancelBuyOrderIdListLength > 0");
                    for (Long orderId : cancelBuyOrderIdList) {
                        String signature = "";
                        String cancelBuyOrderQueryString = getCancelBuyOrderQueryString(orderId);
                        String queryStrSignature = "";
                        try {
                            signature = orderManager.encode(secretKey, cancelBuyOrderQueryString);
                            queryStrSignature = cancelBuyOrderQueryString + "&signature=" + signature;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        orderManager.sendCancelBuyOrderRequest(cancelBuyOrderListener, queryStrSignature);
                    }
                } else if (cancelBuyOrderIdListLength == 0){
                    Log.d(TAG, "cancelBuyOrderIdListLength == 0");
                    cancelBuyOrderListener.onSuccess();
                }
            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        final ServerTimeListener serverTimeListener1 = new ServerTimeListener() {
            @Override
            public void onSuccess(Long serverTime) {
                MainActivity.serverTime = serverTime;
                String signature = "";
                String openOrderQueryString = getOpenOrderQueryString();
                String queryStrSignature = "";
                try {
                    signature = orderManager.encode(secretKey, openOrderQueryString);
                    queryStrSignature = openOrderQueryString + "&signature=" + signature;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                orderManager.sendCheckOpenOrderRequest(openOrderListener, queryStrSignature);
            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        BollingerListener bollingerListener = new BollingerListener() {
            @Override
            public void onSuccess(double bollingerPrice, double buyPriceTier1, double buyPriceTier2, double buyPriceTier3, double buyPriceTier4) {
                buyOrderTierList.clear();
                buyPrice = bollingerPrice;
                Log.d(TAG, "buyPrice: " + buyPrice);
                buyOrderTier1 = buyPriceTier1;
                buyOrderTier2 = buyPriceTier2;
                buyOrderTier3 = buyPriceTier3;
                buyOrderTier4 = buyPriceTier4;
                buyOrderTierList.add(buyPriceTier1);
                buyOrderTierList.add(buyPriceTier2);
                buyOrderTierList.add(buyPriceTier3);
                buyOrderTierList.add(buyPriceTier4);
                orderManager.sendServerTimeRequest(serverTimeListener1);
            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        KlinesListener klinesListener = new KlinesListener() {
            @Override
            public void onSuccess(ArrayList<Double> closePriceArrayList) {
                closePriceList = closePriceArrayList;
            }

            @Override
            public void onFailure(String response) {

            }
        };

        klinesManager.sendDefaultKlinesRequest(klinesListener, symbol);
    }

    private String getCancelBuyOrderQueryString(Long orderId) {
        return "symbol=" + symbol + "&orderId=" + orderId + "&timestamp=" + serverTime;
    }

    private String getOpenOrderQueryString() {
        return "symbol=" + symbol + "&recvWindow=5000&timestamp=" + serverTime;
    }

    private String getAccountInfoQueryString() throws CustomException {
        if (serverTime != 0L) {
            return "timestamp=" + serverTime;
        } else {
            throw new CustomException("serverTime == 0L");
        }
    }

    private String getBuyQueryString(Double buyOrderPrice, double buyQuantity){
        if (buyPrice != 0.0){
//            return "symbol=" + symbol + "&side=BUY&type=LIMIT&timeInForce=GTC&quantity=" + buyQuantity + "&price=" + buyPrice + "&recvWindow=5000&timestamp=" + serverTime;
            return "symbol=" + symbol + "&side=BUY&type=LIMIT&timeInForce=GTC&quantity=" + buyQuantity + "&price=" + buyOrderPrice + "&recvWindow=5000&timestamp=" + serverTime;

        } else {
            Log.d(TAG, "getBuyQueryString buyPrice = 0.0");
            return "";
        }
    }

    private void sendKlinesRequestTimer() {
//        timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//
//            @Override
//            public void run() {
//                sendDefKlinesRequest("BTCUSDT");
//            }
//
//        }, 0, 3000);

        sendDefKlinesRequest("BTCUSDT");

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
                    case SELL_70:
                        startMoney = startMoney + (startCoin * 0.7 * price);
                        startCoin = startCoin * 0.3;
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
            public void onSuccess(ArrayList<Double> closePriceArrayList) {
                closePriceList = closePriceArrayList;
            }

            @Override
            public void onFailure(String response) {

            }

        };

        klinesManager.sendKlinesRequest(klinesListener, utcTimestampOffset);

    }

    private void sendGetSymbolsRequest() {
        final SymbolsListener symbolsListener = new SymbolsListener() {

            @Override
            public void onSuccess(List<String> symbols) {
                symbolsList = symbols;
                Log.d(TAG, "symbolList.Size(): " + symbolsList.size());


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

    public static double roundDouble(double x, int roundTodecimalPlace)
    {
        BigDecimal b = new BigDecimal(Double.toString(x));
        b = b.setScale(roundTodecimalPlace, BigDecimal.ROUND_HALF_DOWN);
        return b.doubleValue();
    }
}

class CustomException extends Exception
{
    public CustomException(String message)
    {
        super(message);
    }
}