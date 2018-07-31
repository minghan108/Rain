package rain.com.rain;

public interface BaseListener {
    void onSuccess();

    void onFailure(String failureMsg);
}
