package rain.com.rain;

import java.util.List;

public interface StopLossListener {
    void onSuccess();

    void onFailure(String failureMsg);
}
