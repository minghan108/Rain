package rain.com.rain;

import java.util.ArrayList;

public interface HistTradeListener {
    void onSuccess(ArrayList<Long> timestampArrayList, ArrayList<Double> volumeArrayList, ArrayList<Double> priceArrayList, ArrayList<Long> idArrayList);

    void onResendNextRequest(Long requestId);

    void onFailure(String failureMsg);
}
