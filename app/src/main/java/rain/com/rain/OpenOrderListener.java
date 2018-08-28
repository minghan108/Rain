package rain.com.rain;

import java.util.List;

public interface OpenOrderListener {
    void onSuccess(List<Long> cancelOrderIdList);

    void onFailure(String failureMsg);
}
