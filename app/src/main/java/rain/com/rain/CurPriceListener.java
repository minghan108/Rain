package rain.com.rain;

import java.util.TreeMap;

public interface CurPriceListener {
    void onSuccess(double curPrice);

    void onFailure(String failMessage);
}
