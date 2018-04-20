package rain.com.rain;

import java.util.TreeMap;

/**
 * Created by MSI\mliu on 06/04/18.
 */

public interface CurrentPriceListener {
    void onSuccess(TreeMap<String, Double> sorted_map, TreeMap<String, Double> reverse_sorted_map);

    void onFailure(String failMessage);

    void repeatKlinesRequest(long utcTimestamp);

}
