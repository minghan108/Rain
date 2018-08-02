package rain.com.rain;

import android.util.Log;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static rain.com.rain.MainActivity.buyState;
import static rain.com.rain.MainActivity.currentDiState;
import static rain.com.rain.MainActivity.initialDiState;
import static rain.com.rain.MainActivity.isFirstLaunch;
import static rain.com.rain.MainActivity.isFirstScanComplete;
import static rain.com.rain.MainActivity.isMinusDiGreater;
import static rain.com.rain.MainActivity.pumpHashMap;
import static rain.com.rain.MainActivity.sellRemainderState;
import static rain.com.rain.MainActivity.symbolBreakoutMap;

public class AdxDmModel {
    private String TAG = "AdxDmModel ";
    private boolean isFirstLaunch = true;


    public void calculatePumpPercent(double[] highPrice, double[] lowPrice, double[] openPrice, double[] closePrice, double[] volume, SmaListener smaListener, String symbol){
        Core core = new Core();
        double minPrice = 0.0;
        int minPriceIndex = 0;
        double deltaPercent = 0.0;
        BigDecimal closePriceBD;
        BigDecimal minPriceBD;

        for (int j = 0; j < 3; j++){
            if (closePrice[j] > minPrice){
                minPrice = closePrice[j];
                minPriceIndex = j;
            }
        }

        if (openPrice[openPrice.length - 1] > minPrice){
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
