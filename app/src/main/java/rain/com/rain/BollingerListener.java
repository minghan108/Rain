package rain.com.rain;

public interface BollingerListener {
    void onSuccess(double bollingerPrice, double buyPriceTier1, double buyPriceTier2, double buyPriceTier3, double buyPriceTier4);

    void onFailure(String failureMsg);
}
