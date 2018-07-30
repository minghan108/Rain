package rain.com.rain;

import android.util.Log;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.math.BigDecimal;
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


    public void calculateSupportResistance(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, AdxListener adxListener, String symbol){
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

//                    if (closePrice[closePrice.length - 1] > resistanceDouble && resistanceDouble != 0.0){
                    if (closePrice[closePrice.length - 1] <= supportDouble && supportDouble != 0.0){

                        isBreakoutNew = true;
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
                        double targetSellPrice = supportDouble * 0.005 + supportDouble;
                        //TODO: NOTIFY NEW BREAKOUT OCCURRED
                        Log.d(TAG, "BREAK BELOW SUPPORT");
                        Log.d(TAG, "price: " + closePrice[closePrice.length - 1]);
                        Log.d(TAG, "supportDouble: " + supportDouble);
                        Log.d(TAG, "targetSellPrice: " + targetSellPrice);
                        displayString = symbol + "\n" +
                                "price: " + closePrice[closePrice.length - 1] + "\n" +
                                "supportLevel: " + supportDouble + "\n" +
                                "targetSellPrice: " + targetSellPrice + "\n" +
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

    public void calculateSma(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, SmaListener smaListener, String symbol){
        double[] smaOutArray = new double[closePrice.length];
        ArrayList<Double> smaOutArrayList = new ArrayList<>();
        ArrayList<Integer> minimaIndexArrayList = new ArrayList<>();
        double maxPercentProf = 0.0;
        double tempMaxPercentProf = 0.0;

        for(int m = 2; m < 60; m++) {
            Log.d(TAG, " ");
            Core c = new Core();
            MInteger begin = new MInteger();
            MInteger length = new MInteger();
            RetCode smaRetCode = c.sma(0, closePrice.length - 1, closePrice, m, begin, length, smaOutArray);
            tempMaxPercentProf = 0.0;
            minimaIndexArrayList.clear();
            //smaOutArrayList = removeZeroInArray(smaOutArray);
            Log.d(TAG, "calculateSma");

            if (smaRetCode == RetCode.Success) {
                int index = 0;
                try {
                    for (double smaPrice : smaOutArray) {
                        double delta1 = 0.0;
                        double delta2 = 0.0;
                        double delta3 = 0.0;
                        double delta4 = 0.0;


                        if (index - 2 >= 0 && index + 2 < smaOutArray.length) {

                            if (smaOutArray[index - 2] != 0.0 && smaOutArray[index - 1] != 0.0 && smaOutArray[index] != 0.0 && smaOutArray[index + 1] != 0.0 && smaOutArray[index + 2] != 0.0) {
                                delta1 = BigDecimal.valueOf(smaOutArray[index - 1]).subtract(BigDecimal.valueOf(smaOutArray[index - 2])).doubleValue();
                                delta2 = BigDecimal.valueOf(smaOutArray[index]).subtract(BigDecimal.valueOf(smaOutArray[index - 1])).doubleValue();
                                delta3 = BigDecimal.valueOf(smaOutArray[index + 1]).subtract(BigDecimal.valueOf(smaOutArray[index])).doubleValue();
                                delta4 = BigDecimal.valueOf(smaOutArray[index + 2]).subtract(BigDecimal.valueOf(smaOutArray[index + 1])).doubleValue();


                                if (delta1 < 0 && delta2 < 0 && delta3 > 0 && delta4 > 0) {
                                    minimaIndexArrayList.add(index);
                                    //Log.d(TAG, "smaPrice: " + smaPrice);
                                }
                            }
                        }

                        index += 1;
                    }

                    for (int minimaIndex : minimaIndexArrayList) {
                        tempMaxPercentProf += (highPrice[minimaIndex + 1] - closePrice[minimaIndex]) / closePrice[minimaIndex];
//                        Log.d(TAG, " ");
                        Log.d(TAG, "closePrice[minimaIndex]: " + closePrice[minimaIndex] + " index: " + minimaIndex);
                        Log.d(TAG, "highPrice[minimaIndex + 1]: " + highPrice[minimaIndex + 1] + " index: " + (minimaIndex + 1));
//                        Log.d(TAG, "highPrice[minimaIndex + 2]: " + highPrice[minimaIndex + 2] + " index: " + (minimaIndex + 2));
//                        Log.d(TAG, "highPrice[minimaIndex + 3]: " + highPrice[minimaIndex + 3] + " index: " + (minimaIndex + 3));

                    }

                    Log.d(TAG, "tempMaxPercentProf: " + tempMaxPercentProf);

                    if (tempMaxPercentProf > maxPercentProf){
                        maxPercentProf = tempMaxPercentProf;
                        Log.d(TAG, "maxPercentProf: " + maxPercentProf);
                        Log.d(TAG, "smaTimePeriod: " + m);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void calculateBol(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, BollingerListener adxListener, String symbol){
        Core c = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] closeSma = new double[closePrice.length];
        double[] outRealUpperBand = new double[closePrice.length];
        double[] outRealMiddleBand =  new double[closePrice.length];
        double[] outRealLowerBand =  new double[closePrice.length];
        double maxPercent = 0.0;
        ArrayList<Double> upperBBArrayList = new ArrayList<>();
        List<Double> lowerBandList = new ArrayList<>();
        int optPeriodIndex = 2;
        double optStdDev = 0.98;
        double lowerBolPrice = 0.0;
        double[] modClosePriceArray = closePrice;
//        modClosePriceArray[modClosePriceArray.length - 1] = 0.17500;
//        modClosePriceArray[modClosePriceArray.length - 1] = 0.1731828384;

//        for (int periodIndex = 1; periodIndex < 20; periodIndex++) {
//            for (double deviationIndex = 0.1; deviationIndex < 20.1; deviationIndex = (BigDecimal.valueOf(deviationIndex).add(BigDecimal.valueOf(0.01))).doubleValue()) {

        //Log.d(TAG, "firstClosePrice: " + modClosePriceArray[modClosePriceArray.length - 1]);

//        for (double price : modClosePriceArray){
//            Log.d(TAG, "closePrice: " + price);
//        }
        if (modClosePriceArray[modClosePriceArray.length - 1] > lowerBolPrice)
        for (int i = 0; modClosePriceArray[modClosePriceArray.length - 1] > lowerBolPrice; i++) {
            RetCode smaRetCode = c.sma(0, modClosePriceArray.length - 1, modClosePriceArray, optPeriodIndex, begin, length, closeSma);
            if (smaRetCode == RetCode.Success) {

                ArrayList<Double> tempCloseSmaArrayList = new ArrayList<>();
                ArrayList<Double> closeSmaArrayList = new ArrayList<>();
                tempCloseSmaArrayList = removeZeroInArray(modClosePriceArray);

                for (int a = 0; a < (optPeriodIndex - tempCloseSmaArrayList.size()); a++) {
                    closeSmaArrayList.add(0.0);
                }

                for (double closeD : tempCloseSmaArrayList) {
                    closeSmaArrayList.add(closeD);
                }

                Log.d(TAG, "closeSmaArrayList.size(): " + closeSmaArrayList.size());

                double[] finalSmaClose = new double[closeSmaArrayList.size()];
                int index = 0;
                for (double closeFinalDouble : closeSmaArrayList) {
                    finalSmaClose[index] = closeFinalDouble;
                    index = index + 1;
                }

                Log.d(TAG, "finalSmaClose.length: " + finalSmaClose.length);


                RetCode bolRetCode = c.bbands(0, closePrice.length - 1, finalSmaClose, optPeriodIndex, optStdDev,
                        optStdDev, MAType.Sma, begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);
                double tempMaxPercent = 0.0;
//                    RetCode bolRetCode = c.bbands(0, closePrice.length - 1, finalSmaClose, periodIndex, deviationIndex,
//                            deviationIndex, MAType.Sma, begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);

                if (bolRetCode == RetCode.Success) {
                    //Lower Band = 20-day SMA - (20-day standard deviation of price x 2)

//                        int bBIndex = 0;
//                        Log.d(TAG, "Symbol: " + symbol);
//                        for (double lowerBB : outRealLowerBand) {
//                                Log.d(TAG, "lowerBB: " + lowerBB);
//                            bBIndex = bBIndex + 1;
//                        }

                    lowerBandList = removeZeroInArray(outRealLowerBand);
                    lowerBolPrice = lowerBandList.get(lowerBandList.size() - 1);
                    Log.d(TAG, "lowerBolPrice: " + lowerBolPrice);
                    Log.d(TAG, "modPrice: " + modClosePriceArray[modClosePriceArray.length - 1]);
                    modClosePriceArray[modClosePriceArray.length - 1] = BigDecimal.valueOf(modClosePriceArray[modClosePriceArray.length - 1]).subtract(BigDecimal.valueOf(0.00001)).doubleValue();

                } else {
                    Log.d(TAG, "BolRetcodeFailed");
                }
            } else {
                Log.d(TAG, "SmaRetcodeFailed");
            }
        }

        double n = lowerBolPrice;
        double roundedDouble;
        roundedDouble = roundDouble(n,5);
        double buyPriceTier1 = roundDouble((roundedDouble - (roundedDouble * 0.002)), 5);
        double buyPriceTier2 = roundDouble((roundedDouble - (roundedDouble * 0.004)), 5);
        double buyPriceTier3 = roundDouble((roundedDouble - (roundedDouble * 0.006)), 5);
        double buyPriceTier4 = roundDouble((roundedDouble - (roundedDouble * 0.007)), 5);

        Log.d(TAG, "FinalBolPrice: " + roundedDouble);
        Log.d(TAG, "FinalModPrice(-0.2%): " + buyPriceTier1);
        Log.d(TAG, "FinalModPrice(-0.4%): " + buyPriceTier2);
        Log.d(TAG, "FinalModPrice(-0.6%): " + buyPriceTier3);
        Log.d(TAG, "FinalModPrice(-0.7%): " + buyPriceTier4);

//        Log.d(TAG, " ");
//        Log.d(TAG, "FinalBolPrice: " + roundedDouble);
//        Log.d(TAG, "FinalModPrice(-0.1%): " + (roundedDouble - (roundedDouble * 0.001)));
//        Log.d(TAG, "FinalModPrice(-0.2%): " + (roundedDouble - (roundedDouble * 0.002)));
//        Log.d(TAG, "FinalModPrice(-0.3%): " + (roundedDouble - (roundedDouble * 0.003)));
//        Log.d(TAG, "FinalModPrice(-0.4%): " + (roundedDouble - (roundedDouble * 0.004)));
//        Log.d(TAG, "FinalModPrice(-0.5%): " + (roundedDouble - (roundedDouble * 0.005)));
//        Log.d(TAG, "FinalModPrice(-0.6%): " + (roundedDouble - (roundedDouble * 0.006)));
//        Log.d(TAG, "FinalModPrice(-0.7%): " + (roundedDouble - (roundedDouble * 0.007)));
//        Log.d(TAG, "FinalModPrice(-0.8%): " + (roundedDouble - (roundedDouble * 0.008)));
//        Log.d(TAG, "FinalModPrice(-0.9%): " + (roundedDouble - (roundedDouble * 0.009)));
//        Log.d(TAG, "FinalModPrice(-1.0%): " + (roundedDouble - (roundedDouble * 0.01)));
//        Log.d(TAG, "SellModPrice(0.2%): " + (roundedDouble + (roundedDouble * 0.002)));
//        Log.d(TAG, "FinalModPrice: " + modClosePriceArray[modClosePriceArray.length - 1]);

//            }
//        }


        adxListener.onSuccess(roundedDouble, buyPriceTier1, buyPriceTier2, buyPriceTier3, buyPriceTier4);

    }

    public void calculateBolOptimization(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, AdxListener adxListener, String symbol){
        Core c = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] closeSma = new double[closePrice.length];
        double[] outRealUpperBand = new double[closePrice.length];
        double[] outRealMiddleBand =  new double[closePrice.length];
        double[] outRealLowerBand =  new double[closePrice.length];
        double maxPercent = 0.0;
        ArrayList<Double> upperBBArrayList = new ArrayList<>();


        for (int periodIndex = 1; periodIndex < 20; periodIndex++) {
            for (double deviationIndex = 0.1; deviationIndex < 20.1; deviationIndex = (BigDecimal.valueOf(deviationIndex).add(BigDecimal.valueOf(0.01))).doubleValue()) {
                RetCode smaRetCode = c.sma(0, closePrice.length - 1, closePrice, periodIndex, begin, length, closeSma);
                if (smaRetCode == RetCode.Success) {

                    ArrayList<Double> tempCloseSmaArrayList = new ArrayList<>();
                    ArrayList<Double> closeSmaArrayList = new ArrayList<>();
                    tempCloseSmaArrayList = removeZeroInArray(closePrice);

                    for (int i = 0; i < (500 - tempCloseSmaArrayList.size()); i++) {
                        closeSmaArrayList.add(0.0);
                    }

                    for (double closeD : tempCloseSmaArrayList) {
                        closeSmaArrayList.add(closeD);
                    }

                    Log.d(TAG, "closeSmaArrayList.size(): " + closeSmaArrayList.size());

                    double[] finalSmaClose = new double[closeSmaArrayList.size()];
                    int index = 0;
                    for (double closeFinalDouble : closeSmaArrayList) {
                        finalSmaClose[index] = closeFinalDouble;
                        index = index + 1;
                    }

                    Log.d(TAG, "finalSmaClose.length: " + finalSmaClose.length);


//            RetCode bolRetCode = c.bbands(0, closePrice.length - 1, finalSmaClose, 20, 2,
//                    2, MAType.Sma, begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);
                    double tempMaxPercent = 0.0;
                    RetCode bolRetCode = c.bbands(0, closePrice.length - 1, finalSmaClose, periodIndex, deviationIndex,
                            deviationIndex, MAType.Sma, begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);

                    if (bolRetCode == RetCode.Success) {
                        int bBIndex = 0;
                        Log.d(TAG, "Symbol: " + symbol);
                        for (double lowerBB : outRealLowerBand) {
                            if (lowPrice[bBIndex] <= lowerBB && lowPrice[bBIndex] != 0.0) {
                                double lowerBBdiffPercent = (closePrice[bBIndex] - lowerBB) / lowerBB;
                                Log.d(TAG, "bBIndex: " + bBIndex);
                                Log.d(TAG, "lowerBB: " + lowerBB);
                                Log.d(TAG, "lowPrice: " + lowPrice[bBIndex]);
                                Log.d(TAG, "closePrice: " + closePrice[bBIndex]);
                                Log.d(TAG, "diffPercent lowerBB and closePrice: " + lowerBBdiffPercent);

                                if (bBIndex + 2 < highPrice.length) {
                                    double nextPeriodLowerBBdiffPercent = (highPrice[bBIndex + 1] - lowerBB) / lowerBB;
                                    double nextNextPeriodLowerBBdiffPercent = (highPrice[bBIndex + 2] - lowerBB) / lowerBB;
                                    Log.d(TAG, "nextPeriod High: " + highPrice[bBIndex + 1]);
                                    Log.d(TAG, "nextPeriodLowerBBdiffPercent: " + nextPeriodLowerBBdiffPercent);
                                    Log.d(TAG, "nextNextPeriodLowerBBdiffPercent: " + nextNextPeriodLowerBBdiffPercent);
                                }

                                if (lowerBBdiffPercent >= 0.002) {
                                    tempMaxPercent = tempMaxPercent + lowerBBdiffPercent;
                                }
                            }
                            bBIndex = bBIndex + 1;
                        }

                        Log.d(TAG, "tempMaxPercent: " + tempMaxPercent);
                        Log.d(TAG, "periodIndex: " + periodIndex);
                        Log.d(TAG, "deviationIndex: " + deviationIndex);

                        if (tempMaxPercent > maxPercent){
                            maxPercent = tempMaxPercent;
                            Log.d(TAG, "maxPercent: " + maxPercent);
                            Log.d(TAG, "periodIndex: " + periodIndex);
                            Log.d(TAG, "deviationIndex: " + deviationIndex);
                        }
                    } else {
                        Log.d(TAG, "BolRetcodeFailed");
                    }
                } else {
                    Log.d(TAG, "SmaRetcodeFailed");
                }

            }
        }


//        adxListener.onSuccess("");

    }

    public void calculateBolCLoseOptimization(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, AdxListener adxListener, String symbol){
        Core c = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] closeSma = new double[closePrice.length];
        double[] outRealUpperBand = new double[closePrice.length];
        double[] outRealMiddleBand =  new double[closePrice.length];
        double[] outRealLowerBand =  new double[closePrice.length];
        double maxPercent = 0.0;
        ArrayList<Double> upperBBArrayList = new ArrayList<>();


        for (int periodIndex = 1; periodIndex < 45; periodIndex++) {
            for (double deviationIndex = 0.1; deviationIndex < 20.1; deviationIndex = (BigDecimal.valueOf(deviationIndex).add(BigDecimal.valueOf(0.01))).doubleValue()) {
                RetCode smaRetCode = c.sma(0, closePrice.length - 1, closePrice, periodIndex, begin, length, closeSma);
                if (smaRetCode == RetCode.Success) {

                    ArrayList<Double> tempCloseSmaArrayList = new ArrayList<>();
                    ArrayList<Double> closeSmaArrayList = new ArrayList<>();
                    tempCloseSmaArrayList = removeZeroInArray(closePrice);

                    for (int i = 0; i < (500 - tempCloseSmaArrayList.size()); i++) {
                        closeSmaArrayList.add(0.0);
                    }

                    for (double closeD : tempCloseSmaArrayList) {
                        closeSmaArrayList.add(closeD);
                    }

                    Log.d(TAG, "closeSmaArrayList.size(): " + closeSmaArrayList.size());

                    double[] finalSmaClose = new double[closeSmaArrayList.size()];
                    int index = 0;
                    for (double closeFinalDouble : closeSmaArrayList) {
                        finalSmaClose[index] = closeFinalDouble;
                        index = index + 1;
                    }

                    Log.d(TAG, "finalSmaClose.length: " + finalSmaClose.length);


//            RetCode bolRetCode = c.bbands(0, closePrice.length - 1, finalSmaClose, 20, 2,
//                    2, MAType.Sma, begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);
                    double tempMaxPercent = 0.0;
                    double nextPeriodLowerBBdiffPercent = 0.0;
                    double nextNextPeriodLowerBBdiffPercent = 0.0;
                    double nextNextNextPeriodLowerBBdiffPercent = 0.0;
                    RetCode bolRetCode = c.bbands(0, closePrice.length - 1, finalSmaClose, periodIndex, deviationIndex,
                            deviationIndex, MAType.Sma, begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);

                    if (bolRetCode == RetCode.Success) {
                        int bBIndex = 0;
                        Log.d(TAG, "Symbol: " + symbol);
                        for (double lowerBB : outRealLowerBand) {
                            if (closePrice[bBIndex] <= lowerBB && closePrice[bBIndex] != 0.0) {
                                //double lowerBBdiffPercent = (closePrice[bBIndex] - lowerBB) / lowerBB;
                                Log.d(TAG, "bBIndex: " + bBIndex);
                                Log.d(TAG, "lowerBB: " + lowerBB);
                                Log.d(TAG, "lowPrice: " + lowPrice[bBIndex]);
                                Log.d(TAG, "closePrice: " + closePrice[bBIndex]);
                                //Log.d(TAG, "diffPercent lowerBB and closePrice: " + lowerBBdiffPercent);

                                if (bBIndex + 3 < highPrice.length) {
                                    nextPeriodLowerBBdiffPercent = (highPrice[bBIndex + 1] - lowerBB) / lowerBB;
                                    nextNextPeriodLowerBBdiffPercent = (highPrice[bBIndex + 2] - lowerBB) / lowerBB;
                                    nextNextNextPeriodLowerBBdiffPercent = (highPrice[bBIndex + 3] - lowerBB) / lowerBB;
                                    Log.d(TAG, "nextPeriod High: " + highPrice[bBIndex + 1]);
                                    Log.d(TAG, "nextPeriodLowerBBdiffPercent: " + nextPeriodLowerBBdiffPercent);
                                    Log.d(TAG, "nextNextPeriodLowerBBdiffPercent: " + nextNextPeriodLowerBBdiffPercent);
                                    Log.d(TAG, "nextNextNextPeriodLowerBBdiffPercent: " + nextNextNextPeriodLowerBBdiffPercent);
                                }

//                                if (nextPeriodLowerBBdiffPercent >= 0.001) {
                                    tempMaxPercent = tempMaxPercent + nextPeriodLowerBBdiffPercent;
//                                }
                            }
                            bBIndex = bBIndex + 1;
                        }

                        Log.d(TAG, "tempMaxPercent: " + tempMaxPercent);
                        Log.d(TAG, "periodIndex: " + periodIndex);
                        Log.d(TAG, "deviationIndex: " + deviationIndex);

                        if (tempMaxPercent > maxPercent){
                            maxPercent = tempMaxPercent;
                            Log.d(TAG, "maxPercent: " + maxPercent);
                            Log.d(TAG, "periodIndex: " + periodIndex);
                            Log.d(TAG, "deviationIndex: " + deviationIndex);
                        }
                    } else {
                        Log.d(TAG, "BolRetcodeFailed");
                    }
                } else {
                    Log.d(TAG, "SmaRetcodeFailed");
                }

            }
        }


//        adxListener.onSuccess("");

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

    public static double roundDouble(double x, int roundTodecimalPlace)
    {
        BigDecimal b = new BigDecimal(Double.toString(x));
        b = b.setScale(roundTodecimalPlace, BigDecimal.ROUND_HALF_DOWN);
        return b.doubleValue();
    }
}
