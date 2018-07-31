package rain.com.rain;

import android.app.Notification;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationListener extends NotificationListenerService {
    public String TAG = "NotificationListener";
    private ArrayList<Double> closePriceList = new ArrayList<>();
    private KlinesManager klinesManager = new KlinesManager();
    private OrderManager orderManager = new OrderManager();
    private Double buyPrice = 0.0;
    Context context;

    public enum SellBuySignal{
        BUY_SIGNAL,
        SELL_SIGNAL
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction("rain.com.rain.notificationlistener");

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SellBuySignal sellBuySignal;
        String symbolKey = "";
        String symbol = "";
        double base;
        Log.d(TAG, "onNotificationPosted");
        String pack = sbn.getPackageName();
        String ticker ="";
        if(sbn.getNotification().tickerText !=null) {
            ticker = sbn.getNotification().tickerText.toString();
        }
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = "";
        if (extras.getCharSequence("android.text") != null)
            text = extras.getCharSequence("android.text").toString();
        int id1 = extras.getInt(Notification.EXTRA_SMALL_ICON);
        Bitmap id = sbn.getNotification().largeIcon;


        Log.i("Package",pack);
        Log.i("Ticker",ticker);
        Log.i("Title",title);
        Log.i("Text",text);

        if (ticker.contains("BUY")){
            sellBuySignal = SellBuySignal.BUY_SIGNAL;
            Log.d(TAG,"SellBuySignal.BUY_SIGNAL");
            symbolKey = "BUY/LONG";
            symbol = regexSymbolSearch(symbolKey, ticker);
            sendBuySellOrder(symbol);
        } else if (ticker.contains("SELL")){
            sellBuySignal = SellBuySignal.SELL_SIGNAL;
            Log.d(TAG,"SellBuySignal.SELL_SIGNAL");
            symbolKey = "SELL/SHORT";
            symbol = regexSymbolSearch(symbolKey, ticker);
            sendBuySellOrder(symbol);
        } else {
            Log.d(TAG, "Ticker Does not Contain Buy or Sell Signal");
        }

    }

    private String regexSymbolSearch(String symbolKey, String ticker){
        String symbol = "";
        String symbolPatternStr = "(?<=\\b" + symbolKey + "\\s)(\\w+)";
        Pattern symbolPattern = Pattern.compile(symbolPatternStr);
        Matcher symbolMatcher = symbolPattern.matcher(ticker);
        if (symbolMatcher.find()) {
            //String symbol2 = symbolMatcher.group(1);
            symbol = symbolMatcher.group(1);
            Log.i(TAG, "Regex Symbol: " + symbol);
        } else {
            Log.i(TAG, "Regex Symbol No Match");
        }

        return symbol;
    }

    private void sendBuySellOrder(String symbol){
        final ServerTimeListener serverTimeListener = new ServerTimeListener() {
            @Override
            public void onSuccess(Long serverTime) {

            }

            @Override
            public void onFailure(String failureMsg) {

            }
        };

        KlinesListener klinesListener = new KlinesListener() {
            @Override
            public void onSuccess(ArrayList<Double> closePriceArrayList) {
                closePriceList = closePriceArrayList;
                orderManager.sendServerTimeRequest(serverTimeListener);
            }

            @Override
            public void onFailure(String response) {

            }
        };

        klinesManager.sendDefaultKlinesRequest(klinesListener, symbol);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationRemoved");
    }
}
