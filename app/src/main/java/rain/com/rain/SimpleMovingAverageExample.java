package rain.com.rain;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SimpleMovingAverageExample {

    /**
     * The total number of periods to generate data for.
     */
    public static final int TOTAL_PERIODS = 100;

    /**
     * The number of periods to average together.
     */
    public static final int PERIODS_AVERAGE = 0;

    private String TAG = "SimpleMovingAverage ";
    public static String outputTextUptrend = "";
    public static String outputTextDowntrend = "";
    private List<Integer>  sevenBeginBelowAllIndexList = new ArrayList<>();
    private String[] symbolsArray = {"BTCUSDT", "LTCUSDT", "BNBUSDT", "ETHUSDT", "BCCUSDT", "ADAUSDT", "QTUMUSDT", "NEOUSDT"};
    private HashMap<String, String> symbolTrendStateMap = new HashMap<String, String>();

    private enum SmaBeginState{
        BEGIN_STATE_SEVEN_BELOW_ALL,
        BEGIN_STATE_SEVEN_BELOW_TWENTYFIVE,
        BEGIN_STATE_SEVEN_ABOVE_ALL,
        BEGIN_STATE_DEFAULT
    }

    private enum SmaEndState{
        END_STATE_SEVEN_ABOVE_ALL,
        END_STATE_SEVEN_BELOW_TWENTYFIVE,
        END_STATE_SEVEN_BELOW_ANY,
        END_STATE_DEFAULT
    }

    public SimpleMovingAverageExample(){
        for(String symbol : symbolsArray){
            symbolTrendStateMap.put(symbol, "Default");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void calculateSimpleMovingAverage(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, KlinesListener klinesListener, String symbol) {
        //double[] closePrice = new double[TOTAL_PERIODS];
        double[] sevenOutArray = new double[closePrice.length];
        double[] twentyFiveOutArray = new double[closePrice.length];
        double[] hundredOutArray = new double[closePrice.length];
        double[] adxOutArray = new double[closePrice.length];
        double[] minusDmOutArray = new double[closePrice.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        SmaBeginState smaBeginState = SmaBeginState.BEGIN_STATE_DEFAULT;
        SmaEndState smaEndState = SmaEndState.END_STATE_DEFAULT;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String curTime = sdf.format(cal.getTime());


        Core c = new Core();
        Log.d(TAG, "symbol" + symbol);
        //RetCode retCode = c.adx(0, closePrice.length - 1, highPrice, lowPrice, closePrice, 14, begin, length, adxOutArray);
        RetCode retCode = c.minusDI(0, 50, highPrice, lowPrice, closePrice, 14, begin, length, minusDmOutArray);
        if (retCode == RetCode.Success){
            for (int i = 0; i < 4; i++) {
                //Log.d(TAG, "adxOutArray: " + adxOutArray[i]);
                Log.d(TAG, "minusDmOutArray: " + minusDmOutArray[i]);
            }
//        RetCode sevenRetCode = c.sma(0, closePrice.length -1, closePrice, 5, begin, length, sevenOutArray);
//        RetCode twentyFiveRetCode = c.sma(0, closePrice.length -1, closePrice, 8, begin, length, twentyFiveOutArray);
//        RetCode hundredRetCode = c.sma(0, closePrice.length -1, closePrice, 13, begin, length, hundredOutArray);
//
//        if (sevenRetCode == RetCode.Success && twentyFiveRetCode == RetCode.Success && hundredRetCode == RetCode.Success) {
////            Log.d(TAG, "Output Start Period: " + begin.value);
////            Log.d(TAG, "Output End Period: " + (begin.value + length.value - 1));
//
////            for (int i = begin.value; i < begin.value + length.value; i++) {
////                StringBuilder line = new StringBuilder();
////                line.append("Period #");
////                line.append(i);
////                line.append(" close=");
////                line.append(closePrice[i]);
////                line.append(" mov_avg=");
////                line.append(out[i - begin.value]);
////                Log.d(TAG, line.toString());
////            }
//
////            for (int i = 0; i < 4; i++){
////                Log.d(TAG, "sevenOutArray: " + sevenOutArray[i]);
////                Log.d(TAG, "twentyFiveOutArray: " + twentyFiveOutArray[i]);
////                Log.d(TAG, "hundredOutArray: " + hundredOutArray[i]);
////
////            }
//
//
//            //used to find if current MA is upward cross
////            for (int i = 48; i >= 0; i--){
////                if (sevenOutArray[i] < twentyFiveOutArray[i] && sevenOutArray[i] < hundredOutArray[i]){
////                    smaBeginState = SmaBeginState.BEGIN_STATE_SEVEN_BELOW_ALL;
////                    sevenBeginBelowAllIndexList.add(i);
////                } else if (sevenOutArray[i] < twentyFiveOutArray[i] && sevenOutArray[i] > hundredOutArray[i]){
////                    smaBeginState = SmaBeginState.BEGIN_STATE_SEVEN_BELOW_TWENTYFIVE;
////                }
////            }
////
////            if (!sevenBeginBelowAllIndexList.isEmpty()){
////                int scanFromIndex = Collections.min(sevenBeginBelowAllIndexList);
////
////                for (int j = scanFromIndex; j >= 0; j--){
////                    if (sevenOutArray[j] > twentyFiveOutArray[j] && sevenOutArray[j] > hundredOutArray[j]) {
////                        smaEndState = SmaEndState.END_STATE_SEVEN_ABOVE_ALL;
////                    } else {
////                        smaEndState = SmaEndState.END_STATE_DEFAULT;
////                    }
////                }
////
////            }
//
//            if (sevenOutArray[2] < twentyFiveOutArray[2] && sevenOutArray[2] < hundredOutArray[2]) {
//                smaBeginState = SmaBeginState.BEGIN_STATE_SEVEN_BELOW_ALL;
//            } else if (sevenOutArray[2] > twentyFiveOutArray[2] && sevenOutArray[2] > hundredOutArray[2]) {
//                smaBeginState = SmaBeginState.BEGIN_STATE_SEVEN_ABOVE_ALL;
//            } else if (sevenOutArray[2] < twentyFiveOutArray[2] && sevenOutArray[2] > hundredOutArray[2]) {
//                smaBeginState = SmaBeginState.BEGIN_STATE_SEVEN_BELOW_TWENTYFIVE;
//            }
//
//            if (sevenOutArray[0] > twentyFiveOutArray[0] && sevenOutArray[0] > hundredOutArray[0]) {
//                smaEndState = SmaEndState.END_STATE_SEVEN_ABOVE_ALL;
//            } else if (sevenOutArray[0] < twentyFiveOutArray[0] || sevenOutArray[0] < hundredOutArray[0]){
//                smaEndState = SmaEndState.END_STATE_SEVEN_BELOW_ANY;
//            }
//
//            if (smaBeginState == SmaBeginState.BEGIN_STATE_SEVEN_BELOW_ALL && smaEndState == SmaEndState.END_STATE_SEVEN_ABOVE_ALL
//                    && !(symbolTrendStateMap.get(symbol).equalsIgnoreCase("Uptrend"))){
//                symbolTrendStateMap.put(symbol, "Uptrend");
//                Log.d(TAG, symbol + " Uptrend Cross");
//                outputTextUptrend = curTime + " " + symbol + " Uptrend Cross \n" + outputTextUptrend;
//            } else if (smaBeginState == SmaBeginState.BEGIN_STATE_SEVEN_ABOVE_ALL && smaEndState == SmaEndState.END_STATE_SEVEN_BELOW_ANY
//                    && !(symbolTrendStateMap.get(symbol).equalsIgnoreCase("Downtrend"))){
//                symbolTrendStateMap.put(symbol, "Downtrend");
//                Log.d(TAG, symbol + "Downtrend Cross");
//                outputTextDowntrend = curTime + " " + symbol + " Downtrend Cross \n" + outputTextDowntrend;
//            } else if (smaBeginState == SmaBeginState.BEGIN_STATE_SEVEN_BELOW_TWENTYFIVE && smaEndState == SmaEndState.END_STATE_SEVEN_ABOVE_ALL
//                    && !(symbolTrendStateMap.get(symbol).equalsIgnoreCase("MidupTrend"))){
//                symbolTrendStateMap.put(symbol, "MidupTrend");
//                Log.d(TAG, symbol + "MidupTrend Cross");
//                outputTextUptrend = curTime + " " + symbol + " MidupTrend Cross \n" + outputTextUptrend;
//            } else if (smaBeginState == SmaBeginState.BEGIN_STATE_SEVEN_ABOVE_ALL && smaEndState == SmaEndState.END_STATE_SEVEN_ABOVE_ALL
//                    && !(symbolTrendStateMap.get(symbol).equalsIgnoreCase("CurrentUptrend"))) {
//                symbolTrendStateMap.put(symbol, "CurrentUptrend");
//                Log.d(TAG, symbol + " CurrentUptrend");
//                outputTextUptrend = curTime + " " + symbol + " CurrentUptrend \n" + outputTextUptrend;
//            } else if (smaBeginState == SmaBeginState.BEGIN_STATE_SEVEN_BELOW_ALL && smaEndState == SmaEndState.END_STATE_SEVEN_BELOW_ANY
//                    && !(symbolTrendStateMap.get(symbol).equalsIgnoreCase("CurrentDowntrend"))) {
//                symbolTrendStateMap.put(symbol, "CurrentDowntrend");
//                Log.d(TAG, symbol + " CurrentDowntrend");
//                outputTextDowntrend = curTime + " " + symbol + " CurrentDowntrend \n" + outputTextDowntrend;
//            }

            //klinesListener.onSuccess();
        }
        else {
            Log.d(TAG, "Error");
        }
    }
}