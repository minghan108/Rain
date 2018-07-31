package rain.com.rain;

import java.util.ArrayList;

/**
 * Created by MSI\mliu on 18/04/18.
 */

public interface KlinesListener {
    void onSuccess(ArrayList<Double> closePriceArrayList);

    void onFailure(String response);

}
