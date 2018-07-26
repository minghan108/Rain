package rain.com.rain;

public class Balance {
    private String asset = "";
    private double freeCoin = 0.0;

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public double getFreeCoin() {
        return freeCoin;
    }

    public void setFreeCoin(Double freeCoin) {
        this.freeCoin = freeCoin;
    }
}
