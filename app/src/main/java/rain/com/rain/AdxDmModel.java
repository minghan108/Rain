package rain.com.rain;

import android.util.Log;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static rain.com.rain.MainActivity.buyState;
import static rain.com.rain.MainActivity.currentDiState;
import static rain.com.rain.MainActivity.decimalPlaces;
import static rain.com.rain.MainActivity.initialDiState;
import static rain.com.rain.MainActivity.isFirstLaunch;
import static rain.com.rain.MainActivity.isFirstScanComplete;
import static rain.com.rain.MainActivity.isMinusDiGreater;
import static rain.com.rain.MainActivity.pumpHashMap;
import static rain.com.rain.MainActivity.sellRemainderState;
import static rain.com.rain.MainActivity.startMoney;
import static rain.com.rain.MainActivity.symbolBreakoutMap;

public class AdxDmModel {
    private String TAG = "AdxDmModel ";
    private boolean isFirstLaunch = true;
    private ArrayList<String> targetSymbolList = new ArrayList<>();
    private final double PRICE_OFFSET = 0.98475454;
    private double maxPrice;
    private double minPrice;



    public void calculatePumpPercent(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, SmaListener smaListener, String symbol){
        Core core = new Core();
        double minPrice = closePrice[0];
        int minPriceIndex = 0;
        double deltaPercent = 0.0;
        BigDecimal closePriceBD;
        BigDecimal minPriceBD;

        for (int j = 1; j < 3; j++){
            if (closePrice[j] < minPrice){
                minPrice = closePrice[j];
                minPriceIndex = j;
            }
        }

        if (openPrice[openPrice.length - 1] < minPrice){
            minPrice = openPrice[openPrice.length - 1];
            minPriceIndex = openPrice.length - 1;
        }

        closePriceBD = BigDecimal.valueOf(closePrice[closePrice.length - 1]);
        minPriceBD = BigDecimal.valueOf(minPrice);
        deltaPercent = (closePriceBD.subtract(minPriceBD)).divide(minPriceBD, 9, RoundingMode.HALF_DOWN).multiply(BigDecimal.valueOf(100.0)).doubleValue();

        pumpHashMap.put(symbol, deltaPercent);

        smaListener.onSuccess();

    }

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

    public void calculateSma(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, SmaListener smaListener, String symbol){
        Log.d(TAG, "symbol: " + symbol);
        int optInTimePeriod = 200;
        double mult = 3.0;
        double[] hlc3VolumeSmaOutArray = new double[closePrice.length];
        double[] volumeSmaOutArray = new double[closePrice.length];
        double[] hlc3Array = new double[closePrice.length];
        ArrayList<Double> hlc3ArrayList = new ArrayList<>();
        ArrayList<Double> hlcVolumeArrayList = new ArrayList<>();
        ArrayList<Double> vwmaArrayList = new ArrayList<>();
        ArrayList<Double> hlc3VolumeSmaOutArrayList = new ArrayList<>();
        ArrayList<Double> volumeSmaOutArrayList = new ArrayList<>();
        ArrayList<Double> hlc3StdOutArrayList = new ArrayList<>();
        ArrayList<Double> cumHlc3VolArrayList = new ArrayList<>();
        ArrayList<Double> cumVolArrayList = new ArrayList<>();
        ArrayList<Double> devArrayList = new ArrayList<>();
        ArrayList<Double> lower5ArrayList = new ArrayList<>();
        ArrayList<Double> lower6ArrayList = new ArrayList<>();
        double[] hlc3VolumeArray = new double[closePrice.length];
        double[] hlc3StdOutArray = new double[closePrice.length];


        int index = 0;

        for (double highP : highPrice){
//            Log.d(TAG, "highPrice: " + highPrice[index]);
//            Log.d(TAG, "lowPrice: " + lowPrice[index]);
//            Log.d(TAG, "openPrice: " + openPrice[index]);
//            Log.d(TAG, "closePrice: " + closePrice[index]);
            BigDecimal highBD = BigDecimal.valueOf(highPrice[index]);
            BigDecimal lowBD = BigDecimal.valueOf(lowPrice[index]);
            BigDecimal openBD = BigDecimal.valueOf(openPrice[index]);
            BigDecimal closeBD = BigDecimal.valueOf(closePrice[index]);
            BigDecimal volumeBD = BigDecimal.valueOf(volume[index]);
            BigDecimal hlc3BD = (highBD.add(lowBD).add(closeBD)).divide(BigDecimal.valueOf(3.0), 9, RoundingMode.HALF_UP);
            BigDecimal hlc3VolumeBD = hlc3BD.multiply(volumeBD);

            hlc3Array[index] = hlc3BD.doubleValue();
            hlc3ArrayList.add(hlc3BD.doubleValue());
            hlcVolumeArrayList.add(hlc3VolumeBD.doubleValue());
            hlc3VolumeArray[index] = hlc3VolumeBD.doubleValue();
            index += 1;
        }

//        int volIndex2 = 199;
//        for (int volIndex1 = 0; volIndex2 < hlc3Array.length; volIndex1++){
//            double cumHlc3Vol = 0.0;
//            double cumVol = 0.0;
//            for(int indx = 0; indx < 200; indx++){
//                cumHlc3Vol = BigDecimal.valueOf(cumHlc3Vol).add(BigDecimal.valueOf(hlc3VolumeArray[indx + volIndex1])).doubleValue();
//                cumVol = BigDecimal.valueOf(cumVol).add(BigDecimal.valueOf(volume[indx + volIndex1])).doubleValue();
//            }
//            cumHlc3VolArrayList.add(cumHlc3Vol);
//            cumVolArrayList.add(cumVol);
//            volIndex2 += 1;
//        }
//
//
//
//        Log.d(TAG, "cumHlc3VolArrayList.size: " + cumHlc3VolArrayList.size());
//        Log.d(TAG, "cumVolArrayList.size: " + cumVolArrayList.size());
//        Log.d(TAG, "hlcVolumeArrayList.size: " + hlcVolumeArrayList.size());


        Core c = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        RetCode hlc3VolumeSmaRetCode = c.sma(0, hlc3VolumeArray.length - 1, hlc3VolumeArray, optInTimePeriod, begin, length, hlc3VolumeSmaOutArray);
        RetCode volumeSmaRetCode = c.sma(0, volume.length - 1, volume, optInTimePeriod, begin, length, volumeSmaOutArray);
        hlc3VolumeSmaOutArrayList = removeZeroInArray(hlc3VolumeSmaOutArray);
        volumeSmaOutArrayList = removeZeroInArray(volumeSmaOutArray);

        if (hlc3VolumeSmaRetCode == RetCode.Success && volumeSmaRetCode == RetCode.Success) {
            int smaIndex = 0;

            for(Double sma : hlc3VolumeSmaOutArrayList){
                BigDecimal hlc3VolumeSmaBD = BigDecimal.valueOf(hlc3VolumeSmaOutArrayList.get(smaIndex));
                BigDecimal volumeSmaBD = BigDecimal.valueOf(volumeSmaOutArrayList.get(smaIndex));
                BigDecimal vwmaBD = hlc3VolumeSmaBD.divide(volumeSmaBD, 9, RoundingMode.HALF_UP);
                vwmaArrayList.add(vwmaBD.doubleValue());
                smaIndex += 1;
            }

//            for (Double sma : hlc3VolumeSmaOutArrayList) {
//                Log.d(TAG, "hlc3VolumeSmaOutArrayList: " + sma);
//                Log.d(TAG, "volumeSmaOutArrayList: " + volumeSmaOutArrayList.get(smaIndex));
////                Log.d(TAG, "hlc3VolumeSmaOutArrayList.size(): " + hlc3VolumeSmaOutArrayList.size());
////                Log.d(TAG, "volumeSmaOutArrayList.size(): " + volumeSmaOutArrayList.size());
//                smaIndex += 1;
//            }
        }

        RetCode hlc3StdRetCode = this.stdDev(0, hlc3Array.length - 1, hlc3Array, optInTimePeriod, 1.0, begin, length, hlc3StdOutArray);

        if (hlc3StdRetCode == RetCode.Success) {
            hlc3StdOutArrayList = removeZeroInArray(hlc3StdOutArray);
            for(Double hlc3StdOut : hlc3StdOutArrayList){
                BigDecimal hlc3StdOutBD = BigDecimal.valueOf(hlc3StdOut);
                BigDecimal devBD = hlc3StdOutBD.multiply(BigDecimal.valueOf(mult));
                devArrayList.add(devBD.doubleValue());
            }
            Log.d(TAG, "hlc3StdOutArrayList.size: " + hlc3StdOutArrayList.size());
        }

        int basisIndex = 0;
        for (Double dev : devArrayList) {
            double basis = vwmaArrayList.get(basisIndex);
            BigDecimal basisBD = BigDecimal.valueOf(basis);
            BigDecimal devBD = BigDecimal.valueOf(dev);

            double upper1 = BigDecimal.valueOf(0.236).multiply(devBD).add(basisBD).doubleValue();
            double upper2 = BigDecimal.valueOf(0.382).multiply(devBD).add(basisBD).doubleValue();
            double upper3 = BigDecimal.valueOf(0.5).multiply(devBD).add(basisBD).doubleValue();
            double upper4 = BigDecimal.valueOf(0.618).multiply(devBD).add(basisBD).doubleValue();
            double upper5 = BigDecimal.valueOf(0.764).multiply(devBD).add(basisBD).doubleValue();
            double upper6 = BigDecimal.valueOf(1.0).multiply(devBD).add(basisBD).doubleValue();
            double lower1 = basisBD.subtract(BigDecimal.valueOf(0.236).multiply(devBD)).doubleValue();
            double lower2 = basisBD.subtract(BigDecimal.valueOf(0.382).multiply(devBD)).doubleValue();
            double lower3 = basisBD.subtract(BigDecimal.valueOf(0.5).multiply(devBD)).doubleValue();
            double lower4 = basisBD.subtract(BigDecimal.valueOf(0.618).multiply(devBD)).doubleValue();
            double lower5 = basisBD.subtract(BigDecimal.valueOf(0.764).multiply(devBD)).doubleValue();
            double lower6 = basisBD.subtract(BigDecimal.valueOf(1.0).multiply(devBD)).doubleValue();
            lower5ArrayList.add(lower5);
            lower6ArrayList.add(lower6);
//            Log.d(TAG, " ");
//            Log.d(TAG, "upper1: " + upper1);
//            Log.d(TAG, "upper2: " + upper2);
//            Log.d(TAG, "upper3: " + upper3);
//            Log.d(TAG, "upper4: " + upper4);
//            Log.d(TAG, "upper5: " + upper5);
//            Log.d(TAG, "upper6: " + upper6);
//            Log.d(TAG, "lower1: " + lower1);
//            Log.d(TAG, "lower2: " + lower2);
//            Log.d(TAG, "lower3: " + lower3);
//            Log.d(TAG, "lower4: " + lower4);
//            Log.d(TAG, "lower5: " + lower5);
//            Log.d(TAG, "lower6: " + lower6);
            basisIndex += 1;
        }
        Log.d(TAG, "symbol: " + symbol);
        Log.d(TAG, "lower5: " + lower5ArrayList.get(lower5ArrayList.size() - 1));
        Log.d(TAG, "closePrice: " + closePrice[closePrice.length - 1]);
        if (closePrice[closePrice.length - 1] < lower6ArrayList.get(lower6ArrayList.size() - 1)){
            Log.d(TAG, "Symbol Lower than Fib 6: " + symbol);
        }

        if (closePrice[closePrice.length - 1] < lower5ArrayList.get(lower5ArrayList.size() - 1)){
            Log.d(TAG, "Symbol Lower than Fib 5: " + symbol);
        }

        smaListener.onSuccess();
    }

    public void calculatePSar(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, SmaListener smaListener, String symbol){
        double[] outPSarArray = new double[highPrice.length];
        ArrayList<Double> outPSarArrayList = new ArrayList<>();
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        RetCode pSarRetCode = core.sar(0, highPrice.length - 1, highPrice, lowPrice, 0.02, 0.2, begin, length, outPSarArray);
//        RetCode pSarRetCode = core.sarExt(0, highPrice.length - 1, highPrice, lowPrice, 0.02, 0.02, 0.02, 0.02,
//                0.2, 0.02, 0.02, 0.2, begin, length, outPSarArray);

//        int startIdx, int endIdx, double[] inHigh, double[] inLow, double optInAcceleration, double optInMaximum, MInteger outBegIdx, MInteger outNBElement, double[] outReal

//        int startIdx, int endIdx, float[] inHigh, float[] inLow, double optInStartValue, double optInOffsetOnReverse, double optInAccelerationInitLong, double optInAccelerationLong,
//        double optInAccelerationMaxLong, double optInAccelerationInitShort, double optInAccelerationShort, double optInAccelerationMaxShort, MInteger outBegIdx, MInteger outNBElement, double[] outReal


        if (pSarRetCode == RetCode.Success){
//            Log.d(TAG, "lowPrice: " + lowPrice[lowPrice.length -1]);
//            Log.d(TAG, "highPrice: " + highPrice[highPrice.length -1]);
            outPSarArrayList = removeZeroInArray(outPSarArray);

            for (Double psar : outPSarArray){
                Log.d(TAG, "psar: " + psar);
            }
        }

    }

    private double[] convertArrayListToArray(ArrayList<Double> arrayList, int arraySize){
//        double[] returnArray = new double[arrayList.size() - (fromIndex)];
//
//        int index = 0;
//        for(int i = fromIndex; i < arrayList.size(); i++){
//            returnArray[index] = arrayList.get(i);
//            index += 1;
//        }

        double[] returnArray = new double[arraySize];
        for (int i = 0; i < (arraySize - arrayList.size()); i++){
            returnArray[i] = 0.0;
        }

        int index = arraySize - arrayList.size();
        for (Double elem : arrayList){
            returnArray[index] = elem;
            index += 1;
        }

        return returnArray;
    }

