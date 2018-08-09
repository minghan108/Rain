package rain.com.rain;

public interface PriceCalculationListener {
    void onSuccess(double price, double buyPriceTier1, double buyPriceTier2, double buyPriceTier3, double buyPriceTier4, double buyPriceTier5);

    void onFailure(String failureMsg);
}
