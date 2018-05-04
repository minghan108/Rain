package rain.com.rain;

public interface BuyListener {
    void onSuccess(Double price);

    void onFailure(String response);
}
