package rain.com.rain;

public interface SellListener {
    void onSuccess(Double price);

    void onFailure(String response);
}
