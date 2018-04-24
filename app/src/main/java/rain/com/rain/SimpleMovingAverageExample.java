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
    private String symbol = "";
    public static String outputText = "";

    private enum SmaBeginState{
        BEGIN_STATE_SEVEN_BELOW_ALL,
        BEGIN_STATE_SEVEN_BELOW_TWENTYFIVE,
        BEGIN_STATE_DEFAULT
    }

    private enum SmaEndState{
        END_STATE_SEVEN_ABOVE_ALL,
        END_STATE_SEVEN_BELOW_TWENTYFIVE,
        END_STATE_DEFAULT
    }

    public SimpleMovingAverageExample(String symbol){
        this.symbol = symbol;
    }

    public void calculateSimpleMovingAverage(double[] closePrice, KlinesListener klinesListener) {
        //double[] closePrice = new double[TOTAL_PERIODS];
        double[] sevenOutArray = new double[closePrice.length];
        double[] twentyFiveOutArray = new double[closePrice.length];
        double[] hundredOutArray = new double[closePrice.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        SmaBeginState smaBeginState = SmaBeginState.BEGIN_STATE_DEFAULT;
        SmaEndState smaEndState = SmaEndState.END_STATE_DEFAULT;

        Core c = new Core();
        RetCode sevenRetCode = c.sma(0, closePrice.length -1, closePrice, 7, begin, length, sevenOutArray);
        RetCode twentyFiveRetCode = c.sma(0, closePrice.length -1, closePrice, 25, begin, length, twentyFiveOutArray);
        RetCode hundredRetCode = c.sma(0, closePrice.length -1, closePrice, 99, begin, length, hundredOutArray);


        if (sevenRetCode == RetCode.Success && twentyFiveRetCode == RetCode.Success && hundredRetCode == RetCode.Success) {
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

            for (int i = 0; i < 4; i++){
                Log.d(TAG, "sevenOutArray: " + sevenOutArray[i]);
                Log.d(TAG, "twentyFiveOutArray: " + twentyFiveOutArray[i]);
                Log.d(TAG, "hundredOutArray: " + hundredOutArray[i]);

            }

            if (sevenOutArray[4] < twentyFiveOutArray[4] && sevenOutArray[4] < hundredOutArray[4]){
                smaBeginState = SmaBeginState.BEGIN_STATE_SEVEN_BELOW_ALL;
            } else if (sevenOutArray[4] < twentyFiveOutArray[4] && sevenOutArray[4] > hundredOutArray[4]){
                smaBeginState = SmaBeginState.BEGIN_STATE_SEVEN_BELOW_TWENTYFIVE;
            }

            if (sevenOutArray[0] > twentyFiveOutArray[4] && sevenOutArray[4] > hundredOutArray[4]){
                smaEndState = SmaEndState.END_STATE_SEVEN_ABOVE_ALL;
            } else if (sevenOutArray[4] < twentyFiveOutArray[4] && sevenOutArray[4] > hundredOutArray[4]){
                smaEndState = SmaEndState.END_STATE_SEVEN_BELOW_TWENTYFIVE;
            }

            if (smaBeginState == SmaBeginState.BEGIN_STATE_SEVEN_BELOW_ALL && smaEndState == SmaEndState.END_STATE_SEVEN_ABOVE_ALL){
                Log.d(TAG, symbol + " Uptrend Cross");
                outputText += symbol + " Uptrend Cross";
                klinesListener.onSuccess();
            }

        }
        else {
            Log.d(TAG, "Error");
        }
    }
}