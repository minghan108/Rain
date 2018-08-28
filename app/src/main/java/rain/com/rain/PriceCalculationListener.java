package rain.com.rain;

public interface PriceCalculationListener {
    void onSuccess(double maxPrice, double minPrice);

    void onFailure(String failureMsg);
}
