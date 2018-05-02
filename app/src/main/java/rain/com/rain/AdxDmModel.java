package rain.com.rain;

import android.util.Log;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;

public class AdxDmModel {
    private String TAG = "SimpleMovingAverage ";


    public void calculateAdxDm(double[] highPrice, double[] lowPrice, double[] closePrice, double[] volume, KlinesListener klinesListener, String symbol) {
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
