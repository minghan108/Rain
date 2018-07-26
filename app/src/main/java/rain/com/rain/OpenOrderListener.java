package rain.com.rain;

import java.util.List;

public interface OpenOrderListener {
    void onSuccess(List<Long> cancelBuyOrderIdList);

    void onFailure(String failureMsg);
}
