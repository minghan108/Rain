package rain.com.rain;

/**
 * Created by MSI\mliu on 18/04/18.
 */

public interface KlinesListener {
    void onSuccess();

    void onFailure(String response);

    void repeatKlinesRequest(long utcTimestamp);
}