    public void calculateRSI(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, SmaListener smaListener, String symbol){
        double[] outRSIArray = new double[closePrice.length];
        ArrayList<Double> outRSIArrayList = new ArrayList<>();

        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        RetCode rsiRetCode = core.rsi(0, closePrice.length - 1, closePrice, 14, begin, length, outRSIArray);

        for (double rsi : outRSIArray){
            outRSIArrayList = removeZeroInArray(outRSIArray);
        }

        if (outRSIArrayList.get(outRSIArrayList.size() - 1) < 35){
            Log.d(TAG, "Rsi < 40: " + symbol + " " + (outRSIArrayList.get(outRSIArrayList.size() - 1)));
            targetSymbolList.add(symbol);
        }


    }

    public void calculateWaveC(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, SmaListener smaListener, String symbol){
        if (targetSymbolList.contains(symbol)) {
            double[] outFastMA5Array = new double[closePrice.length];
            double[] outSlowMA5Array = new double[closePrice.length];
            double[] outSlowMA6Array = new double[closePrice.length];
            double[] outFastMA6Array;
            double[] signal5Array = new double[closePrice.length];
            ArrayList<Double> fastMA5ArrayList = new ArrayList<>();
            ArrayList<Double> slowMA5ArrayList = new ArrayList<>();
            ArrayList<Double> signal5ArrayList = new ArrayList<>();
            ArrayList<Double> slowMA6ArrayList = new ArrayList<>();
            Core core = new Core();
            MInteger begin = new MInteger();
            MInteger length = new MInteger();

            RetCode fastMA5RetCode = core.ema(0, closePrice.length - 1, closePrice, 8, begin, length, outFastMA5Array);
            RetCode slowMA5RetCode = core.ema(0, closePrice.length - 1, closePrice, 233, begin, length, outSlowMA5Array);

//        for (double fastMA5 : outFastMA5Array){
//            Log.d(TAG, "fastMA5: " + fastMA5);
//        }

//        for (double slowMA5 : outSlowMA5Array){
//            Log.d(TAG, "slowMA5: " + slowMA5);
//        }
//
            fastMA5ArrayList = removeZeroInArray(outFastMA5Array);
            slowMA5ArrayList = removeZeroInArray(outSlowMA5Array);


            double[] fastMA5Copy = convertArrayListToArray(fastMA5ArrayList, 1000);
            Log.d(TAG, "fastMA5Copy: " + fastMA5Copy[fastMA5Copy.length - 2]);
            double[] slowMA5Copy = convertArrayListToArray(slowMA5ArrayList, 1000);
            double[] macd5Array = new double[fastMA5Copy.length];

//        for (double slowMA5 : slowMA5Copy){
//            Log.d(TAG, "slowMA5: " + slowMA5);
//        }


            if (fastMA5RetCode == RetCode.Success && slowMA5RetCode == RetCode.Success) {
                int waveIndex = 0;
                for (double fastMA5 : fastMA5Copy) {
                    BigDecimal fastMA5BD = BigDecimal.valueOf(fastMA5);
                    BigDecimal slowMA5BD = BigDecimal.valueOf(slowMA5Copy[waveIndex]);
                    macd5Array[waveIndex] = fastMA5BD.subtract(slowMA5BD).doubleValue();
                    waveIndex += 1;
                }
            }

            macd5Array = Arrays.copyOfRange(macd5Array, 500, fastMA5Copy.length);
            Log.d(TAG, "macd5Array: " + macd5Array[macd5Array.length - 1]);
            Log.d(TAG, "macd5Array.size: " + macd5Array.length);

            RetCode signal5RetCode = core.ema(0, macd5Array.length - 1, macd5Array, 233, begin, length, signal5Array);
            signal5ArrayList = removeZeroInArray(signal5Array);
            Log.d(TAG, "signal5ArrayList.size: " + signal5ArrayList.size());
            double[] signal5ArrayCopy = convertArrayListToArray(signal5ArrayList, 500);
            signal5ArrayCopy = Arrays.copyOfRange(signal5ArrayCopy, 470, 500);
            double[] macd5ArrayCopy = Arrays.copyOfRange(macd5Array, 470, 500);
            double[] hist5Array = new double[macd5ArrayCopy.length];

            int index = 0;
            for (double signal5 : signal5ArrayCopy) {
                BigDecimal signal5BD = BigDecimal.valueOf(signal5);
                BigDecimal macd5BD = BigDecimal.valueOf(macd5ArrayCopy[index]);
                hist5Array[index] = macd5BD.subtract(signal5BD).doubleValue();
                //Log.d(TAG, "hist5: " + hist5Array[index]);
                index += 1;
            }

            Log.d(TAG, "removeFastMa5: " + (removeZeroInArray(fastMA5Copy).size()));
            Log.d(TAG, "fastMA5Copy.size: " + fastMA5Copy.length);
            Log.d(TAG, "slowMA5Copy.size: " + slowMA5Copy.length);
            Log.d(TAG, "macd5Array.size: " + macd5Array.length);


//        RetCode fastMA5RetCode = core.ema(0, closePrice.length - 1, closePrice, 8, begin, length, outFastMA5Array);
//        RetCode slowMA5RetCode = core.ema(0, closePrice.length - 1, closePrice, 233, begin, length, outSlowMA5Array);
//
////        for (double fastMA5 : outFastMA5Array){
////            Log.d(TAG, "fastMA5: " + fastMA5);
////        }
//
////        for (double slowMA5 : outSlowMA5Array){
////            Log.d(TAG, "slowMA5: " + slowMA5);
////        }
////
//        fastMA5ArrayList = removeZeroInArray(outFastMA5Array);
//        slowMA5ArrayList = removeZeroInArray(outSlowMA5Array);

//        RetCode signal5RetCode = core.ema(0, macd5Array.length - 1, macd5Array, 233, begin, length, signal5Array);
//        signal5ArrayList = removeZeroInArray(signal5Array);
//        double[] signal5ArrayCopy = convertArrayListToArray(signal5ArrayList);
//        double[] hist5Array = new double[signal5ArrayCopy.length];
//
//
//        if (signal5RetCode == RetCode.Success){
//            int waveIndex = 0;
//            for (double macd5 : macd5Array){
//                BigDecimal macd5BD = BigDecimal.valueOf(macd5);
//                BigDecimal signal5BD = BigDecimal.valueOf(signal5ArrayCopy[waveIndex]);
//                hist5Array[waveIndex] = macd5BD.subtract(signal5BD).doubleValue();
//                waveIndex += 1;
//            }
//        }
//
            outFastMA6Array = fastMA5Copy;
            RetCode slowMA6RetCode = core.ema(0, closePrice.length - 1, closePrice, 377, begin, length, outSlowMA6Array);
            slowMA6ArrayList = removeZeroInArray(outSlowMA6Array);
            double[] slowMA6Copy = convertArrayListToArray(slowMA6ArrayList, 1000);
            double[] macd6Array = new double[slowMA6Copy.length];
//
//        for(double slowMA : slowMA6Copy){
//            Log.d(TAG, "slowMA: " + slowMA);
//        }

            if (slowMA6RetCode == RetCode.Success) {
                int waveIndex = 0;
                int histIndex = 0;
                for (double fastMA6 : outFastMA6Array) {
                    BigDecimal fastMacd6BD = BigDecimal.valueOf(fastMA6);
                    BigDecimal slowMA6 = BigDecimal.valueOf(slowMA6Copy[waveIndex]);
                    macd6Array[waveIndex] = fastMacd6BD.subtract(slowMA6).doubleValue();
//                BigDecimal fastMacd6BD = BigDecimal.valueOf(roundDouble(fastMA6, 2));
//                BigDecimal slowMA6 = BigDecimal.valueOf(roundDouble(slowMA6Copy[waveIndex], 2));
//                macd6Array[waveIndex] = fastMacd6BD.subtract(slowMA6).doubleValue();
//                    Log.d(TAG, "macd6: " + macd6Array[waveIndex]);
//                    if (waveIndex > 969) {
//                        Log.d(TAG, "hist5: " + hist5Array[histIndex]);
//                        histIndex += 1;
//                    }
//                    waveIndex += 1;
                }
            }

            if (macd6Array[macd6Array.length - 1] > 0.0 || hist5Array[hist5Array.length - 1] > 0.0){
                Log.d(TAG, "symbol passed wave and rsi: " + symbol);
            }
        }

        smaListener.onSuccess();

//        int waveIndex = 0;
//        for (double hist5 : hist5Array){
//            Log.d(TAG, "hist5: " + hist5);
//            Log.d(TAG, "waveIndex: " + waveIndex);
//            waveIndex += 1;
//        }
//        Log.d(TAG, "closePrice.length: " + closePrice.length);

        //int startIdx, int endIdx, double[] inReal, int optInTimePeriod, MInteger outBegIdx, MInteger outNBElement, double[] outReal

        // Wave C
//        fastMA5 = usewc ? ema(close, 8) : na
//        slowMA5 = usewc ? ema(close, 233) : na
//        macd5 = usewc ? fastMA5 - slowMA5 : na
//        signal5 = usewc ? ema(macd5, 233) : na
//        hist5 = usewc ? macd5 - signal5 : na
//
//        fastMA6 = usewc ? ema(close, 8) : na
//        slowMA6 = usewc ? ema(close, 377) : na
//        macd6 = usewc ? fastMA6 - slowMA6 : na
    }

    public  HashMap<String, double[]> calculateHeikinAshiCandle(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice){
        int maxIndex = closePrice.length;
        double[] haCloseArray = new double[closePrice.length];
        double[] haOpenArray = new double[closePrice.length];
        double[] haLowArray = new double[closePrice.length];
        double[] haHighArray = new double[closePrice.length];
        BigDecimal firCloseBD = BigDecimal.valueOf(closePrice[0]);
        BigDecimal firOpenBD = BigDecimal.valueOf(openPrice[0]);
        BigDecimal firLowBD = BigDecimal.valueOf(lowPrice[0]);
        BigDecimal firHighBD = BigDecimal.valueOf(highPrice[0]);
        haCloseArray[0] = (firCloseBD.add(firOpenBD).add(firLowBD).add(firHighBD)).divide(BigDecimal.valueOf(4.0), 9, RoundingMode.HALF_UP).doubleValue();
        haOpenArray[0] = (firOpenBD.add(firCloseBD)).divide(BigDecimal.valueOf(2.0), 9, RoundingMode.HALF_UP).doubleValue();
        haLowArray[0] = lowPrice[0];
        haHighArray[0] = highPrice[0];
        HashMap<String, double[]> haHashMap = new HashMap<>();

        for (int index = 1; index < maxIndex; index++){
            BigDecimal curCloseBD = BigDecimal.valueOf(closePrice[index]);
            BigDecimal curOpenBD = BigDecimal.valueOf(openPrice[index]);
            BigDecimal curLowBD = BigDecimal.valueOf(lowPrice[index]);
            BigDecimal curHighBD = BigDecimal.valueOf(highPrice[index]);
            BigDecimal prevCloseBD = BigDecimal.valueOf(closePrice[index - 1]);
            BigDecimal prevOpenBD = BigDecimal.valueOf(openPrice[index - 1]);
            haCloseArray[index] = (curCloseBD.add(curOpenBD).add(curLowBD).add(curHighBD)).divide(BigDecimal.valueOf(4.0), 9, RoundingMode.HALF_UP).doubleValue();
            haOpenArray[index] = (prevOpenBD.add(prevCloseBD)).divide(BigDecimal.valueOf(2.0), 9, RoundingMode.HALF_UP).doubleValue();
            haLowArray[index] = Math.min(lowPrice[index], Math.min(haOpenArray[index], haCloseArray[index]));
            haHighArray[index] = Math.max(highPrice[index], Math.max(haOpenArray[index], haCloseArray[index]));
        }

        haHashMap.put("haCloseArray", haCloseArray);
        haHashMap.put("haOpenArray", haOpenArray);
        haHashMap.put("haLowArray", haLowArray);
        haHashMap.put("haHighArray", haHighArray);
        BigDecimal total;
        for (int index = haCloseArray.length - 1; index > haCloseArray.length - 7; index--){


        }

        localToGMT();
        Log.d(TAG, "haCloseArray: " + haCloseArray[haCloseArray.length - 2]);
        Log.d(TAG, "haOpenArray: " + haOpenArray[haOpenArray.length - 2]);
        Log.d(TAG, "haLowArray: " + haLowArray[haLowArray.length - 2]);
        Log.d(TAG, "haHighArray: " + haHighArray[haHighArray.length - 2]);



        return haHashMap;
    }

    public void localToGMT() {
        String format = "HH:mm";
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTime = sdf.format(new Date());
        Log.d(TAG, "utcTime: " + utcTime);
    }

