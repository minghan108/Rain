package rain.com.rain;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static rain.com.rain.SimpleMovingAverageExample.outputText;

public class MainActivity extends AppCompatActivity {
    KlinesManager klinesManager = new KlinesManager();
    TextView rateTextView;
    TextView reverseRateTextView;
    String TAG = "MainActivity";
    Timer timer;
    private List<String> symbolsList = new ArrayList<>();
    private int symbolsIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rateTextView = (TextView)this.findViewById(R.id.RatesTextView);
        reverseRateTextView = (TextView)this.findViewById(R.id.ReverseRatesTextView);
        Log.d(TAG, "timestamp: " + localToGMT());
        rateTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    public static long localToGMT() {
        return new Date().getTime()/1000;
    }

    public static long localToGMTOffset() {
        return (new Date().getTime() - 8674560000L);
//        return (new Date().getTime() - 8760960000L);
    }

    @Override
    public void onResume(){
        super.onResume();
        sendGetSymbolsRequest();
        //sendCurrentPriceRequest();
        //resendKlinesRequest();
        //sendKlinesRequest(localToGMTOffset());
        //sendDefKlinesRequest(symbol);
    }

    private void sendDefKlinesRequest(String symbol) {
        final KlinesListener klinesListener = new KlinesListener() {
            @Override
            public void onSuccess() {
                symbolsIndex += 1;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rateTextView.setText(outputText);
                    }
                });

                if (symbolsIndex < symbolsList.size()) {
                    sendDefKlinesRequest(symbolsList.get(symbolsIndex));
                } else {
                    symbolsIndex = 0;
                    sendDefKlinesRequest(symbolsList.get(symbolsIndex));
                }
            }

            @Override
            public void onFailure(String response) {
                displayFailMessage(response);
            }

            @Override
            public void repeatKlinesRequest(long utcTimestamp) {
            }
        };

        klinesManager.sendDefaultKlinesRequest(klinesListener, symbol);
    }

    private void sendKlinesRequest(long utcTimestampOffset) {
        final KlinesListener klinesListener = new KlinesListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String response) {

            }

            @Override
            public void repeatKlinesRequest(long utcTimestamp) {
                sendKlinesRequest(utcTimestamp);
            }
        };

        klinesManager.sendKlinesRequest(klinesListener, utcTimestampOffset);

    }

    private void sendGetSymbolsRequest() {
        final SymbolsListener symbolsListener = new SymbolsListener() {

            @Override
            public void onSuccess(List<String> symbols) {
                symbolsList = symbols;

                sendDefKlinesRequest(symbolsList.get(symbolsIndex));

            }

            @Override
            public void onFailure(String response) {

            }
        };

        klinesManager.sendGetSymbolsRequest(symbolsListener);

    }

    private void sendCurrentPriceRequest() {
        CurrentPriceListener currentPriceListener = new CurrentPriceListener(){

            @Override
            public void onSuccess(final TreeMap<String, Double> sorted_map, final TreeMap<String, Double> reverse_sorted_map) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rateTextView.setText(sorted_map.toString());
                        reverseRateTextView.setText(reverse_sorted_map.toString());
                    }
                });
            }

            @Override
            public void onFailure(String failMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rateTextView.setText("Initializing");
                        reverseRateTextView.setText("Initializing");
                    }
                });
            }

            @Override
            public void repeatKlinesRequest(long utcTimestamp) {

            }
        };

        klinesManager.sendPriceRequest(currentPriceListener);

    }

    private void resendKlinesRequest() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                sendCurrentPriceRequest();
            }

        }, 0, 10000);
    }

    public void displayFailMessage(final String failMessage){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setMessage(failMessage);
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Dismiss",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });

    }
}
