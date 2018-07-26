package rain.com.rain;

import java.util.HashMap;

public interface AccountInfoListener {
    void onSuccess(HashMap<String, Balance> balanceHashMap);

    void onFailure(String failureMsg);
}