    public void calculateReversalBand(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, PriceCalculationListener priceCalculationListener, String symbol) {
        double[] closePriceCopy = Arrays.copyOfRange(closePrice, 0, 1000);
        double[] lowPriceCopy = Arrays.copyOfRange(lowPrice, 0, 1000);
        Log.d(TAG, "closePriceCopy.length: " + closePriceCopy.length);
        double decrement = 1.0;
        int maxDecrement = findDecimalPlaces(closePrice);
        decimalPlaces = maxDecrement;
        Log.d(TAG, "maxDecrement: " + maxDecrement);

//        for (double hp : highPrice){
//            Log.d(TAG, "highPrice: " + hp);
//        }
//
//        for (double lp : lowPrice){
//            Log.d(TAG, "lowPrice: " + lp);
//        }
//
//        for (double op : openPrice){
//            Log.d(TAG, "openPrice: " + op);
//        }
//
//        for (double cp : closePrice){
//            Log.d(TAG, "closePrice: " + cp);
//        }
//        for (double mIdx = 2.0; mIdx < 3.1; mIdx += 0.1) {

//        for (int index = 30; index < 999; index++) {
            double lower = 0.0;

            for (int decreIndex = 0; decreIndex < maxDecrement; decreIndex++) {
                decrement = BigDecimal.valueOf(decrement).divide(BigDecimal.valueOf(10.0), 10, RoundingMode.HALF_UP).doubleValue();
                Log.d(TAG, "decrement: " + decrement);
                int closeIndex = 0;

                while (closePriceCopy[closePriceCopy.length - 1] > lower) {
                    int timePeriod = 25;
                    int atrRange = 25;
                    double mult = 2.5;
//                    int timePeriod = period;
//                    int atrRange = period;
//                    double mult = mIdx;
                    double[] outTREmaArray = new double[closePriceCopy.length];
                    double[] outMaArray = new double[closePriceCopy.length];
                    double[] outTrueRangeArray = new double[closePriceCopy.length];
                    ArrayList<Double> outTREmaArrayList = new ArrayList<>();
                    ArrayList<Double> outMaArrayList = new ArrayList<>();
                    ArrayList<Double> outTrueRangeArrayList = new ArrayList<>();
                    Core core = new Core();
                    MInteger begin = new MInteger();
                    MInteger length = new MInteger();

                    RetCode trueRangeRetCode = core.trueRange(0, closePriceCopy.length - 1, openPrice, lowPriceCopy, closePriceCopy, begin, length, outTrueRangeArray);
                    outTrueRangeArrayList = removeZeroInArray(outTrueRangeArray);
                    //Log.d(TAG, "outTrueRangeArrayList.size: " + outTrueRangeArrayList.size());
                    double[] trueRangeArray = convertArrayListToArray(outTrueRangeArrayList, 1000);

                    RetCode maRetcode = core.ema(0, closePriceCopy.length - 1, closePriceCopy, timePeriod, begin, length, outMaArray);
                    outMaArrayList = removeZeroInArray(outMaArray);
                    //Log.d(TAG, "outMaArrayList.size: " + outMaArrayList.size());
                    double[] maArray = convertArrayListToArray(outMaArrayList, 1000);

                    RetCode rangeMaRetCode = core.ema(0, trueRangeArray.length - 1, trueRangeArray, atrRange, begin, length, outTREmaArray);
                    outTREmaArrayList = removeZeroInArray(outTREmaArray);
                    //Log.d(TAG, "outTREmaArrayList.size: " + outTREmaArrayList.size());
                    double[] trEmaArray = convertArrayListToArray(outTREmaArrayList, 1000);

//                Log.d(TAG, "trueRangeArray: " + trueRangeArray[trueRangeArray.length - 2]);
//                Log.d(TAG, "maArray: " + maArray[maArray.length - 2]);
//                Log.d(TAG, "trEmaArray: " + trEmaArray[trEmaArray.length - 2]);

//        double upper = maArray[maArray.length - 2] + trEmaArray[trEmaArray.length - 2] * mult;
//        double lower = maArray[maArray.length - 2] - trEmaArray[trEmaArray.length - 2] * mult;
                    // double upper = maArray[maArray.length - 1] + trEmaArray[trEmaArray.length - 1] * mult;
                    lower = maArray[maArray.length - 1] - trEmaArray[trEmaArray.length - 1] * mult;
                    //Log.d(TAG, "upper: " + upper);
//                Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
//                Log.d(TAG, "closePrice: " + closePrice[index]);
//                Log.d(TAG, "lower: " + lower);
                    //closePriceCopy[closePriceCopy.length - 1] -= decrement;
                    closePriceCopy[closePriceCopy.length - 1] = BigDecimal.valueOf(closePriceCopy[closePriceCopy.length - 1]).subtract(BigDecimal.valueOf(decrement)).doubleValue();

                    if (lowPriceCopy[lowPriceCopy.length - 1] > closePriceCopy[closePriceCopy.length - 1]) {
                        lowPriceCopy[lowPriceCopy.length - 1] = closePriceCopy[closePriceCopy.length - 1];
                    }

                    closeIndex += 1;
                    Log.d(TAG, "closeIndex: " + closeIndex);
                    Log.d(TAG, "lower: " + lower);

                    if (closeIndex == 1){
                        break;
                    }
                }

                if (lowPriceCopy[lowPriceCopy.length - 1] == closePriceCopy[closePriceCopy.length - 1]) {
                    //lowPriceCopy[lowPriceCopy.length - 1] += decrement;
                    lowPriceCopy[lowPriceCopy.length - 1] = BigDecimal.valueOf(lowPriceCopy[lowPriceCopy.length - 1]).add(BigDecimal.valueOf(decrement)).doubleValue();
                }
                //closePriceCopy[closePriceCopy.length - 1] += decrement;
                closePriceCopy[closePriceCopy.length - 1] = BigDecimal.valueOf(closePriceCopy[closePriceCopy.length - 1]).add(BigDecimal.valueOf(decrement)).doubleValue();
            }

        closePriceCopy[closePriceCopy.length - 1] = BigDecimal.valueOf(closePriceCopy[closePriceCopy.length - 1]).multiply(BigDecimal.valueOf(PRICE_OFFSET)).doubleValue();
        closePriceCopy[closePriceCopy.length - 1] = roundDouble(closePriceCopy[closePriceCopy.length - 1], maxDecrement - 1);
        BigDecimal closePriceCopyBD = BigDecimal.valueOf(closePriceCopy[closePriceCopy.length - 1]);

        double sellPrice = closePriceCopy[closePriceCopy.length - 1];
//        double buyLevel_01 = roundDouble(BigDecimal.valueOf(0.998).multiply(closePriceCopyBD).doubleValue(), maxDecrement);
//        double buyLevel_02 = roundDouble(BigDecimal.valueOf(0.9975).multiply(closePriceCopyBD).doubleValue(), maxDecrement);
//        double buyLevel_03 = roundDouble(BigDecimal.valueOf(0.9970).multiply(closePriceCopyBD).doubleValue(), maxDecrement);
//        double buyLevel_04 = roundDouble(BigDecimal.valueOf(0.9965).multiply(closePriceCopyBD).doubleValue(), maxDecrement);
//        double buyLevel_05 = roundDouble(BigDecimal.valueOf(0.9960).multiply(closePriceCopyBD).doubleValue(), maxDecrement);

        double buyLevel_01 = roundDouble(closePriceCopyBD.multiply(BigDecimal.valueOf(0.998)).doubleValue(), maxDecrement - 1);
        double buyLevel_02 = roundDouble(closePriceCopyBD.multiply(BigDecimal.valueOf(0.996)).doubleValue(), maxDecrement - 1);
        double buyLevel_03 = roundDouble(closePriceCopyBD.multiply(BigDecimal.valueOf(0.994)).doubleValue(), maxDecrement - 1);
        double buyLevel_04 = roundDouble(closePriceCopyBD.multiply(BigDecimal.valueOf(0.992)).doubleValue(), maxDecrement - 1);
        double buyLevel_05 = roundDouble(closePriceCopyBD.multiply(BigDecimal.valueOf(0.990)).doubleValue(), maxDecrement - 1);
        double buyLevel_06 = roundDouble(closePriceCopyBD.multiply(BigDecimal.valueOf(0.988)).doubleValue(), maxDecrement - 1);

        Log.d(TAG, "closePriceCopy: " + closePriceCopy[closePriceCopy.length - 1]);
        Log.d(TAG, "sellPrice: " + sellPrice);
        Log.d(TAG, "buyLevel_01: " + buyLevel_01);
        Log.d(TAG, "buyLevel_02: " + buyLevel_02);
        Log.d(TAG, "buyLevel_03: " + buyLevel_03);
        Log.d(TAG, "buyLevel_04: " + buyLevel_04);
        Log.d(TAG, "buyLevel_05: " + buyLevel_05);
        Log.d(TAG, "buyLevel_06: " + buyLevel_06);

        //priceCalculationListener.onSuccess(sellPrice, buyLevel_02, buyLevel_03, buyLevel_04);

    }

    public void calculateUpperEmaOptimization(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, PriceCalculationListener priceCalculationListener, String symbol){
        double[] closePriceCopy = Arrays.copyOfRange(closePrice, 0, 1000);
        double[] highPriceCopy = Arrays.copyOfRange(highPrice, 0, 1000);
        double[] openPriceCopy = Arrays.copyOfRange(openPrice, 0, 1000);
        Log.d(TAG, "closePriceCopy.length: " + closePriceCopy.length);
        int maxDecrement = findDecimalPlaces(closePrice);
        Log.d(TAG, "maxDecrement: " + maxDecrement);
        int closeIndex;
        boolean isOpenLessLower;
        double startingMoney = 5000.0;

        for (double mIdx = 1.0; mIdx < 2.0; mIdx += 0.01) {
            for (int lengthIdx = 2; lengthIdx < 101; lengthIdx++) {
//                for (int atrIdx = 25; atrIdx < 28; atrIdx++) {
                int negativeGainCounter = 0;
                double minPercentGain = 1.0;
                double cMinPercentGain = 1.0;

                for (int index = 60; index < 999; index++) {
                    double upper = 0.0;
                    double decrement = 100.0;
                    closePriceCopy[index] = openPriceCopy[index];
                    highPriceCopy[index] = openPriceCopy[index];
                    closeIndex = 0;
                    double percentGain = 0.0;
                    double cPercentGain = 0.0;
                    isOpenLessLower = false;

                    for (int decreIndex = 0; decreIndex <= (maxDecrement + 1); decreIndex++) {
                        decrement = BigDecimal.valueOf(decrement).divide(BigDecimal.valueOf(10.0), 10, RoundingMode.HALF_UP).doubleValue();
                        //Log.d(TAG, "decrement: " + decrement);

                        while (closePriceCopy[index] > upper) {
                            int timePeriod = lengthIdx;
//                                int atrRange = atrIdx;
                            double mult = mIdx;

                            double[] outTREmaArray = new double[closePriceCopy.length];
                            double[] outMaArray = new double[closePriceCopy.length];
                            double[] outTrueRangeArray = new double[closePriceCopy.length];
                            ArrayList<Double> outTREmaArrayList = new ArrayList<>();
                            ArrayList<Double> outMaArrayList = new ArrayList<>();
                            ArrayList<Double> outTrueRangeArrayList = new ArrayList<>();
                            Core core = new Core();
                            MInteger begin = new MInteger();
                            MInteger length = new MInteger();

                            RetCode maRetcode = core.ema(0, closePriceCopy.length - 1, closePriceCopy, timePeriod, begin, length, outMaArray);
                            outMaArrayList = removeZeroInArray(outMaArray);
                            double[] maArray = convertArrayListToArray(outMaArrayList, 1000);

                            upper = mult * maArray[index];

                            //closePriceCopy[index] -= decrement;
                            closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).add(BigDecimal.valueOf(decrement)).doubleValue();
                            if (highPriceCopy[index] < closePriceCopy[index]) {
                                highPriceCopy[index] = closePriceCopy[index];
                            }

                            closeIndex += 1;
//                    Log.d(TAG, "closeIndex: " + closeIndex);
//                    Log.d(TAG, "lower: " + lower);
                        }

                        if (highPriceCopy[index] == closePriceCopy[index]) {
                            //lowPriceCopy[lowPriceCopy.length - 1] += decrement;
                            highPriceCopy[index] = BigDecimal.valueOf(highPriceCopy[index]).subtract(BigDecimal.valueOf(decrement)).doubleValue();
                        }
                        //closePriceCopy[index] += decrement;
                        closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).subtract(BigDecimal.valueOf(decrement)).doubleValue();

                        if (closeIndex == 1 && decreIndex == (maxDecrement + 1)) {
                            isOpenLessLower = true;
//                                Log.d(TAG, "closeIndex: " + closeIndex);
//                                Log.d(TAG, "openPrice: " + openPrice[index]);
//                                Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
//                                Log.d(TAG, "lowPrice: " + lowPrice[index]);
//                                Log.d(TAG, "lower: " + lower);
                            Log.d(TAG, "openPrice > upper");
                            //closePriceCopy[index] = lower;
                            break;
                        }
                    }

                    closePriceCopy[index] = roundDouble(closePriceCopy[index], maxDecrement - 1);

                    if (closePrice[index] > closePriceCopy[index] && !isOpenLessLower) {
                        BigDecimal closePriceCopyBD = BigDecimal.valueOf(closePriceCopy[index]);
                        BigDecimal nextHighPriceBD = BigDecimal.valueOf(highPrice[index + 1]);
                        BigDecimal highPriceBD = BigDecimal.valueOf(highPrice[index]);
                        percentGain = (nextHighPriceBD.subtract(closePriceCopyBD)).divide(closePriceCopyBD, 9, RoundingMode.HALF_DOWN).doubleValue();
                        cPercentGain = (highPriceBD.subtract(closePriceCopyBD)).divide(closePriceCopyBD, 9, RoundingMode.HALF_DOWN).doubleValue();

                        if (percentGain < minPercentGain){
                            minPercentGain = percentGain;
                        }

                        if (cPercentGain < cMinPercentGain){
                            cMinPercentGain = cPercentGain;
                        }
                        Log.d(TAG, "upper: " + upper);
                        Log.d(TAG, "closePrice[index]: " + closePrice[index]);
                        Log.d(TAG, "closePriceCopy[index]: " + closePriceCopy[index]);
                        Log.d(TAG, "highPrice[index]: " + highPrice[index]);
                        Log.d(TAG, "highPrice[index + 1]: " + highPrice[index + 1]);
                        Log.d(TAG, "percentGain: " + percentGain);
                        Log.d(TAG, "cPercentGain: " + cPercentGain);

                    }
                    Log.d(TAG, "index: " + index);
                    closePriceCopy[index] = closePrice[index];
                    highPriceCopy[index] = lowPrice[index];
                }


                Log.d(TAG, "mult: " + mIdx);
                Log.d(TAG, "lengthIdx: " + lengthIdx);
//                    Log.d(TAG, "atrIdx: " + atrIdx);
                Log.d(TAG, "minPercentGain: " + minPercentGain);
                Log.d(TAG, "cMinPercentGain: " + cMinPercentGain);

            }
        }
