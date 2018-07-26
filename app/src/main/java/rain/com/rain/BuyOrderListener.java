package rain.com.rain;

public interface BuyOrderListener {
    void onSuccess();

    void onFailure(String failureMsg);
}
