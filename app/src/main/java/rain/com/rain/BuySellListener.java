package rain.com.rain;

public interface BuySellListener extends BaseListener{

    void onSuccess(double price);

    void onFailure(String failureMsg);
}