//        }


        // TTM - Revertion to the Mean Band - INPUTS
//        z = input(0, title = "Offset")
//        usesl = input(true, title = "RTM against Slope (UCSgears Addition)", type=bool)
//        length = a == 1 ? 13 : 25
//        atrlen = a == 1 ? 13 : 25
//        mult = a == 1 ? 1.5 : 2.5
//        range =  tr
//
//// Calculations
//        ma = ema(close, length)
//        rangema = ema(range, atrlen)
//        upper = ma + rangema * mult
//        lower = ma - rangema * mult
//
//// All Plots
//        plot(upper, color=purple, title="Upper Channel", offset = z)
//        plot(ma, color=red, title="Middle Line", offset = z)
//        plot(lower, color=purple, title="Lower Channel", offset = z)
    }

//    public void monitorCurrentPrice(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, PriceCalculationListener priceCalculationListener, String symbol){
//        int maxDecrement = (findDecimalPlaces(closePrice) - 1);
//        priceCalculationListener.onSuccess(closePrice.length - 1, maxDecrement);
//    }

    public void calculateRange(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, PriceCalculationListener priceCalculationListener, String symbol){
        maxPrice = findMax(highPrice);
        minPrice = findMin(lowPrice);

        priceCalculationListener.onSuccess(maxPrice, minPrice);
    }

