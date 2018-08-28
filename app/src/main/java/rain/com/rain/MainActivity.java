package rain.com.rain;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    private String[] symbolsArray = {"BTCUSDT", "LTCUSDT", "BNBUSDT", "ETHUSDT", "BCCUSDT", "ADAUSDT", "QTUMUSDT", "NEOUSDT"};
    private int symbolsIndex = 0;
    private double buyPrice = 0.0;
    private double sellPrice = 0.0;
    private String secretKey = "Eqr4AvEAPufgZ8fD97ciJQjzNp7KMScUBAjqXDoc0IAaMTrUks3lTXGQVbuEEfzi";
    private double sellQuantity = 0.0;
    private double sellCoinQuantity = 0.0;
    private int cancelBuyOrderIdListLength = 0;
    private int cancelBuyOrderOnSuccessCount = 0;
    private int cancelSellOrderIdListLength = 0;
    private int cancelOrderIdListLength = 0;
    private int cancelSellOrderOnSuccessCount = 0;
    private int cancelBuySellOrderOnSuccessCount = 0;
    private double buyOrderTier1 = 0.0;
    private double buyOrderTier2 = 0.0;
    private double buyOrderTier3 = 0.0;
    private double buyOrderTier4 = 0.0;
    private double buyOrderTier5 = 0.0;
    private List<Double> buyOrderTierList = new ArrayList<>();
    private List<Double> buyOrderQuantityPercentList = new ArrayList<>();
    private NotificationManager notifManager;
    private double takeProfitPrice = 2.350;
    private double stopLossPrice = 0.0;
    private double currentPrice = 0.0;
    private int openOrders = 5;
    private int decimalPlace = 0;
    private boolean isStopLossActivated = false;
    private boolean isBuyOrderFilled = false;


    //Buy Order
    private double buyCoinQuantity = 0.0;
    private double buyQuantity = 0.0;



    private static enum InitialDiState{

    }

    public static enum BuySellState{
        NOT_IN_SELL_STATE,
        SELL_50,
        SELL_70,
        SELL_85,
        SELL_100
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
    public static List<String> symbolsList = new ArrayList<>();
    public static HashMap<String, Boolean> symbolBreakoutMap = new HashMap<>();
    public static HashMap<String, Balance> balanceHashMap = new HashMap<>();
    public static HashMap<String, Double> pumpHashMap = new HashMap<>();
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
    public static int limit  = 500;
    public static int decimalPlaces  = 0;
    public static Long serverTime = 0L;
    public static String symbol = "BTCUSDT";
    public static String sellCoin = "ONT";
    //public static String sellSymbolPair = "ONTUSDT";
    public static Long currentTS;
    public static Long midnightUTCTS;
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        rateTextView = (TextView)this.findViewById(R.id.RatesTextView);
//        reverseRateTextView = (TextView)this.findViewById(R.id.ReverseRatesTextView);
        breakoutTextView = (TextView)this.findViewById(R.id.BreakoutTextView);
        //Log.d(TAG, "timestamp: " + localToGMT());
//        rateTextView.setMovementMethod(new ScrollingMovementMethod());
//        reverseRateTextView.setMovementMethod(new ScrollingMovementMethod());
        breakoutTextView.setMovementMethod(new ScrollingMovementMethod());
        currentTS = timestamp.getTime();
        midnightUTCTS = findMidnightUTCTS();
        Log.d(TAG, "currentTS: " + currentTS);
        Log.d(TAG, "midnightUTCTS: " + midnightUTCTS);

//        double testD = 0.12340;
//        Log.d(TAG, "testD: " + roundDouble(testD, 4));

        new BuySellMonitorAsyncTask().execute();
//        sendEmail();
    }

    public long findMidnightUTCTS(){
        Calendar date = new GregorianCalendar();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        //        date.add(Calendar.DAY_OF_MONTH, 1);

        long timeParameter = date.getTimeInMillis();

        return timeParameter;
    }

    public static long localToGMT() {
        return new Date().getTime()/1000;
    }

    public static long localToGMTOffset() {
        return (new Date().getTime() - 8674560000L);
//        return (new Date().getTime() - 8760960000L);
    }

    protected void sendEmail() {
//        Intent i = new Intent(Intent.ACTION_SEND);
//        i.setType("message/rfc822");
//        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"2894893036@txt.freedommobile.ca"});
//        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
//        i.putExtra(Intent.EXTRA_TEXT   , "body of email");
//        try {
//            startActivity(Intent.createChooser(i, "Send mail..."));
//        } catch (android.content.ActivityNotFoundException ex) {
//            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "send email failed");
//        }
    }

    @Override
    public void onResume(){
        super.onResume();
//        sendGetSymbolsRequest();

//        sendDefKlinesRequest(symbolsArray[symbolsIndex]);
        //sendDefKlinesRequest(symbol);
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

    private void sendDefKlinesRequest(final String symbol) {

        final BuyOrderListener buyOrderListener = new BuyOrderListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Order Placed");

            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        final AccountInfoListener accountInfoListener = new AccountInfoListener() {
            @Override
            public void onSuccess(HashMap<String, Balance> balanceHashMap) {
                MainActivity.balanceHashMap = balanceHashMap;
                Balance sellSymbolBalance = balanceHashMap.get(sellCoin);
                sellCoinQuantity = 0;
                sellCoinQuantity = roundDouble(sellSymbolBalance.getFreeCoin(), 2);
                Log.d(TAG, "sellCoinQuantity: " + sellCoinQuantity);

                String signature = "";
                String sellQueryString = getSellQueryString(roundDouble(takeProfitPrice, decimalPlace), sellCoinQuantity);
                String queryStrSignature = "";
                try {
                    signature = orderManager.encode(secretKey, sellQueryString);
                    queryStrSignature = sellQueryString + "&signature=" + signature;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (takeProfitPrice != 0.0 || queryStrSignature != "") {
                    Log.d(TAG, "TakeProfitOrderPlaced");
                    orderManager.sendBuyOrderRequest(buyOrderListener, queryStrSignature);
                } else {
                    Log.d(TAG, "takeProfitPrice = 0.0 || queryStrSignature = \"\"");
                }

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

        final ServerTimeListener serverTimeListener3 = new ServerTimeListener() {
            @Override
            public void onSuccess(Long serverTime) {
                MainActivity.serverTime = serverTime;
                String signature = "";
                String stopLossQueryString = "";
                String queryStrSignature = "";

                if (isBuyOrderFilled) {
                    try {
                        sellCoinQuantity = 0;
                        Balance sellSymbolBalance = balanceHashMap.get(sellCoin);
                        sellCoinQuantity = roundDouble(sellSymbolBalance.getFreeCoin(), 2);
                        Log.d(TAG, "sellCoinQuantity: " + sellCoinQuantity);
                        stopLossQueryString = getStopLostQueryString(sellCoinQuantity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        signature = orderManager.encode(secretKey, stopLossQueryString);
                        queryStrSignature = stopLossQueryString + "&signature=" + signature;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "StopLoss Sell Market Order Placed");
                    orderManager.sendBuyOrderRequest(buyOrderListener, queryStrSignature);
                }
            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        final CancelBuyOrderListener cancelBuyOrderListener = new CancelBuyOrderListener() {
            @Override
            public void onSuccess() {
                cancelBuyOrderOnSuccessCount += 1;
                if (cancelBuySellOrderOnSuccessCount >= cancelOrderIdListLength){
                    orderManager.sendServerTimeRequest(serverTimeListener3);
                }
            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        final OpenOrderListener openOrderListener = new OpenOrderListener() {
            @Override
            public void onSuccess(List<Long> cancelOrderIdList) {
                cancelBuySellOrderOnSuccessCount = 0;
                cancelOrderIdListLength = cancelOrderIdList.size();

                if (cancelBuyOrderIdListLength < openOrders && currentPrice != 0.0 && openOrders != 0 && !isStopLossActivated) {
                    isBuyOrderFilled = true;
                    Log.d(TAG, "cancelBuyOrderIdListLength: " + cancelBuyOrderIdListLength);
                    openOrders = cancelBuyOrderIdListLength;
                    orderManager.sendServerTimeRequest(serverTimeListener2);
                    Log.d(TAG, "openOrders: " + openOrders);

                } else if (currentPrice <= stopLossPrice && !isStopLossActivated){
                    isStopLossActivated = true;
                    for (Long orderId : cancelOrderIdList) {
                        String signature = "";
                        String cancelBuyOrderQueryString = getCancelBuyOrderQueryString(orderId);
                        String queryStrSignature = "";
                        Log.d(TAG, "cancel orderId: " + orderId);

                        try {
                            signature = orderManager.encode(secretKey, cancelBuyOrderQueryString);
                            queryStrSignature = cancelBuyOrderQueryString + "&signature=" + signature;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        orderManager.sendCancelBuyOrderRequest(cancelBuyOrderListener, queryStrSignature);
                    }
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
//                Log.d(TAG, "gmtTime: " + localToGMT());
//                Log.d(TAG, "gmtTimeOffset: " + localToGMTOffset());
//                long gmtTime = localToGMT() * 1000L;
//                long gmtTimeOffset = localToGMTOffset() * 1000L;
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

//        PriceCalculationListener priceCalculationListener = new PriceCalculationListener() {
//            @Override
//            public void onSuccess(double closePrice, int maxDecrement) {
//                currentPrice = closePrice;
//                decimalPlace = maxDecrement;
//                orderManager.sendServerTimeRequest(serverTimeListener1);
//
//            }
//
//            @Override
//            public void onFailure(String failureMsg) {
//
//            }
//        };
//
//        klinesManager.sendDefaultKlinesRequest(priceCalculationListener, symbol);

    }

    private void placeBuyOrderRequest(String symbol) {
        final BuyOrderListener buyOrderListener = new BuyOrderListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Order Placed");

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
                String buyQueryString = getBuyQueryString(buyPrice, buyCoinQuantity);
                String queryStrSignature = "";
                try {
                    signature = orderManager.encode(secretKey, buyQueryString);
                    queryStrSignature = buyQueryString + "&signature=" + signature;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (buyPrice != 0.0 || buyCoinQuantity != 0.0 || queryStrSignature != "") {
                    Log.d(TAG, "TakeProfitOrderPlaced");
                    orderManager.sendBuyOrderRequest(buyOrderListener, queryStrSignature);
                } else {
                    Log.d(TAG, "takeProfitPrice = 0.0 || queryStrSignature = \"\"");
                }
            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        orderManager.sendServerTimeRequest(serverTimeListener1);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotification(String aMessage) {
        final int NOTIFY_ID = 1002;

        // There are hardcoding only for show it's just strings
        String name = "my_package_channel";
        String id = "my_package_channel_1"; // The user-visible name of the channel.
        String description = "my_package_first_channel"; // The user-visible description of the channel.

        Intent intent;
        PendingIntent pendingIntent;
        Notification.Builder builder;

        if (notifManager == null) {
            notifManager =
                    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new Notification.Builder(getApplicationContext(), id);

            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            builder.setContentTitle(aMessage)  // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(this.getString(R.string.app_name))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        } else {

            builder = new Notification.Builder(this, id);

            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            builder.setContentTitle(aMessage)                           // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(this.getString(R.string.app_name))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        } // else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);
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

    private String getStopLostQueryString(double sellQuantity) {
        return "symbol=" + symbol + "&side=SELL&type=MARKET&timeInForce=GTC&quantity=" + sellQuantity + "&recvWindow=5000&timestamp=" + serverTime;
    }

    private String getBuyQueryString(Double buyOrderPrice, double buyQuantity){
//        if (buyPrice != 0.0){
//            return "symbol=" + symbol + "&side=BUY&type=LIMIT&timeInForce=GTC&quantity=" + buyQuantity + "&price=" + buyPrice + "&recvWindow=5000&timestamp=" + serverTime;
            return "symbol=" + symbol + "&side=BUY&type=LIMIT&timeInForce=GTC&quantity=" + buyQuantity + "&price=" + buyOrderPrice + "&recvWindow=5000&timestamp=" + serverTime;

//        } else {
//            Log.d(TAG, "getBuyQueryString buyPrice = 0.0");
//            return "";
//        }
    }

    private String getSellQueryString(Double sellOrderPrice, double sellQuantity){
//        if (buyPrice != 0.0){
//            return "symbol=" + symbol + "&side=BUY&type=LIMIT&timeInForce=GTC&quantity=" + buyQuantity + "&price=" + buyPrice + "&recvWindow=5000&timestamp=" + serverTime;
            return "symbol=" + symbol + "&side=SELL&type=LIMIT&timeInForce=GTC&quantity=" + sellQuantity + "&price=" + sellOrderPrice + "&recvWindow=5000&timestamp=" + serverTime;

//        } else {
//            Log.d(TAG, "getBuyQueryString buyPrice = 0.0");
//            return "";
//        }
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
                Log.d(TAG, "symbolList.Size(): " + symbolsList.size());

                sendDefKlinesRequest(symbolsList.get(0));
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
        b = b.setScale(roundTodecimalPlace, BigDecimal.ROUND_DOWN);
        return b.doubleValue();
    }

//    private void checkBuyOrderFilled(){
//        final AccountInfoListener accountInfoListener = new AccountInfoListener() {
//            @Override
//            public void onSuccess(HashMap<String, Balance> balanceHashMap) {
//
//            }
//
//            @Override
//            public void onFailure(String failureMsg) {
//
//            }
//        };
//
//        final ServerTimeListener serverTimeListener = new ServerTimeListener() {
//            @Override
//            public void onSuccess(Long serverTime) {
//                MainActivity.serverTime = serverTime;
//                String signature = "";
//                String accountInfoQueryString = "";
//                String queryStrSignature = "";
//
//                try {
//                    accountInfoQueryString = getAccountInfoQueryString();
//                } catch (CustomException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    signature = orderManager.encode(secretKey, accountInfoQueryString);
//                    queryStrSignature = accountInfoQueryString + "&signature=" + signature;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                orderManager.sendAccountInfoRequest(accountInfoListener, queryStrSignature);
//            }
//
//            @Override
//            public void onFailure(String failureMsg) {
//
//            }
//        };
//
//        orderManager.sendServerTimeRequest(serverTimeListener);
//    }

    private class BuySellMonitorAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
//            placeBuyOrderRequest(symbol);
//            sendDefKlinesRequest(symbol);
            calculateVolumeProfile(symbol);
            //sendGetSymbolsRequest();
            return null;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private void calculateVolumeProfile(final String symbol) {
        final VolProfileListener volProfileListener = new VolProfileListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        final HistTradeListener histTradeListener = new HistTradeListener() {
            @Override
            public void onSuccess(ArrayList<Long> timestampArrayList, ArrayList<Double> volumeArrayList, ArrayList<Double> priceArrayList, ArrayList<Long> idArrayList) {
                Log.d(TAG, "histTradeListener onSuccess");

            }

            @Override
            public void onResendNextRequest(Long requestId) {
                Log.d(TAG, "histTradeListener onResendNextRequest");
                klinesManager.sendHistoricalTradeRequest(this, symbol, requestId);
            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        PriceCalculationListener priceCalculationListener = new PriceCalculationListener() {
            @Override
            public void onSuccess(double maxPrice, double minPrice) {
                klinesManager.sendHistoricalTradeRequest(histTradeListener, symbol, 0L);
            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };


        klinesManager.sendDefaultKlinesRequest(priceCalculationListener, symbol);
    }



//    private class SellOrderCheckerAsyncTask extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            checkBuyOrderFilled();
//            return null;
//        }
//
//        @Override
//        protected void onPreExecute() {}
//
//        @Override
//        protected void onProgressUpdate(Void... values) {}
//    }
}

class CustomException extends Exception
{
    public CustomException(String message)
    {
        super(message);
    }
}