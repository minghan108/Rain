package rain.com.rain;

public interface AdxListener {
    void onSuccess();

    void onFailure(String response);

    void onBuy();

    void onSell();
}