//    public void calculateVolumeProfile(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, PriceCalculationListener priceCalculationListener, String symbol) {
    public void calculateVolumeProfile(ArrayList<Long> timestampArrayList, ArrayList<Double> volumeArrayList, ArrayList<Double> priceArrayList, ArrayList<Long> idArrayList, HistTradeListener histTradeListener) {
        HashMap<Double, Double> volumePriceHM = new HashMap<>();
        ArrayList<Double> keySetArrayList = new ArrayList<>();
        int volumeIndex = 0;
        double poc = 0.0;
        double totalVolume = 0.0;
        double pocPrice = 0.0;
        int pocKey = 0;
        int maxDecrement = findDecimalPlaces(priceArrayList);

        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        double intervalPrice = minPrice;
        BigDecimal minPriceBD = BigDecimal.valueOf(minPrice);
        keySetArrayList.add(minPrice);

        BigDecimal intervalBD = (BigDecimal.valueOf(maxPrice).subtract(BigDecimal.valueOf(minPrice))).divide(BigDecimal.valueOf(240), 9, RoundingMode.HALF_UP);
        Log.d(TAG, "interval: " + intervalBD.doubleValue());
        Log.d(TAG, "minPrice: " + minPrice);
        Log.d(TAG, "maxPrice: " + maxPrice);

        for (int i = 0; i < 240; i++){
            intervalPrice = BigDecimal.valueOf(intervalPrice).add(intervalBD).doubleValue();
            keySetArrayList.add(intervalPrice);
            volumePriceHM.put(intervalPrice, 0.0);
        }
        Log.d(TAG, "volumeArrayList.size(): " + volumeArrayList.size());
        Log.d(TAG, "timestampArrayList.size(): " + timestampArrayList.size());
        Log.d(TAG, "priceArrayList.size(): " + priceArrayList.size());
        Log.d(TAG, "idArrayList.size(): " + idArrayList.size());
        Log.d(TAG, "volumePriceHM created");

        for (Double price : priceArrayList) {
            if (price.equals(null)){
                break;
            }
            BigDecimal priceBD = BigDecimal.valueOf(price);
            double index = (priceBD.subtract(minPriceBD)).divide(intervalBD, 0, RoundingMode.UP).doubleValue();
            Log.d(TAG, "price: " + price);
            Log.d(TAG, "index: " + index);
            Log.d(TAG, "keyPrice: " + keySetArrayList.get((int)index));

            for (int k = (int)index; k < 241; k++) {
                Log.d(TAG, "k: " + k);
                BigDecimal volumeElementBD = BigDecimal.valueOf(volumeArrayList.get(volumeIndex));
                totalVolume = BigDecimal.valueOf(totalVolume).add(volumeElementBD).doubleValue();
                //if (inRange(keySetArrayList.get(k), keySetArrayList.get(k - 1), price)){
                if (price < keySetArrayList.get(k) && price >= keySetArrayList.get(k - 1)){
                    BigDecimal prevVolumeBD = BigDecimal.valueOf(volumePriceHM.get(keySetArrayList.get(k)));
                    double curVolume = prevVolumeBD.add(volumeElementBD).doubleValue();
                    volumePriceHM.put(keySetArrayList.get(k), curVolume);

                    if (poc < curVolume){
                        pocKey = k;
                        poc = curVolume;
                    }
                    break;
                }
            }
            Log.d(TAG, "POC: " + poc);
            Log.d(TAG, "volumeIndex: " + volumeIndex);
            volumeIndex++;
        }

        pocPrice = keySetArrayList.get(pocKey);
        Log.d(TAG, "Final POC: " + poc);
        Log.d(TAG, "pocPrice: " + pocPrice);
        Log.d(TAG, "pocPriceVolume: " + volumePriceHM.get(pocPrice));

        calculateValueArea(keySetArrayList, pocKey, pocPrice, volumePriceHM, totalVolume, histTradeListener, maxDecrement);
    }

    private void calculateValueArea(ArrayList<Double> keySetArrayList, int pocKey, double pocPrice, HashMap<Double, Double> volumePriceHM, double totalVolume, HistTradeListener histTradeListener, int maxDecrement) {
        double volumeCap = BigDecimal.valueOf(0.7).multiply(BigDecimal.valueOf(totalVolume)).doubleValue();
        double volumeCounter = volumePriceHM.get(keySetArrayList.get(pocKey));
        double valueAreaHighVolume = 0.0;
        double valueAreaLowVolume = 0.0;
        int valueAreaLowIndex = pocKey;
        int valueAreaHighIndex = pocKey;
        double valueAreaHighPrice = 0.0;
        double valueAreaLowPrice = 0.0;


        while(volumeCounter < volumeCap){
            if ((valueAreaHighIndex + 2) > 241 && (valueAreaLowIndex - 2) > 0) {
                valueAreaLowVolume = BigDecimal.valueOf(volumePriceHM.get(keySetArrayList.get(valueAreaLowIndex - 1))).add(BigDecimal.valueOf(volumePriceHM.get(keySetArrayList.get(valueAreaLowIndex - 2)))).doubleValue();
                volumeCounter = BigDecimal.valueOf(volumeCounter).add(BigDecimal.valueOf(valueAreaLowVolume)).doubleValue();
                valueAreaLowIndex -= 2;
            } else if ((valueAreaLowIndex - 2) < 0 && (valueAreaHighIndex + 2) < 241){
                valueAreaHighVolume = BigDecimal.valueOf(volumePriceHM.get(keySetArrayList.get(valueAreaHighIndex + 1))).add(BigDecimal.valueOf(volumePriceHM.get(keySetArrayList.get(valueAreaHighIndex + 2)))).doubleValue();
                volumeCounter = BigDecimal.valueOf(volumeCounter).add(BigDecimal.valueOf(valueAreaHighVolume)).doubleValue();
                valueAreaHighIndex += 2;
            } else if ((valueAreaLowIndex - 2) > 0 && (valueAreaHighIndex + 2) < 241){
                valueAreaHighVolume = BigDecimal.valueOf(volumePriceHM.get(keySetArrayList.get(valueAreaHighIndex + 1))).add(BigDecimal.valueOf(volumePriceHM.get(keySetArrayList.get(valueAreaHighIndex + 2)))).doubleValue();
                valueAreaLowVolume = BigDecimal.valueOf(volumePriceHM.get(keySetArrayList.get(valueAreaLowIndex - 1))).add(BigDecimal.valueOf(volumePriceHM.get(keySetArrayList.get(valueAreaLowIndex - 2)))).doubleValue();

                if (valueAreaHighVolume >= valueAreaLowVolume){
                    volumeCounter = BigDecimal.valueOf(volumeCounter).add(BigDecimal.valueOf(valueAreaHighVolume)).doubleValue();
                    valueAreaHighIndex += 2;
                } else {
                    volumeCounter = BigDecimal.valueOf(volumeCounter).add(BigDecimal.valueOf(valueAreaLowVolume)).doubleValue();
                    valueAreaLowIndex -= 2;
                }
            }
        }

        valueAreaHighPrice = keySetArrayList.get(valueAreaHighIndex);
        valueAreaLowPrice = keySetArrayList.get(valueAreaLowIndex);
        Log.d(TAG, "valueAreaHighIndex: " + valueAreaHighIndex);
        Log.d(TAG, "valueAreaLowIndex: " + valueAreaLowIndex);
        Log.d(TAG, "valueAreaHighPrice: " + valueAreaHighPrice);
        Log.d(TAG, "valueAreaLowPrice: " + valueAreaLowPrice);

        histTradeListener.onSuccess(valueAreaHighPrice, valueAreaLowPrice, pocPrice, maxDecrement);
    }

    private int findDecimalPlaces(ArrayList<Double> closePriceArrayList){
        int maxDecimalPlace = 0;


        for (int index = closePriceArrayList.size() - 100; index < closePriceArrayList.size(); index++){
            String text = Double.toString(Math.abs(closePriceArrayList.get(index)));
            int integerPlaces = text.indexOf('.');
            int decimalPlace = text.length() - integerPlaces;

            if (maxDecimalPlace < decimalPlace){
                maxDecimalPlace = (decimalPlace - 1);
            }
        }

        return maxDecimalPlace;
    }

    private boolean inRange(double high, double low, double x){
        BigDecimal xBD = BigDecimal.valueOf(x);
        BigDecimal highBD = BigDecimal.valueOf(high);
        BigDecimal lowBD = BigDecimal.valueOf(low);
        return  ((xBD.subtract(lowBD).doubleValue()) < (highBD.subtract(lowBD).doubleValue()) && (xBD.subtract(lowBD).doubleValue()) >= 0);
    }

    private double findMin(double[] array){
        double min = array[0];

        for(double element : array){
            if (min > element){
                min = element;
            }
        }

        return min;
    }

    private double findMax(double[] array){
        double max = array[0];

        for(double element : array){
            if (max < element){
                max = element;
            }
        }

        return max;
    }

    public void calculateLowerEma(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, PriceCalculationListener priceCalculationListener, String symbol){
        double[] closePriceCopy = Arrays.copyOfRange(closePrice, 0, 1000);
        double[] lowPriceCopy = Arrays.copyOfRange(lowPrice, 0, 1000);
        double[] openPriceCopy = Arrays.copyOfRange(openPrice, 0, 1000);
        Log.d(TAG, "closePriceCopy.length: " + closePriceCopy.length);
        double maxPercentGain = 0.0;
        int maxDecrement = findDecimalPlaces(closePrice);
        Log.d(TAG, "maxDecrement: " + maxDecrement);
        int closeIndex;
        boolean isOpenLessLower;

        double lower = 0.0;
        double decrement = 100.0;
        closePriceCopy[closePrice.length - 1] = openPriceCopy[closePrice.length - 1];
        lowPriceCopy[closePrice.length - 1] = openPriceCopy[closePrice.length - 1];
        closeIndex = 0;
        double targetPrice1 = 0.0;
        double targetPrice2 = 0.0;
        double targetPrice3 = 0.0;
        double targetPrice4 = 0.0;
        double targetPrice5 = 0.0;
        double targetPrice6 = 0.0;
                    isOpenLessLower = false;
                    Log.d(TAG, "closePrice: " + closePrice[closePrice.length - 1]);
                    Log.d(TAG, "openPrice: " + closePrice[openPrice.length - 1]);

                    for (int decreIndex = 0; decreIndex <= (maxDecrement + 1); decreIndex++) {
                        decrement = BigDecimal.valueOf(decrement).divide(BigDecimal.valueOf(10.0), 10, RoundingMode.HALF_UP).doubleValue();
                        //Log.d(TAG, "decrement: " + decrement);


                        while (closePriceCopy[closePrice.length - 1] > lower) {
                            int timePeriod = 53;
//                                int atrRange = atrIdx;
                            double mult = 0.95;

                            double[] outTREmaArray = new double[closePriceCopy.length];
                            double[] outMaArray = new double[closePriceCopy.length];
                            double[] outTrueRangeArray = new double[closePriceCopy.length];
                            ArrayList<Double> outTREmaArrayList = new ArrayList<>();
                            ArrayList<Double> outMaArrayList = new ArrayList<>();
                            ArrayList<Double> outTrueRangeArrayList = new ArrayList<>();
                            Core core = new Core();
                            MInteger begin = new MInteger();
                            MInteger length = new MInteger();

//                                RetCode trueRangeRetCode = core.trueRange(0, closePriceCopy.length - 1, openPrice, lowPriceCopy, closePriceCopy, begin, length, outTrueRangeArray);
//                                outTrueRangeArrayList = removeZeroInArray(outTrueRangeArray);
//                                //Log.d(TAG, "outTrueRangeArrayList.size: " + outTrueRangeArrayList.size());
//                                double[] trueRangeArray = convertArrayListToArray(outTrueRangeArrayList, 1000);

                            RetCode maRetcode = core.ema(0, closePriceCopy.length - 1, closePriceCopy, timePeriod, begin, length, outMaArray);
                            outMaArrayList = removeZeroInArray(outMaArray);
                            //Log.d(TAG, "outMaArrayList.size: " + outMaArrayList.size());
                            double[] maArray = convertArrayListToArray(outMaArrayList, 1000);

//                                RetCode rangeMaRetCode = core.ema(0, trueRangeArray.length - 1, trueRangeArray, atrRange, begin, length, outTREmaArray);
//                                outTREmaArrayList = removeZeroInArray(outTREmaArray);
//                                //Log.d(TAG, "outTREmaArrayList.size: " + outTREmaArrayList.size());
//                                double[] trEmaArray = convertArrayListToArray(outTREmaArrayList, 1000);

//        double upper = maArray[maArray.length - 2] + trEmaArray[trEmaArray.length - 2] * mult;
//        double lower = maArray[maArray.length - 2] - trEmaArray[trEmaArray.length - 2] * mult;
                            // double upper = maArray[maArray.length - 1] + trEmaArray[trEmaArray.length - 1] * mult;
//                                lower = maArray[index] - trEmaArray[index] * mult;
                            lower = mult * maArray[closePrice.length - 1];

                            //Log.d(TAG, "upper: " + upper);
//                Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
//                Log.d(TAG, "closePrice: " + closePrice[index]);
//                Log.d(TAG, "lower: " + lower);

                            //closePriceCopy[index] -= decrement;
                            closePriceCopy[closePrice.length - 1] = BigDecimal.valueOf(closePriceCopy[closePrice.length - 1]).subtract(BigDecimal.valueOf(decrement)).doubleValue();
                            if (lowPriceCopy[closePrice.length - 1] > closePriceCopy[closePrice.length - 1]) {
                                lowPriceCopy[closePrice.length - 1] = closePriceCopy[closePrice.length - 1];
                            }

                            closeIndex += 1;
//                    Log.d(TAG, "closeIndex: " + closeIndex);
//                    Log.d(TAG, "lower: " + lower);
                        }

                        if (lowPriceCopy[closePrice.length - 1] == closePriceCopy[closePrice.length - 1]) {
                            lowPriceCopy[closePrice.length - 1] = BigDecimal.valueOf(lowPriceCopy[closePrice.length - 1]).add(BigDecimal.valueOf(decrement)).doubleValue();
                        }
                        closePriceCopy[closePrice.length - 1] = BigDecimal.valueOf(closePriceCopy[closePrice.length - 1]).add(BigDecimal.valueOf(decrement)).doubleValue();

                        if (closeIndex == 1 && decreIndex == (maxDecrement + 1)) {
                            Log.d(TAG, "openPrice < lower");
                            break;
                        }
                        Log.d(TAG, "closePriceCopy: " + closePriceCopy[closePrice.length - 1]);
                        Log.d(TAG, "lower: " + lower);
                    }

                    //closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).multiply(BigDecimal.valueOf(PRICE_OFFSET)).doubleValue();
                    closePriceCopy[closePrice.length - 1] = roundDouble(closePriceCopy[closePrice.length - 1], maxDecrement - 1);

                    if (!isOpenLessLower) {
                        Log.d(TAG, "lower: " + lower);
                        Log.d(TAG, "openPrice: " + openPrice[closePrice.length - 1]);
                        Log.d(TAG, "lowPrice: " + lowPrice[closePrice.length - 1]);
                        Log.d(TAG, "closePrice: " + closePrice[closePrice.length - 1]);
                        Log.d(TAG, "closePriceCopy: " + closePriceCopy[closePrice.length - 1]);

                        BigDecimal closePriceCopyBD = BigDecimal.valueOf(closePriceCopy[closePrice.length - 1]);
                        targetPrice1 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.998))).doubleValue();
                        targetPrice2 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.996))).doubleValue();
                        targetPrice3 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.994))).doubleValue();
                        targetPrice4 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.992))).doubleValue();
                        targetPrice5 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.990))).doubleValue();
                        targetPrice6 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.988))).doubleValue();
                        Log.d(TAG, "targetPrice1: " + targetPrice1);
                        Log.d(TAG, "targetPrice2: " + targetPrice2);
                        Log.d(TAG, "targetPrice3: " + targetPrice3);
                        Log.d(TAG, "targetPrice4: " + targetPrice4);
                        Log.d(TAG, "targetPrice5: " + targetPrice5);
                        Log.d(TAG, "targetPrice6: " + targetPrice6);

                    }

                    //priceCalculationListener.onSuccess(closePriceCopy[closePrice.length - 1], targetPrice2, targetPrice3, targetPrice4);
    }

    public void calculateLowerEmaOptimization(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, PriceCalculationListener priceCalculationListener, String symbol){
        double[] closePriceCopy = Arrays.copyOfRange(closePrice, 0, 1000);
        double[] lowPriceCopy = Arrays.copyOfRange(lowPrice, 0, 1000);
        double[] openPriceCopy = Arrays.copyOfRange(openPrice, 0, 1000);
        Log.d(TAG, "closePriceCopy.length: " + closePriceCopy.length);
        double maxPercentGain = 0.0;
        int maxDecrement = findDecimalPlaces(closePrice);
        Log.d(TAG, "maxDecrement: " + maxDecrement);
        int closeIndex;
        boolean isOpenLessLower;

        for (double mIdx = 1.0; mIdx > 0.80; mIdx -= 0.01) {
            for (int lengthIdx = 2; lengthIdx < 101; lengthIdx++) {
//                for (int atrIdx = 25; atrIdx < 28; atrIdx++) {
                double netPercentGain = 0.0;
                double netTargetGain = 0.0;
                double maxPercentDrawdown = 0.0;
                double maxPercentLoss = 0.0;
                double startingMoney = 5000.0;
                int negativeGainCounter = 0;

                for (int index = 60; index < 999; index++) {
                    double lower = 0.0;
                    double decrement = 100.0;
                    closePriceCopy[index] = openPriceCopy[index];
                    lowPriceCopy[index] = openPriceCopy[index];
                    closeIndex = 0;
                    int target1Counter = 0;
                    int target2Counter = 0;
                    int target3Counter = 0;
                    int target4Counter = 0;
                    int target5Counter = 0;
                    int target6Counter = 0;
                    isOpenLessLower = false;

                    for (int decreIndex = 0; decreIndex <= (maxDecrement + 1); decreIndex++) {
                        decrement = BigDecimal.valueOf(decrement).divide(BigDecimal.valueOf(10.0), 10, RoundingMode.HALF_UP).doubleValue();
                        //Log.d(TAG, "decrement: " + decrement);


                        while (closePriceCopy[index] > lower) {
                            int timePeriod = lengthIdx;
//                                int atrRange = atrIdx;
                            double mult = mIdx;
//                            int timePeriod = 20;
//                            int atrRange = 15;
//                            double mult = 2.5;
//                            int timePeriod = 25;
//                            int atrRange = 25;
//                            double mult = 2.5;

                            double[] outTREmaArray = new double[closePriceCopy.length];
                            double[] outMaArray = new double[closePriceCopy.length];
                            double[] outTrueRangeArray = new double[closePriceCopy.length];
                            ArrayList<Double> outTREmaArrayList = new ArrayList<>();
                            ArrayList<Double> outMaArrayList = new ArrayList<>();
                            ArrayList<Double> outTrueRangeArrayList = new ArrayList<>();
                            Core core = new Core();
                            MInteger begin = new MInteger();
                            MInteger length = new MInteger();


//                                RetCode trueRangeRetCode = core.trueRange(0, closePriceCopy.length - 1, openPrice, lowPriceCopy, closePriceCopy, begin, length, outTrueRangeArray);
//                                outTrueRangeArrayList = removeZeroInArray(outTrueRangeArray);
//                                //Log.d(TAG, "outTrueRangeArrayList.size: " + outTrueRangeArrayList.size());
//                                double[] trueRangeArray = convertArrayListToArray(outTrueRangeArrayList, 1000);

                            RetCode maRetcode = core.ema(0, closePriceCopy.length - 1, closePriceCopy, timePeriod, begin, length, outMaArray);
                            outMaArrayList = removeZeroInArray(outMaArray);
                            //Log.d(TAG, "outMaArrayList.size: " + outMaArrayList.size());
                            double[] maArray = convertArrayListToArray(outMaArrayList, 1000);

//                                RetCode rangeMaRetCode = core.ema(0, trueRangeArray.length - 1, trueRangeArray, atrRange, begin, length, outTREmaArray);
//                                outTREmaArrayList = removeZeroInArray(outTREmaArray);
//                                //Log.d(TAG, "outTREmaArrayList.size: " + outTREmaArrayList.size());
//                                double[] trEmaArray = convertArrayListToArray(outTREmaArrayList, 1000);

//        double upper = maArray[maArray.length - 2] + trEmaArray[trEmaArray.length - 2] * mult;
//        double lower = maArray[maArray.length - 2] - trEmaArray[trEmaArray.length - 2] * mult;
                            // double upper = maArray[maArray.length - 1] + trEmaArray[trEmaArray.length - 1] * mult;
//                                lower = maArray[index] - trEmaArray[index] * mult;
                            lower = mult * maArray[index];

                            //Log.d(TAG, "upper: " + upper);
//                Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
//                Log.d(TAG, "closePrice: " + closePrice[index]);
//                Log.d(TAG, "lower: " + lower);

                            //closePriceCopy[index] -= decrement;
                            closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).subtract(BigDecimal.valueOf(decrement)).doubleValue();
                            if (lowPriceCopy[index] > closePriceCopy[index]) {
                                lowPriceCopy[index] = closePriceCopy[index];
                            }

                            closeIndex += 1;
//                    Log.d(TAG, "closeIndex: " + closeIndex);
//                    Log.d(TAG, "lower: " + lower);
                        }

                        if (lowPriceCopy[index] == closePriceCopy[index]) {
                            //lowPriceCopy[lowPriceCopy.length - 1] += decrement;
                            lowPriceCopy[index] = BigDecimal.valueOf(lowPriceCopy[index]).add(BigDecimal.valueOf(decrement)).doubleValue();
                        }
                        //closePriceCopy[index] += decrement;
                        closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).add(BigDecimal.valueOf(decrement)).doubleValue();

                        if (closeIndex == 1 && decreIndex == (maxDecrement + 1)) {
                            //isOpenLessLower = true;
//                                Log.d(TAG, "closeIndex: " + closeIndex);
//                                Log.d(TAG, "openPrice: " + openPrice[index]);
//                                Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
//                                Log.d(TAG, "lowPrice: " + lowPrice[index]);
//                                Log.d(TAG, "lower: " + lower);
                            Log.d(TAG, "openPrice < lower");
                            //closePriceCopy[index] = lower;
                            break;
                        }
                    }

                    //closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).multiply(BigDecimal.valueOf(PRICE_OFFSET)).doubleValue();
                    closePriceCopy[index] = roundDouble(closePriceCopy[index], maxDecrement - 1);

                    if (lowPrice[index] < closePriceCopy[index] && !isOpenLessLower) {
                        Log.d(TAG, "lower: " + lower);
                        Log.d(TAG, "openPrice: " + openPrice[index]);
                        Log.d(TAG, "lowPrice: " + lowPrice[index]);
                        Log.d(TAG, "closePrice: " + closePrice[index]);
                        Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
                        Log.d(TAG, "next openPrice: " + openPrice[index + 1]);
                        Log.d(TAG, "next highPrice: " + highPrice[index + 1]);
                        Log.d(TAG, "next closePrice: " + closePrice[index + 1]);

                        BigDecimal highPriceBD = BigDecimal.valueOf(highPrice[index + 1]);
                        BigDecimal closePriceCopyBD = BigDecimal.valueOf(closePriceCopy[index]);
                        BigDecimal closePriceBD = BigDecimal.valueOf(closePrice[index]);
                        double percentGain = (highPriceBD.subtract(closePriceCopyBD)).divide(closePriceCopyBD, 8, RoundingMode.HALF_DOWN).doubleValue();
                        double targetPrice1 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.998))).doubleValue();
                        double targetPrice2 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.996))).doubleValue();
                        double targetPrice3 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.994))).doubleValue();
                        double targetPrice4 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.992))).doubleValue();
                        double targetPrice5 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.990))).doubleValue();
                        double targetPrice6 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.988))).doubleValue();
                        double sellPrice;
                        double netCoinQuantity = 0.0;
                        double currentMoney = startingMoney;
                        //Log.d(TAG, "targetPrice1: " + targetPrice1);

                        if (percentGain < 0) {
                            sellPrice = closePrice[index + 1];
                        } else if (closePrice[index] > closePriceCopy[index]){
                            sellPrice = closePrice[index];
                        } else {
                            sellPrice = closePriceCopy[index];
                        }

                        if (percentGain < 0) {
                            if (maxPercentLoss > percentGain){
                                maxPercentLoss = percentGain;
                            }
                            negativeGainCounter += 1;
                        } else {
                            BigDecimal targetPrice1BD = BigDecimal.valueOf(targetPrice1);
                            double tempDrawdown = (targetPrice1BD.subtract(BigDecimal.valueOf(lowPrice[index + 1]))).divide(targetPrice1BD, 9, RoundingMode.HALF_UP).doubleValue();
                            if (tempDrawdown > maxPercentDrawdown){
                                maxPercentDrawdown = tempDrawdown;
                            }
                        }

                        double target1Percent = 1.0;
                        double target2Percent = 0.0;
                        double target3Percent = 0.0;
                        double target4Percent = 0.0;
                        double target5Percent = 0.0;
                        double target6Percent = 0.0;

                        if (targetPrice1 > lowPrice[index]) {
                            Log.d(TAG, "targetPrice1 greater than low: ");
                            target1Counter += 1;
                            netCoinQuantity += (BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target1Percent))).divide(BigDecimal.valueOf(targetPrice1), 9, RoundingMode.HALF_DOWN).doubleValue();
                            currentMoney = BigDecimal.valueOf(currentMoney).subtract((BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target1Percent)))).doubleValue();
                        }

                        if (targetPrice2 > lowPrice[index]) {
                            Log.d(TAG, "targetPrice2 greater than low: ");
                            target2Counter += 1;
                            netCoinQuantity += (BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target2Percent))).divide(BigDecimal.valueOf(targetPrice2), 9, RoundingMode.HALF_DOWN).doubleValue();
                            currentMoney = BigDecimal.valueOf(currentMoney).subtract((BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target2Percent)))).doubleValue();
                        }

                        if (targetPrice3 > lowPrice[index]) {
                            Log.d(TAG, "targetPrice3 greater than low: ");
                            target3Counter += 1;
                            netCoinQuantity += (BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target3Percent))).divide(BigDecimal.valueOf(targetPrice3), 9, RoundingMode.HALF_DOWN).doubleValue();
                            currentMoney = BigDecimal.valueOf(currentMoney).subtract((BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target3Percent)))).doubleValue();
                        }

                        if (targetPrice4 > lowPrice[index]) {
                            Log.d(TAG, "targetPrice4 greater than low: ");
                            target4Counter += 1;
                            netCoinQuantity += (BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target4Percent))).divide(BigDecimal.valueOf(targetPrice4), 9, RoundingMode.HALF_DOWN).doubleValue();
                            currentMoney = BigDecimal.valueOf(currentMoney).subtract((BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target4Percent)))).doubleValue();
                        }

                        if (targetPrice5 > lowPrice[index]) {
                            Log.d(TAG, "targetPrice5 greater than low: ");
                            target5Counter += 1;
                            netCoinQuantity += (BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target5Percent))).divide(BigDecimal.valueOf(targetPrice5), 9, RoundingMode.HALF_DOWN).doubleValue();
                            currentMoney = BigDecimal.valueOf(currentMoney).subtract((BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target5Percent)))).doubleValue();
                        }

                        if (targetPrice6 > lowPrice[index]) {
                            Log.d(TAG, "targetPrice6 greater than low: ");
                            target6Counter += 1;
                            netCoinQuantity += (BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target6Percent))).divide(BigDecimal.valueOf(targetPrice6), 9, RoundingMode.HALF_DOWN).doubleValue();
                            currentMoney = BigDecimal.valueOf(currentMoney).subtract((BigDecimal.valueOf(startingMoney).multiply(BigDecimal.valueOf(target6Percent)))).doubleValue();
                        }

                        startingMoney = (BigDecimal.valueOf(netCoinQuantity).multiply(BigDecimal.valueOf(sellPrice))).add(BigDecimal.valueOf(currentMoney)).doubleValue();
                        Log.d(TAG, "percentGain: " + percentGain);
                        Log.d(TAG, "startingMoney: " + startingMoney);
                        netPercentGain += percentGain;
                    }
                    Log.d(TAG, "index: " + index);
                    closePriceCopy[index] = closePrice[index];
                    lowPriceCopy[index] = lowPrice[index];
                }

                double targetGain = startingMoney/5000;
                double targetLoss = negativeGainCounter * maxPercentDrawdown;
                netTargetGain = targetGain - targetLoss;
                Log.d(TAG, "mult: " + mIdx);
                Log.d(TAG, "lengthIdx: " + lengthIdx);
