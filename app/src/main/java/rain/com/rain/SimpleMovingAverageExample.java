package rain.com.rain;

import android.util.Log;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

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

    public void calculateSimpleMovingAverage(double[] closePrice) {
        //double[] closePrice = new double[TOTAL_PERIODS];
        double[] out = new double[closePrice.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        Core c = new Core();
        RetCode retCode = c.sma(0, closePrice.length -1, closePrice, 47, begin, length, out);


        if (retCode == RetCode.Success) {
//            Log.d(TAG, "Output Start Period: " + begin.value);
//            Log.d(TAG, "Output End Period: " + (begin.value + length.value - 1));

//            for (int i = begin.value; i < begin.value + length.value; i++) {
//                StringBuilder line = new StringBuilder();
//                line.append("Period #");
//                line.append(i);
//                line.append(" close=");
//                line.append(closePrice[i]);
//                line.append(" mov_avg=");
//                line.append(out[i - begin.value]);
//                Log.d(TAG, line.toString());
//            }
            for(Double d : out)
            Log.d(TAG, "avg: " + d);

        }
        else {
            Log.d(TAG, "Error");
        }
    }
}