package rain.com.rain;

public interface HistTradeListener {
    void onSuccess(double valueAreaHighPrice, double valueAreaLowPrice, double pocPrice, int maxDecrement);

    void onResendNextRequest(Long requestId);

    void onFailure(String failureMsg);
}