//                    Log.d(TAG, "atrIdx: " + atrIdx);
                Log.d(TAG, "negativeGainCounter: " + negativeGainCounter);
                Log.d(TAG, "maxPercentDrawdown: " + maxPercentDrawdown);
                Log.d(TAG, "netPercentGain: " + netPercentGain);
                Log.d(TAG, "maxPercentLoss: " + maxPercentLoss);
                Log.d(TAG, "targetGain: " + targetGain);
                Log.d(TAG, "targetLoss: " + targetLoss);
                Log.d(TAG, "netTargetGain: " + netTargetGain);
                Log.d(TAG, "startingMoney: " + startingMoney);
                //calculateOffsetReversalBand(highPrice, lowPrice, openPrice, closePrice, volume, maxPercentLoss, lengthIdx, atrIdx);
                if (maxPercentGain < netTargetGain) {
                    maxPercentGain = netTargetGain;
                }
            }
        }
//        }

        Log.d(TAG, "maxPercentGain: " + maxPercentGain);

        // TTM - Revertion to the Mean Band - INPUTS
//        z = input(0, title = "Offset")
//        usesl = input(true, title = "RTM against Slope (UCSgears Addition)", type=bool)
//        length = a == 1 ? 13 : 25
//        atrlen = a == 1 ? 13 : 25
//        mult = a == 1 ? 1.5 : 2.5
//        range =  tr
//
//// Calculations
//        ma = ema(close, length)
//        rangema = ema(range, atrlen)
//        upper = ma + rangema * mult
//        lower = ma - rangema * mult
//
//// All Plots
//        plot(upper, color=purple, title="Upper Channel", offset = z)
//        plot(ma, color=red, title="Middle Line", offset = z)
//        plot(lower, color=purple, title="Lower Channel", offset = z)
    }

    public void calculateReversalBandOptimization(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, PriceCalculationListener priceCalculationListener, String symbol){
        double[] closePriceCopy = Arrays.copyOfRange(closePrice, 0, 1000);
        double[] lowPriceCopy = Arrays.copyOfRange(lowPrice, 0, 1000);
        double[] openPriceCopy = Arrays.copyOfRange(openPrice, 0, 1000);
        Log.d(TAG, "closePriceCopy.length: " + closePriceCopy.length);
        double maxPercentGain = 0.0;
        int maxDecrement = findDecimalPlaces(closePrice);
        Log.d(TAG, "maxDecrement: " + maxDecrement);
        int closeIndex;
        boolean isOpenLessLower;

        for (double mIdx = 0.9; mIdx > 0.0; mIdx -= 0.1) {
            for (int lengthIdx = 1; lengthIdx < 20; lengthIdx++) {
//                for (int atrIdx = 25; atrIdx < 28; atrIdx++) {
                    double netPercentGain = 0.0;
                    double netTargetGain = 0.0;
                    double maxPercentDrawdown = 0.0;
                    double maxPercentLoss = 0.0;
                    int target1Counter = 0;
                    int target2Counter = 0;
                    int target3Counter = 0;
                    int target4Counter = 0;
                    int target5Counter = 0;
                    int target6Counter = 0;
                    int negativeGainCounter = 0;

                    for (int index = 60; index < 999; index++) {
                        double lower = 0.0;
                        double decrement = 100.0;
                        closePriceCopy[index] = openPriceCopy[index];
                        lowPriceCopy[index] = openPriceCopy[index];
                        closeIndex = 0;
                        isOpenLessLower = false;

                        for (int decreIndex = 0; decreIndex <= (maxDecrement + 1); decreIndex++) {
                            decrement = BigDecimal.valueOf(decrement).divide(BigDecimal.valueOf(10.0), 10, RoundingMode.HALF_UP).doubleValue();
                            //Log.d(TAG, "decrement: " + decrement);


                            while (closePriceCopy[index] > lower) {
                                int timePeriod = lengthIdx;
//                                int atrRange = atrIdx;
                                double mult = mIdx;
//                            int timePeriod = 20;
//                            int atrRange = 15;
//                            double mult = 2.5;
//                            int timePeriod = 25;
//                            int atrRange = 25;
//                            double mult = 2.5;

                                double[] outTREmaArray = new double[closePriceCopy.length];
                                double[] outMaArray = new double[closePriceCopy.length];
                                double[] outTrueRangeArray = new double[closePriceCopy.length];
                                ArrayList<Double> outTREmaArrayList = new ArrayList<>();
                                ArrayList<Double> outMaArrayList = new ArrayList<>();
                                ArrayList<Double> outTrueRangeArrayList = new ArrayList<>();
                                Core core = new Core();
                                MInteger begin = new MInteger();
                                MInteger length = new MInteger();


//                                RetCode trueRangeRetCode = core.trueRange(0, closePriceCopy.length - 1, openPrice, lowPriceCopy, closePriceCopy, begin, length, outTrueRangeArray);
//                                outTrueRangeArrayList = removeZeroInArray(outTrueRangeArray);
//                                //Log.d(TAG, "outTrueRangeArrayList.size: " + outTrueRangeArrayList.size());
//                                double[] trueRangeArray = convertArrayListToArray(outTrueRangeArrayList, 1000);

                                RetCode maRetcode = core.ema(0, closePriceCopy.length - 1, closePriceCopy, timePeriod, begin, length, outMaArray);
                                outMaArrayList = removeZeroInArray(outMaArray);
                                //Log.d(TAG, "outMaArrayList.size: " + outMaArrayList.size());
                                double[] maArray = convertArrayListToArray(outMaArrayList, 1000);

//                                RetCode rangeMaRetCode = core.ema(0, trueRangeArray.length - 1, trueRangeArray, atrRange, begin, length, outTREmaArray);
//                                outTREmaArrayList = removeZeroInArray(outTREmaArray);
//                                //Log.d(TAG, "outTREmaArrayList.size: " + outTREmaArrayList.size());
//                                double[] trEmaArray = convertArrayListToArray(outTREmaArrayList, 1000);

//        double upper = maArray[maArray.length - 2] + trEmaArray[trEmaArray.length - 2] * mult;
//        double lower = maArray[maArray.length - 2] - trEmaArray[trEmaArray.length - 2] * mult;
                                // double upper = maArray[maArray.length - 1] + trEmaArray[trEmaArray.length - 1] * mult;
//                                lower = maArray[index] - trEmaArray[index] * mult;
                                lower = mult * maArray[index];

                                //Log.d(TAG, "upper: " + upper);
//                Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
//                Log.d(TAG, "closePrice: " + closePrice[index]);
//                Log.d(TAG, "lower: " + lower);

                                //closePriceCopy[index] -= decrement;
                                closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).subtract(BigDecimal.valueOf(decrement)).doubleValue();
                                if (lowPriceCopy[index] > closePriceCopy[index]) {
                                    lowPriceCopy[index] = closePriceCopy[index];
                                }

                                closeIndex += 1;
//                    Log.d(TAG, "closeIndex: " + closeIndex);
//                    Log.d(TAG, "lower: " + lower);
                            }

                            if (lowPriceCopy[index] == closePriceCopy[index]) {
                                //lowPriceCopy[lowPriceCopy.length - 1] += decrement;
                                lowPriceCopy[index] = BigDecimal.valueOf(lowPriceCopy[index]).add(BigDecimal.valueOf(decrement)).doubleValue();
                            }
                            //closePriceCopy[index] += decrement;
                            closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).add(BigDecimal.valueOf(decrement)).doubleValue();

                            if (closeIndex == 1 && decreIndex == (maxDecrement + 1)) {
                                isOpenLessLower = true;
//                                Log.d(TAG, "closeIndex: " + closeIndex);
//                                Log.d(TAG, "openPrice: " + openPrice[index]);
//                                Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
//                                Log.d(TAG, "lowPrice: " + lowPrice[index]);
//                                Log.d(TAG, "lower: " + lower);
                                Log.d(TAG, "openPrice < lower");
                                //closePriceCopy[index] = lower;
                                break;
                            }
                        }

                        //closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).multiply(BigDecimal.valueOf(PRICE_OFFSET)).doubleValue();
                        closePriceCopy[index] = roundDouble(closePriceCopy[index], maxDecrement - 1);

                        if (lowPrice[index] < closePriceCopy[index] && !isOpenLessLower) {
                            Log.d(TAG, "lowPrice: " + lowPrice[index]);
                            Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
                            Log.d(TAG, "next highPrice: " + highPrice[index + 1]);
                            Log.d(TAG, "next closePrice: " + closePrice[index + 1]);

                            BigDecimal highPriceBD = BigDecimal.valueOf(highPrice[index + 1]);
                            BigDecimal closePriceCopyBD = BigDecimal.valueOf(closePriceCopy[index]);
                            double percentGain = (highPriceBD.subtract(closePriceCopyBD)).divide(closePriceCopyBD, 8, RoundingMode.HALF_DOWN).doubleValue();
                            double targetPrice1 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.998))).doubleValue();
                            double targetPrice2 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.996))).doubleValue();
                            double targetPrice3 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.994))).doubleValue();
                            double targetPrice4 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.992))).doubleValue();
                            double targetPrice5 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.990))).doubleValue();
                            double targetPrice6 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.988))).doubleValue();
                            //Log.d(TAG, "targetPrice1: " + targetPrice1);

                            if (percentGain < 0) {
                                if (maxPercentLoss > percentGain){
                                    maxPercentLoss = percentGain;
                                }
                                negativeGainCounter += 1;
                            } else {
                                BigDecimal targetPrice1BD = BigDecimal.valueOf(targetPrice1);
                                double tempDrawdown = (targetPrice1BD.subtract(BigDecimal.valueOf(lowPrice[index + 1]))).divide(targetPrice1BD, 9, RoundingMode.HALF_UP).doubleValue();
                                if (tempDrawdown > maxPercentDrawdown){
                                    maxPercentDrawdown = tempDrawdown;
                                }
                            }

                            if (targetPrice1 > lowPrice[index] && percentGain > 0) {
                                Log.d(TAG, "targetPrice1 greater than low: ");
                                target1Counter += 1;
                            }

                            if (targetPrice2 > lowPrice[index] && percentGain > 0) {
                                Log.d(TAG, "targetPrice2 greater than low: ");
                                target2Counter += 1;
                            }

                            if (targetPrice3 > lowPrice[index] && percentGain > 0) {
                                Log.d(TAG, "targetPrice3 greater than low: ");
                                target3Counter += 1;
                            }

                            if (targetPrice4 > lowPrice[index] && percentGain > 0) {
                                Log.d(TAG, "targetPrice4 greater than low: ");
                                target4Counter += 1;
                            }

                            if (targetPrice5 > lowPrice[index] && percentGain > 0) {
                                Log.d(TAG, "targetPrice5 greater than low: ");
                                target5Counter += 1;
                            }

                            if (targetPrice6 > lowPrice[index] && percentGain > 0) {
                                Log.d(TAG, "targetPrice6 greater than low: ");
                                target6Counter += 1;
                            }

                            Log.d(TAG, "percentGain: " + percentGain);
                            netPercentGain += percentGain;
                        }
                        Log.d(TAG, "index: " + index);
                        closePriceCopy[index] = closePrice[index];
                        lowPriceCopy[index] = lowPrice[index];
                    }

                    double targetGain = target1Counter * 0.0005 + target2Counter * 0.0025 + target3Counter * 0.0045 + target4Counter * 0.0065 + target5Counter * 0.0085 + target6Counter * 0.0105;
                    double targetLoss = negativeGainCounter * maxPercentDrawdown;
                    netTargetGain = targetGain - targetLoss;
                                        Log.d(TAG, "mult: " + mIdx);
                    Log.d(TAG, "lengthIdx: " + lengthIdx);
