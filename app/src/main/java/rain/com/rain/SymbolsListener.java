package rain.com.rain;

import java.util.List;

/**
 * Created by MSI\mliu on 24/04/18.
 */

public interface SymbolsListener {
    void onSuccess(List<String> symbols);

    void onFailure(String response);
}
