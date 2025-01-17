package rain.com.rain;

import android.util.Log;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;

import static rain.com.rain.MainActivity.buyState;
import static rain.com.rain.MainActivity.currentDiState;
import static rain.com.rain.MainActivity.initialDiState;
import static rain.com.rain.MainActivity.isFirstLaunch;
import static rain.com.rain.MainActivity.isMinusDiGreater;
import static rain.com.rain.MainActivity.maxDiDiff;
import static rain.com.rain.MainActivity.sellRemainderState;

public class AdxDmModel {
    private String TAG = "AdxDmModel ";


    public void calculateAdxDi(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, AdxListener adxListener, String symbol) {
        double[] adxOutArray = new double[closePrice.length];
        double[] minusDiOutArray = new double[closePrice.length];
        double[] plusDiOutArray = new double[closePrice.length];
        ArrayList<Double> adxOutArrayList = new ArrayList<>();
        ArrayList<Double> minusDiOutArrayList = new ArrayList<>();
        ArrayList<Double> plusDiOutArrayList = new ArrayList<>();

        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        Core c = new Core();
        Log.d(TAG, "symbol" + symbol);
        RetCode adxRetCode = c.adx(0, closePrice.length - 1, highPrice, lowPrice, closePrice, 14, begin, length, adxOutArray);
        RetCode minusDIRetCode = c.minusDI(0, closePrice.length - 1, highPrice, lowPrice, closePrice, 14, begin, length, minusDiOutArray);
        RetCode plusDIRetCode = c.plusDI(0, closePrice.length - 1, highPrice, lowPrice, closePrice, 14, begin, length, plusDiOutArray);
        adxOutArrayList = removeZeroInArray(adxOutArray);
        minusDiOutArrayList = removeZeroInArray(minusDiOutArray);
        plusDiOutArrayList = removeZeroInArray(plusDiOutArray);

        if (minusDIRetCode == RetCode.Success && plusDIRetCode == RetCode.Success && adxRetCode == RetCode.Success){
            Double plusDi = plusDiOutArrayList.get(plusDiOutArrayList.size() - 1);
            Double minusDi = minusDiOutArrayList.get(plusDiOutArrayList.size() - 1);
            Log.d(TAG, "plusDi: " + plusDi);
            Log.d(TAG, "minusDi: " + minusDi);

            double currentDiDiff = 0.0;
            double diDiffPercent = 0.0;
            if (buyState != MainActivity.BuyState.IN_BUY_STATE) {
                currentDiDiff = plusDi - minusDi;
                Log.d(TAG, "currentDiDiff: " + currentDiDiff);

                if (currentDiDiff > maxDiDiff) {
                    maxDiDiff = currentDiDiff;
                }

                Log.d(TAG, "maxDiDiff: " + maxDiDiff);
                diDiffPercent = currentDiDiff / maxDiDiff;
                Log.d(TAG, "diDiffPercent: " + diDiffPercent);
            }

            if(!isMinusDiGreater){
                if(minusDi > plusDi){
                    Log.d(TAG, "MinusDiGreater Prereq State True");
                    isMinusDiGreater = true;
                } else {
                    Log.d(TAG, "PlusDiGreater Prereq State False");
                }
            } else {
                if(buyState == MainActivity.BuyState.IN_BUY_STATE && (plusDi >= minusDi)){
                    buyState = MainActivity.BuyState.IN_SELL_STATE;
                    //TODO: SHOULD NOW BUY
                    Log.d(TAG, "Now Buy");
                    adxListener.onBuy();

//                } else if (buyState == MainActivity.BuyState.IN_SELL_STATE && (minusDi > plusDi)){
//                    buyState = MainActivity.BuyState.IN_BUY_STATE;
//                    //TODO: SHOULD NOW SELL
//                    Log.d(TAG, "Now Sell");
//                    adxListener.onSell();

                } else if (buyState == MainActivity.BuyState.IN_SELL_STATE && (diDiffPercent <= 0.6 && diDiffPercent > 0.5)){
                    //TODO: SELL 50%
                    Log.d(TAG, "Now Selling 50%");
                    buyState = MainActivity.BuyState.IN_POST_SELL_50_STATE;
                    sellRemainderState = MainActivity.SellRemainderState.SELL_50;
                    adxListener.onSell();
//                } else if (buyState == MainActivity.BuyState.IN_POST_SELL_50_STATE && (diDiffPercent <= 0.4 && diDiffPercent > 0.3)){
//                    //TODO: SELL REMAINING 75%
//                    Log.d(TAG, "Now Selling 75%");
//                    buyState = MainActivity.BuyState.IN_POST_SELL_75_STATE;
//                    sellRemainderState = MainActivity.SellRemainderState.SELL_75;
//                    adxListener.onSell();
//                } else if (buyState == MainActivity.BuyState.IN_POST_SELL_75_STATE && (diDiffPercent <= 0.3 && diDiffPercent > 0.2)){
//                    //TODO: SELL REMAINING 85%
//                    Log.d(TAG, "Now Selling 85%");
//                    buyState = MainActivity.BuyState.IN_POST_SELL_85_STATE;
//                    sellRemainderState = MainActivity.SellRemainderState.SELL_85;
//                    adxListener.onSell();
                } else if ((buyState == MainActivity.BuyState.IN_POST_SELL_50_STATE || buyState == MainActivity.BuyState.IN_SELL_STATE) && (diDiffPercent <= 0.5)){
                    buyState = MainActivity.BuyState.IN_BUY_STATE;
                    maxDiDiff = 0.0;
                    //TODO: SELL REMAINING 100%
                    Log.d(TAG, "Now Selling 100%");
                    isMinusDiGreater = false;
                    sellRemainderState = MainActivity.SellRemainderState.SELL_100;
                    adxListener.onSell();
                } else {
                    Log.d(TAG, "Not Buying or Selling");
                }
            }

//            if (isFirstLaunch){
//                isFirstLaunch = true;
//
//                if(plusDi >= minusDi){
//                    initialDiState = MainActivity.InitialDiState.LAUNCH_PLUSDI_GREATER;
//                } else {
//                    initialDiState = MainActivity.InitialDiState.LAUNCH_MINUSDI_GREATER;
//                }
//            }
//
//            if (!isFirstLaunch){
//                if (plusDi >= minusDi){
//                    currentDiState = MainActivity.CurrentDiState.CURRENT_PLUSDI_GREATER;
//                } else {
//                    currentDiState = MainActivity.CurrentDiState.CURRENT_MINUSDI_GREATER;
//                }
//            }
//
//            if (initialDiState == MainActivity.InitialDiState.LAUNCH_PLUSDI_GREATER && currentDiState == MainActivity.CurrentDiState.CURRENT_MINUSDI_GREATER){
//
//            }
//            for (int i = 0; i < minusDiOutArray.length - 1; i++) {
//                Log.d(TAG, "adxOutArray: " + adxOutArray[i]);
//                Log.d(TAG, "minusDmOutArray: " + minusDiOutArray[i]);
//                Log.d(TAG, "plusDmOutArray: " + plusDiOutArray[i]);
//            }
//            for(Double e : adxOutArrayList){
//                Log.d(TAG, "adxOutArrayList: " + e);
//            }
//
//            for(Double f : minusDiOutArrayList){
//                Log.d(TAG, "minusDiOutArrayList: " + f);
//            }
//
//            for(Double g : plusDiOutArrayList){
//                Log.d(TAG, "plusDiOutArrayList: " + g);
//            }
        }
        else {
            Log.d(TAG, "Error");
        }
    }

    private ArrayList<Double> removeZeroInArray(double[] doubleArray){
        ArrayList<Double> doubleArrayList = new ArrayList<>();

        for(double d : doubleArray){
            if (Double.compare(d, 0.0) != 0){
                doubleArrayList.add(d);
            }
        }

        return doubleArrayList;
    }
}