//                    Log.d(TAG, "atrIdx: " + atrIdx);

                    Log.d(TAG, "negativeGainCounter: " + negativeGainCounter);
                    Log.d(TAG, "maxPercentDrawdown: " + maxPercentDrawdown);
                    Log.d(TAG, "netPercentGain: " + netPercentGain);
                    Log.d(TAG, "maxPercentLoss: " + maxPercentLoss);
                    Log.d(TAG, "targetGain: " + targetGain);
                    Log.d(TAG, "targetLoss: " + targetLoss);
                    Log.d(TAG, "netTargetGain: " + netTargetGain);
                    //calculateOffsetReversalBand(highPrice, lowPrice, openPrice, closePrice, volume, maxPercentLoss, lengthIdx, atrIdx);
                    if (maxPercentGain < netTargetGain) {
                        maxPercentGain = netTargetGain;
                    }
                }
            }
//        }

        Log.d(TAG, "maxPercentGain: " + maxPercentGain);

        // TTM - Revertion to the Mean Band - INPUTS
//        z = input(0, title = "Offset")
//        usesl = input(true, title = "RTM against Slope (UCSgears Addition)", type=bool)
//        length = a == 1 ? 13 : 25
//        atrlen = a == 1 ? 13 : 25
//        mult = a == 1 ? 1.5 : 2.5
//        range =  tr
//
//// Calculations
//        ma = ema(close, length)
//        rangema = ema(range, atrlen)
//        upper = ma + rangema * mult
//        lower = ma - rangema * mult
//
//// All Plots
//        plot(upper, color=purple, title="Upper Channel", offset = z)
//        plot(ma, color=red, title="Middle Line", offset = z)
//        plot(lower, color=purple, title="Lower Channel", offset = z)
    }

    private void calculateOffsetReversalBand(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, double offset, int lengthIdx, int atrIdx){
        double[] closePriceCopy = Arrays.copyOfRange(closePrice, 0, 1000);
        double[] lowPriceCopy = Arrays.copyOfRange(lowPrice, 0, 1000);
        double[] openPriceCopy = Arrays.copyOfRange(openPrice, 0, 1000);
        Log.d(TAG, "closePriceCopy.length: " + closePriceCopy.length);
        double maxPercentGain = 0.0;
        int maxDecrement = findDecimalPlaces(closePrice);
        Log.d(TAG, "maxDecrement: " + maxDecrement);
        int closeIndex;
        boolean isOpenLessLower;

//        for (double mIdx = 2.0; mIdx < 3.1; mIdx += 0.1) {
//            for (int lengthIdx = 15; lengthIdx < 21; lengthIdx++) {
//                for (int atrIdx = 7; atrIdx < 21; atrIdx++) {
        double netPercentGain = 0.0;
        double netTargetGain = 0.0;
        double maxPercentDrawdown = 0.0;
        double maxPercentLoss = 0.0;
        int target1Counter = 0;
        int target2Counter = 0;
        int target3Counter = 0;
        int target4Counter = 0;
        int target5Counter = 0;
        int target6Counter = 0;
        int negativeGainCounter = 0;

        for (int index = 60; index < 999; index++) {
            double lower = 0.0;
            double decrement = 100.0;
            closePriceCopy[index] = openPriceCopy[index];
            lowPriceCopy[index] = openPriceCopy[index];
            closeIndex = 0;
            isOpenLessLower = false;

            for (int decreIndex = 0; decreIndex <= (maxDecrement + 1); decreIndex++) {
                decrement = BigDecimal.valueOf(decrement).divide(BigDecimal.valueOf(10.0), 10, RoundingMode.HALF_UP).doubleValue();
                //Log.d(TAG, "decrement: " + decrement);


                while (closePriceCopy[index] > lower) {
                                int timePeriod = lengthIdx;
                                int atrRange = atrIdx;
//                                double mult = mIdx;
//                    int timePeriod = 20;
//                    int atrRange = 15;
//                            double mult = 2.5;
//                            int timePeriod = 25;
//                            int atrRange = 25;
                    double mult = 2.5;

                    double[] outTREmaArray = new double[closePriceCopy.length];
                    double[] outMaArray = new double[closePriceCopy.length];
                    double[] outTrueRangeArray = new double[closePriceCopy.length];
                    ArrayList<Double> outTREmaArrayList = new ArrayList<>();
                    ArrayList<Double> outMaArrayList = new ArrayList<>();
                    ArrayList<Double> outTrueRangeArrayList = new ArrayList<>();
                    Core core = new Core();
                    MInteger begin = new MInteger();
                    MInteger length = new MInteger();


                    RetCode trueRangeRetCode = core.trueRange(0, closePriceCopy.length - 1, openPrice, lowPriceCopy, closePriceCopy, begin, length, outTrueRangeArray);
                    outTrueRangeArrayList = removeZeroInArray(outTrueRangeArray);
                    //Log.d(TAG, "outTrueRangeArrayList.size: " + outTrueRangeArrayList.size());
                    double[] trueRangeArray = convertArrayListToArray(outTrueRangeArrayList, 1000);

                    RetCode maRetcode = core.ema(0, closePriceCopy.length - 1, closePriceCopy, timePeriod, begin, length, outMaArray);
                    outMaArrayList = removeZeroInArray(outMaArray);
                    //Log.d(TAG, "outMaArrayList.size: " + outMaArrayList.size());
                    double[] maArray = convertArrayListToArray(outMaArrayList, 1000);

                    RetCode rangeMaRetCode = core.ema(0, trueRangeArray.length - 1, trueRangeArray, atrRange, begin, length, outTREmaArray);
                    outTREmaArrayList = removeZeroInArray(outTREmaArray);
                    //Log.d(TAG, "outTREmaArrayList.size: " + outTREmaArrayList.size());
                    double[] trEmaArray = convertArrayListToArray(outTREmaArrayList, 1000);

//        double upper = maArray[maArray.length - 2] + trEmaArray[trEmaArray.length - 2] * mult;
//        double lower = maArray[maArray.length - 2] - trEmaArray[trEmaArray.length - 2] * mult;
                    // double upper = maArray[maArray.length - 1] + trEmaArray[trEmaArray.length - 1] * mult;
                    lower = maArray[index] - trEmaArray[index] * mult;
                    //Log.d(TAG, "upper: " + upper);
//                Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
//                Log.d(TAG, "closePrice: " + closePrice[index]);
//                Log.d(TAG, "lower: " + lower);

                    //closePriceCopy[index] -= decrement;
                    closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).subtract(BigDecimal.valueOf(decrement)).doubleValue();
                    if (lowPriceCopy[index] > closePriceCopy[index]) {
                        lowPriceCopy[index] = closePriceCopy[index];
                    }

                    closeIndex += 1;
//                    Log.d(TAG, "closeIndex: " + closeIndex);
//                    Log.d(TAG, "lower: " + lower);

                }

                if (lowPriceCopy[index] == closePriceCopy[index]) {
                    //lowPriceCopy[lowPriceCopy.length - 1] += decrement;
                    lowPriceCopy[index] = BigDecimal.valueOf(lowPriceCopy[index]).add(BigDecimal.valueOf(decrement)).doubleValue();
                }
                //closePriceCopy[index] += decrement;
                closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).add(BigDecimal.valueOf(decrement)).doubleValue();

                if (closeIndex == 1 && decreIndex == (maxDecrement + 1)) {
                    isOpenLessLower = true;
//                                Log.d(TAG, "closeIndex: " + closeIndex);
//                                Log.d(TAG, "openPrice: " + openPrice[index]);
//                                Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
//                                Log.d(TAG, "lowPrice: " + lowPrice[index]);
//                                Log.d(TAG, "lower: " + lower);
                    Log.d(TAG, "openPrice < lower");
                    //closePriceCopy[index] = lower;
                    break;
                }
            }

            closePriceCopy[index] = BigDecimal.valueOf(closePriceCopy[index]).multiply(BigDecimal.valueOf(1+offset)).doubleValue();
            closePriceCopy[index] = roundDouble(closePriceCopy[index], maxDecrement - 1);

            if (lowPrice[index] < closePriceCopy[index] && !isOpenLessLower) {
                Log.d(TAG, "lowPrice: " + lowPrice[index]);
                Log.d(TAG, "closePriceCopy: " + closePriceCopy[index]);
                Log.d(TAG, "next highPrice: " + highPrice[index + 1]);
                Log.d(TAG, "next closePrice: " + closePrice[index + 1]);

                BigDecimal highPriceBD = BigDecimal.valueOf(highPrice[index + 1]);
                BigDecimal closePriceCopyBD = BigDecimal.valueOf(closePriceCopy[index]);
                double percentGain = (highPriceBD.subtract(closePriceCopyBD)).divide(closePriceCopyBD, 8, RoundingMode.HALF_DOWN).doubleValue();
                double targetPrice1 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.998))).doubleValue();
                double targetPrice2 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.996))).doubleValue();
                double targetPrice3 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.994))).doubleValue();
                double targetPrice4 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.992))).doubleValue();
                double targetPrice5 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.990))).doubleValue();
                double targetPrice6 = (closePriceCopyBD.multiply(BigDecimal.valueOf(0.988))).doubleValue();
                //Log.d(TAG, "targetPrice1: " + targetPrice1);

                if (percentGain < 0) {
                    if (maxPercentLoss > percentGain){
                        maxPercentLoss = percentGain;
                    }
                    negativeGainCounter += 1;
                } else {
                    BigDecimal targetPrice1BD = BigDecimal.valueOf(targetPrice1);
                    double tempDrawdown = (targetPrice1BD.subtract(BigDecimal.valueOf(lowPrice[index + 1]))).divide(targetPrice1BD, 9, RoundingMode.HALF_UP).doubleValue();
                    if (tempDrawdown > maxPercentDrawdown){
                        maxPercentDrawdown = tempDrawdown;
                    }
                }

                if (targetPrice1 > lowPrice[index] && percentGain > 0) {
                    Log.d(TAG, "targetPrice1 greater than low: ");
                    target1Counter += 1;
                }

                if (targetPrice2 > lowPrice[index] && percentGain > 0) {
                    Log.d(TAG, "targetPrice2 greater than low: ");
                    target2Counter += 1;
                }

                if (targetPrice3 > lowPrice[index] && percentGain > 0) {
                    Log.d(TAG, "targetPrice3 greater than low: ");
                    target3Counter += 1;
                }

                if (targetPrice4 > lowPrice[index] && percentGain > 0) {
                    Log.d(TAG, "targetPrice4 greater than low: ");
                    target4Counter += 1;
                }

                if (targetPrice5 > lowPrice[index] && percentGain > 0) {
                    Log.d(TAG, "targetPrice5 greater than low: ");
                    target5Counter += 1;
                }

                if (targetPrice6 > lowPrice[index] && percentGain > 0) {
                    Log.d(TAG, "targetPrice6 greater than low: ");
                    target6Counter += 1;
                }

                Log.d(TAG, "percentGain: " + percentGain);
                netPercentGain += percentGain;
            }
            Log.d(TAG, "index: " + index);
            closePriceCopy[index] = closePrice[index];
            lowPriceCopy[index] = lowPrice[index];
        }

        double targetGain = target1Counter * 0.0005 + target2Counter * 0.0025 + target3Counter * 0.0045 + target4Counter * 0.0065 + target5Counter * 0.0085 + target6Counter * 0.0105;
        double targetLoss = negativeGainCounter * maxPercentDrawdown;
        netTargetGain = targetGain - targetLoss;
        //                    Log.d(TAG, "mult: " + mIdx);
