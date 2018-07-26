package rain.com.rain;

public interface CancelBuyOrderListener {
    void onSuccess();

    void onFailure(String failureMsg);
}
