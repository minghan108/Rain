package rain.com.rain;

public interface AdxListener {
    void onSuccess(String displayString);

    void onFailure(String response);

    void onBuy();

    void onSell();
}