//                    Log.d(TAG, "lengthIdx: " + lengthIdx);
//                    Log.d(TAG, "atrIdx: " + atrIdx);

        Log.d(TAG, "negativeGainCounter: " + negativeGainCounter);
        Log.d(TAG, "maxPercentDrawdown: " + maxPercentDrawdown);
        Log.d(TAG, "netPercentGain: " + netPercentGain);
        Log.d(TAG, "maxPercentLoss: " + maxPercentLoss);
        Log.d(TAG, "targetOffsetGain: " + targetGain);
        Log.d(TAG, "targetLoss: " + targetLoss);
        Log.d(TAG, "netTargetOffsetGain: " + netTargetGain);
    }

    private int findDecimalPlaces(double[] closePrice){
        int maxDecimalPlace = 0;
        double[] closePriceCopy = Arrays.copyOfRange(closePrice, 0, 100);

        for (double price : closePriceCopy){
            String text = Double.toString(Math.abs(price));
            int integerPlaces = text.indexOf('.');
            int decimalPlace = text.length() - integerPlaces;

            if (maxDecimalPlace < decimalPlace){
                maxDecimalPlace = decimalPlace;
            }
        }

        return maxDecimalPlace;
    }

    public void calculateStochiastic(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, SmaListener smaListener, String symbol){
        double[] outSlowKArray = new double[highPrice.length];
        double[] outSlowDArray = new double[highPrice.length];
        ArrayList<Double> outSlowKArrayList = new ArrayList<>();
        ArrayList<Double> outSlowDArrayList = new ArrayList<>();
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        RetCode stochiasticRetCode = core.stoch(0, highPrice.length - 1, highPrice, lowPrice, closePrice, 14, 3, MAType.Sma, 3,
                MAType.Sma, begin, length, outSlowKArray, outSlowDArray);

        if (stochiasticRetCode == RetCode.Success) {
            outSlowKArrayList = removeZeroInArray(outSlowKArray);
            outSlowDArrayList = removeZeroInArray(outSlowDArray);
            Log.d(TAG, "SlowK: " + outSlowKArrayList.get(outSlowKArrayList.size() - 2));
            Log.d(TAG, "SlowD: " + outSlowDArrayList.get(outSlowDArrayList.size() - 2));
        }
    }

    public void calculateStochiasticRsi(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, SmaListener smaListener, String symbol){
        double[] outSlowKRsiArray = new double[highPrice.length];
        double[] outSlowDRsiArray = new double[highPrice.length];
        double[] outRsiArray = new double[highPrice.length];
        ArrayList<Double> outSlowKArrayList = new ArrayList<>();
        ArrayList<Double> outSlowDArrayList = new ArrayList<>();
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        RetCode rsiRetCode = core.rsi(0, highPrice.length - 1, closePrice, 14, begin, length, outRsiArray);
        RetCode stochiasticRsiRetCode = core.stochRsi(0, highPrice.length - 1, outRsiArray, 14, 3, 3,
                MAType.Sma, begin, length, outSlowKRsiArray, outSlowDRsiArray);
    }

    //int startIdx, int endIdx, double[] inReal, int optInTimePeriod, MInteger outBegIdx, MInteger outNBElement, double[] outReal

//    int startIdx, int endIdx, double[] inReal, int optInTimePeriod, int optInFastK_Period, int optInFastD_Period,
//    MAType optInFastD_MAType, MInteger outBegIdx, MInteger outNBElement, double[] outFastK, double[] outFastD

    public RetCode stdDev(int startIdx, int endIdx, double[] inReal, int optInTimePeriod, double optInNbDev, MInteger outBegIdx, MInteger outNBElement, double[] outReal) {
        if (startIdx < 0) {
            return RetCode.OutOfRangeStartIndex;
        } else if (endIdx >= 0 && endIdx >= startIdx) {
            if (optInTimePeriod == -2147483648) {
                optInTimePeriod = 5;
            } else if (optInTimePeriod < 2 || optInTimePeriod > 100000) {
                return RetCode.BadParam;
            }

            if (optInNbDev == -4.0E37D) {
                optInNbDev = 1.0D;
            } else if (optInNbDev < -3.0E37D || optInNbDev > 3.0E37D) {
                return RetCode.BadParam;
            }

            Core core = new Core();
            RetCode retCode = core.TA_INT_VAR(startIdx, endIdx, inReal, optInTimePeriod, outBegIdx, outNBElement, outReal);
            if (retCode != RetCode.Success) {
                return retCode;
            } else {
                int i;
                double tempReal;
                if (optInNbDev != 1.0D) {
                    for(i = 0; i < outNBElement.value; ++i) {
                        tempReal = outReal[i];
                        if (tempReal >= 1.0E-30D) {
                            outReal[i] = Math.sqrt(tempReal) * optInNbDev;
                        } else {
                            outReal[i] = 0.0D;
                        }
                    }
                } else {
                    for(i = 0; i < outNBElement.value; ++i) {
                        tempReal = outReal[i];
                        if (tempReal >= 1.0E-30D) {
                            outReal[i] = Math.sqrt(tempReal);
                        } else {
                            outReal[i] = 0.0D;
                        }
                    }
                }

                return RetCode.Success;
            }
        } else {
            return RetCode.OutOfRangeEndIndex;
        }
    }

//    public void calculateBol(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, BollingerListener adxListener, String symbol){
//        Core c = new Core();
//        MInteger begin = new MInteger();
//        MInteger length = new MInteger();
//        double[] closeSma = new double[closePrice.length];
//        double[] outRealUpperBand = new double[closePrice.length];
//        double[] outRealMiddleBand =  new double[closePrice.length];
//        double[] outRealLowerBand =  new double[closePrice.length];
//        double maxPercent = 0.0;
//        ArrayList<Double> upperBBArrayList = new ArrayList<>();
//        List<Double> lowerBandList = new ArrayList<>();
//        int optPeriodIndex = 2;
//        double optStdDev = 0.98;
//        double lowerBolPrice = 0.0;
//        double[] modClosePriceArray = closePrice;
////        modClosePriceArray[modClosePriceArray.length - 1] = 0.17500;
////        modClosePriceArray[modClosePriceArray.length - 1] = 0.1731828384;
//
////        for (int periodIndex = 1; periodIndex < 20; periodIndex++) {
////            for (double deviationIndex = 0.1; deviationIndex < 20.1; deviationIndex = (BigDecimal.valueOf(deviationIndex).add(BigDecimal.valueOf(0.01))).doubleValue()) {
//
//        //Log.d(TAG, "firstClosePrice: " + modClosePriceArray[modClosePriceArray.length - 1]);
//
////        for (double price : modClosePriceArray){
////            Log.d(TAG, "closePrice: " + price);
////        }
//        if (modClosePriceArray[modClosePriceArray.length - 1] > lowerBolPrice)
//        for (int i = 0; modClosePriceArray[modClosePriceArray.length - 1] > lowerBolPrice; i++) {
//            RetCode smaRetCode = c.sma(0, modClosePriceArray.length - 1, modClosePriceArray, optPeriodIndex, begin, length, closeSma);
//            if (smaRetCode == RetCode.Success) {
//
//                ArrayList<Double> tempCloseSmaArrayList = new ArrayList<>();
//                ArrayList<Double> closeSmaArrayList = new ArrayList<>();
//                tempCloseSmaArrayList = removeZeroInArray(modClosePriceArray);
//
//                for (int a = 0; a < (optPeriodIndex - tempCloseSmaArrayList.size()); a++) {
//                    closeSmaArrayList.add(0.0);
//                }
//
//                for (double closeD : tempCloseSmaArrayList) {
//                    closeSmaArrayList.add(closeD);
//                }
//
//                Log.d(TAG, "closeSmaArrayList.size(): " + closeSmaArrayList.size());
//
//                double[] finalSmaClose = new double[closeSmaArrayList.size()];
//                int index = 0;
//                for (double closeFinalDouble : closeSmaArrayList) {
//                    finalSmaClose[index] = closeFinalDouble;
//                    index = index + 1;
//                }
//
//                Log.d(TAG, "finalSmaClose.length: " + finalSmaClose.length);
//
//
//                RetCode bolRetCode = c.bbands(0, closePrice.length - 1, finalSmaClose, optPeriodIndex, optStdDev,
//                        optStdDev, MAType.Sma, begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);
//                double tempMaxPercent = 0.0;
////                    RetCode bolRetCode = c.bbands(0, closePrice.length - 1, finalSmaClose, periodIndex, deviationIndex,
////                            deviationIndex, MAType.Sma, begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);
//
//                if (bolRetCode == RetCode.Success) {
//                    //Lower Band = 20-day SMA - (20-day standard deviation of price x 2)
//
////                        int bBIndex = 0;
////                        Log.d(TAG, "Symbol: " + symbol);
////                        for (double lowerBB : outRealLowerBand) {
////                                Log.d(TAG, "lowerBB: " + lowerBB);
////                            bBIndex = bBIndex + 1;
////                        }
//
//                    lowerBandList = removeZeroInArray(outRealLowerBand);
//                    lowerBolPrice = lowerBandList.get(lowerBandList.size() - 1);
//                    Log.d(TAG, "lowerBolPrice: " + lowerBolPrice);
//                    Log.d(TAG, "modPrice: " + modClosePriceArray[modClosePriceArray.length - 1]);
//                    modClosePriceArray[modClosePriceArray.length - 1] = BigDecimal.valueOf(modClosePriceArray[modClosePriceArray.length - 1]).subtract(BigDecimal.valueOf(0.00001)).doubleValue();
//
//                } else {
//                    Log.d(TAG, "BolRetcodeFailed");
//                }
//            } else {
//                Log.d(TAG, "SmaRetcodeFailed");
//            }
//        }

//        double n = lowerBolPrice;
//        double roundedDouble;
//        roundedDouble = roundDouble(n,5);
//        double buyPriceTier1 = roundDouble((roundedDouble - (roundedDouble * 0.002)), 5);
//        double buyPriceTier2 = roundDouble((roundedDouble - (roundedDouble * 0.004)), 5);
//        double buyPriceTier3 = roundDouble((roundedDouble - (roundedDouble * 0.006)), 5);
//        double buyPriceTier4 = roundDouble((roundedDouble - (roundedDouble * 0.007)), 5);
//
//        Log.d(TAG, "FinalBolPrice: " + roundedDouble);
//        Log.d(TAG, "FinalModPrice(-0.2%): " + buyPriceTier1);
//        Log.d(TAG, "FinalModPrice(-0.4%): " + buyPriceTier2);
//        Log.d(TAG, "FinalModPrice(-0.6%): " + buyPriceTier3);
//        Log.d(TAG, "FinalModPrice(-0.7%): " + buyPriceTier4);

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


//        adxListener.onSuccess(roundedDouble, buyPriceTier1, buyPriceTier2, buyPriceTier3, buyPriceTier4);
//
//    }

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

    public void calculateAdxDiTest(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, PriceCalculationListener priceCalculationListener, String symbol) {
        double[] adxOutArray = new double[closePrice.length];
        double[] minusDiOutArray = new double[closePrice.length];
        double[] plusDiOutArray = new double[closePrice.length];
        double maxDiDiff = 0.0;
        ArrayList<Double> adxOutArrayList = new ArrayList<>();
        ArrayList<Double> minusDiOutArrayList = new ArrayList<>();
        ArrayList<Double> plusDiOutArrayList = new ArrayList<>();



        for (int lengthIdx = 3; lengthIdx < 30; lengthIdx++) {
            MInteger begin = new MInteger();
            MInteger length = new MInteger();
            Core c = new Core();
//            Log.d(TAG, "symbol" + symbol);
            RetCode adxRetCode = c.adx(0, closePrice.length - 1, highPrice, lowPrice, closePrice, lengthIdx, begin, length, adxOutArray);
            RetCode minusDIRetCode = c.minusDI(0, closePrice.length - 1, highPrice, lowPrice, closePrice, lengthIdx, begin, length, minusDiOutArray);
            RetCode plusDIRetCode = c.plusDI(0, closePrice.length - 1, highPrice, lowPrice, closePrice, lengthIdx, begin, length, plusDiOutArray);
            adxOutArrayList = removeZeroInArray(adxOutArray);
            minusDiOutArrayList = removeZeroInArray(minusDiOutArray);
            double[] minusDiArray = convertArrayListToArray(minusDiOutArrayList, 1000);
            plusDiOutArrayList = removeZeroInArray(plusDiOutArray);
            double[] plusDiArray = convertArrayListToArray(plusDiOutArrayList, 1000);
            boolean isPlusIntersect = false;

            if (minusDIRetCode == RetCode.Success && plusDIRetCode == RetCode.Success && adxRetCode == RetCode.Success) {
                double minPercentGain = 1.0;
                for (int index = 1; index < 999; index++) {
                    double mDi = minusDiArray[index];
                    double pDi = plusDiArray[index];
                    if (pDi > mDi && plusDiArray[index - 1] < minusDiArray[index - 1]) {
                        isPlusIntersect = true;
                    } else {
                        isPlusIntersect = false;
                    }

                    if (isPlusIntersect) {
                        Log.d(TAG, "lengthIdx: " + lengthIdx);
                        Log.d(TAG, "index: " + index);
                        Log.d(TAG, "closePrice: " + closePrice[index]);
                        Log.d(TAG, "highPrice + 1: " + highPrice[index + 1]);
                        Log.d(TAG, "highPrice + 2: " + highPrice[index + 2]);
                        Log.d(TAG, "index + 1 is p>m: " + (plusDiArray[index + 1] > minusDiArray[index + 1]));
                        Log.d(TAG, "index + 2 is p>m: " + (plusDiArray[index + 2] > minusDiArray[index + 2]));

                        double percentGain = (highPrice[index + 1] - closePrice[index]) / closePrice[index];
                        if (percentGain < minPercentGain) {
                            minPercentGain = percentGain;
                        }
                        Log.d(TAG, "percentGain: " + percentGain);

                    }
                }
                Log.d(TAG, "minPercentGain: " + minPercentGain);
            }
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

    public static double roundDouble(double x, int roundTodecimalPlace)
    {
        BigDecimal b = new BigDecimal(Double.toString(x));
        b = b.setScale(roundTodecimalPlace, BigDecimal.ROUND_HALF_DOWN);
        return b.doubleValue();
    }
}
