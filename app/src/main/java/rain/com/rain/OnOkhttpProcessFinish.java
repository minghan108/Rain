package rain.com.rain;

public interface OnOkhttpProcessFinish {

    void onHttpEvent(String response);

    void onHttpFailure(String response);
}