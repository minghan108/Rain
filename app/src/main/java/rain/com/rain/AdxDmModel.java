package rain.com.rain;

import android.util.Log;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static rain.com.rain.MainActivity.buyState;
import static rain.com.rain.MainActivity.currentDiState;
import static rain.com.rain.MainActivity.initialDiState;
import static rain.com.rain.MainActivity.isFirstLaunch;
import static rain.com.rain.MainActivity.isFirstScanComplete;
import static rain.com.rain.MainActivity.isMinusDiGreater;
import static rain.com.rain.MainActivity.sellRemainderState;
import static rain.com.rain.MainActivity.symbolBreakoutMap;

public class AdxDmModel {
    private String TAG = "AdxDmModel ";
    private boolean isFirstLaunch = true;


    public void calculateSma(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, AdxListener adxListener, String symbol){
        double[] smaOutArray = new double[volume.length];
        ArrayList<Double> smaOutArrayList = new ArrayList<>();
        boolean up = false;
        boolean down = false;
        double resistanceDouble = 0.0;
        double supportDouble = 0.0;
        boolean isBreakoutOld = symbolBreakoutMap.get(symbol);
        boolean isBreakoutNew = false;
        String displayString = "";

        Core c = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        RetCode smaRetCode = c.sma(0, volume.length - 1, volume, 6, begin, length, smaOutArray);
        smaOutArrayList = removeZeroInArray(smaOutArray);
        Log.d(TAG, "calculateSma");

        if (smaRetCode == RetCode.Success) {
            try {
                if (isFirstLaunch) {
//                    isFirstLaunch = false;
                    final int SUB_ARRAY_WINDOW = 5;

                    for (int i = 99; i > 5; i--) {
                        double[] subHighPrice = Arrays.copyOfRange(highPrice, highPrice.length - i, highPrice.length - i + SUB_ARRAY_WINDOW);
                        double[] subLowPrice = Arrays.copyOfRange(lowPrice, lowPrice.length - i, lowPrice.length - i + SUB_ARRAY_WINDOW);
                        //double[] subSmaVolume = Arrays.copyOfRange(smaOutArrayList, smaOutArrayList.size() - i, smaOutArrayList.size() - i + SUB_ARRAY_WINDOW);
                        List<Double> subSmaVolume = smaOutArrayList.subList(smaOutArrayList.size() - i, smaOutArrayList.size() - i + SUB_ARRAY_WINDOW);
                        //Log.d(TAG, "subSmaVolume.Size(): " + subSmaVolume.size());
                        double[] subVolume = Arrays.copyOfRange(volume, volume.length - i, volume.length - i + SUB_ARRAY_WINDOW);

                        //Use to compare currentPrice to resistanceDouble
                        double subClosePrice = closePrice[closePrice.length - i + SUB_ARRAY_WINDOW];

                        //Use when Testing When Breakout Occurs
                        double sHighPrice = highPrice[highPrice.length - i + SUB_ARRAY_WINDOW];

                        up = (subHighPrice[subHighPrice.length - 3] > subHighPrice[subHighPrice.length - 4]) && (subHighPrice[subHighPrice.length - 4] > subHighPrice[subHighPrice.length - 5]) &&
                                (subHighPrice[subHighPrice.length - 2] < subHighPrice[subHighPrice.length - 3]) && (subHighPrice[subHighPrice.length - 1] < subHighPrice[subHighPrice.length - 2]) &&
                                (subVolume[subVolume.length - 3] > subSmaVolume.get(subSmaVolume.size() - 3));

                        down = (subLowPrice[subLowPrice.length - 3] < subLowPrice[subLowPrice.length - 4]) && (subLowPrice[subLowPrice.length - 4] < subLowPrice[subLowPrice.length - 5]) &&
                                (subLowPrice[subLowPrice.length - 2] > subLowPrice[subLowPrice.length - 3]) && (subLowPrice[subLowPrice.length - 1] > subLowPrice[subLowPrice.length - 2]) &&
                                (subVolume[subVolume.length - 3] > subSmaVolume.get(subSmaVolume.size() - 3));

                        resistanceDouble = up ? subHighPrice[subHighPrice.length - 3] : resistanceDouble;
                        supportDouble = down ? subLowPrice[subLowPrice.length - 3] : supportDouble;

                        //if (sHighPrice > resistanceDouble && resistanceDouble != 0.0){
//                        if (subClosePrice > resistanceDouble && resistanceDouble != 0.0){
//                            //TODO: SEND NOTIFICATION THAT BREAKOUT OCCURRED
//                            Log.d(TAG, "BREAK ABOVE RESISTANCE");
//                            Log.d(TAG, "symbol: " + symbol);
//                        }
//                        //Log.d(TAG, "sHighPrice: " + sHighPrice);
//                        Log.d(TAG, "subClosePrice: " + subClosePrice);
//                        Log.d(TAG, "resistanceDouble: " + resistanceDouble);
//                        Log.d(TAG, "supportDouble: " + supportDouble);
                    }

                    if (closePrice[closePrice.length - 1] > resistanceDouble && resistanceDouble != 0.0){
                        isBreakoutNew = true;
//                        Log.d(TAG, "BREAK ABOVE RESISTANCE");
//                        Log.d(TAG, "price: " + closePrice[closePrice.length - 1]);
//                        Log.d(TAG, "resistanceDouble: " + resistanceDouble);
                        //Log.d(TAG, "supportDouble: " + supportDouble);
                    }
                    Log.d(TAG, "symbol: " + symbol);

                } else {
                    up = (highPrice[highPrice.length - 3] > highPrice[highPrice.length - 4]) && (highPrice[highPrice.length - 4] > highPrice[highPrice.length - 5]) &&
                            (highPrice[highPrice.length - 2] < highPrice[highPrice.length - 3]) && (highPrice[highPrice.length - 1] < highPrice[highPrice.length - 2]) &&
                            (volume[volume.length - 3] > smaOutArrayList.get(smaOutArrayList.size() - 3));

                    down = (lowPrice[lowPrice.length - 3] < lowPrice[lowPrice.length - 4]) && (lowPrice[lowPrice.length - 4] < lowPrice[lowPrice.length - 5]) &&
                            (lowPrice[lowPrice.length - 2] > lowPrice[lowPrice.length - 3]) && (lowPrice[lowPrice.length - 1] > lowPrice[lowPrice.length - 2]) &&
                            (volume[volume.length - 3] < smaOutArrayList.get(smaOutArrayList.size() - 3));

                    resistanceDouble = up ? highPrice[highPrice.length - 3] : resistanceDouble;
                    supportDouble = down ? lowPrice[lowPrice.length - 3] : supportDouble;

                    Log.d(TAG, "resistanceDouble: " + resistanceDouble);
                    Log.d(TAG, "supportDouble: " + supportDouble);
                }

                if (isFirstScanComplete){
                    if (isBreakoutOld == false && isBreakoutNew == true){
                        symbolBreakoutMap.put(symbol, isBreakoutNew);
                        //TODO: NOTIFY NEW BREAKOUT OCCURRED
                        Log.d(TAG, "BREAK ABOVE RESISTANCE");
                        Log.d(TAG, "price: " + closePrice[closePrice.length - 1]);
                        Log.d(TAG, "resistanceDouble: " + resistanceDouble);
                        displayString = symbol + "\n" +
                                "price: " + closePrice[closePrice.length - 1] + "\n" +
                                "resistanceLevel: " + resistanceDouble + "\n" +
                                "\n";
                    }
                } else {
                    symbolBreakoutMap.put(symbol, isBreakoutNew);
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            adxListener.onSuccess(displayString);
        }
    }

    public void calculateAdxDi(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, AdxListener adxListener, String symbol) {
        double[] adxOutArray = new double[closePrice.length];
        double[] minusDiOutArray = new double[closePrice.length];
        double[] plusDiOutArray = new double[closePrice.length];
        double maxDiDiff = 0.0;
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

            double currentDiDiff = plusDi - minusDi;
            if (currentDiDiff > maxDiDiff){
                maxDiDiff = currentDiDiff;
            }
            double diDiffPercent = currentDiDiff/maxDiDiff;

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

                } else if (buyState == MainActivity.BuyState.IN_SELL_STATE && (diDiffPercent <= 0.5 && diDiffPercent > 0.4)){
                    //TODO: SELL 50%
                    Log.d(TAG, "Now Selling 50%");
                    sellRemainderState = MainActivity.SellRemainderState.SELL_50;
                    adxListener.onSell();
                } else if (buyState == MainActivity.BuyState.IN_SELL_STATE && (diDiffPercent <= 0.4 && diDiffPercent > 0.3)){
                    //TODO: SELL REMAINING 70%
                    Log.d(TAG, "Now Selling 70%");
                    sellRemainderState = MainActivity.SellRemainderState.SELL_70;
                    adxListener.onSell();
                } else if (buyState == MainActivity.BuyState.IN_SELL_STATE && (diDiffPercent <= 0.3 && diDiffPercent > 0.2)){
                    //TODO: SELL REMAINING 85%
                    Log.d(TAG, "Now Selling 85%");
                    sellRemainderState = MainActivity.SellRemainderState.SELL_85;
                    adxListener.onSell();
                } else if (buyState == MainActivity.BuyState.IN_SELL_STATE && (diDiffPercent <= 0.2 && diDiffPercent > 0.1)){
                    buyState = MainActivity.BuyState.IN_BUY_STATE;
                    //TODO: SELL REMAINING 100%
                    Log.d(TAG, "Now Selling 100%");
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
