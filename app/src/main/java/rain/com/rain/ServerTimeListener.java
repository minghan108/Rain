package rain.com.rain;

public interface ServerTimeListener {
    void onSuccess(Long serverTime);

    void onFailure(String failureMsg);
}
