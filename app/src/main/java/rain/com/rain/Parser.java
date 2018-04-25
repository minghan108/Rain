package rain.com.rain;

import android.util.JsonReader;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by MSI\mliu on 06/04/18.
 */

public class Parser {
    String TAG = "Parse";
    public static int index = 0;
    String[] symbolArray;
    private boolean isSymbolArrayInit = false;
    public static LinkedList<HashMap<String, String>> currentPriceHashMapLinkedList = new LinkedList<>();
    private LinkedList<Double> klinesLinkedList = new LinkedList<>();
    private double hunDayPriceAvg = 0;
    private SimpleMovingAverageExample simpleSMA = new SimpleMovingAverageExample();


    public rain.com.rain.SortedMap parseCurrentPriceJsonResponse(String response) throws IOException {

        JsonReader reader = new JsonReader(new StringReader(response));
        String jsonTimeStamp = "";
        String dateFormat = "";
        String timeFormat = "";
        HashMap<String, String> currentPriceHashMap = new HashMap<>();

        try {
            JSONArray jsonArray = new JSONArray(response);
            //JSONObject epgObject = new JSONObject(response);
//            JSONObject motaStationObject = epgObject.getJSONObject("");
//            JSONArray channelsArray = motaStationObject.getJSONArray("");
//            JSONArray programsArray = channelsArray.getJSONArray("channels");


            for (int i = 0; i < jsonArray.length(); i++) {

                String symbol = jsonArray.getJSONObject(i).getString("symbol");
                String price = jsonArray.getJSONObject(i).getString("price");
                Log.d(TAG, "symbol: " + symbol);
                Log.d(TAG, "price: " + price);
                currentPriceHashMap.put(symbol, price);

                if (!isSymbolArrayInit) {
                    if (symbolArray == null) {
                        symbolArray = new String[jsonArray.length()];
                    }
                    symbolArray[i] = symbol;
                    Log.d(TAG, "symbolArray.size(): " + symbolArray.length);
                }


            }

            isSymbolArrayInit = true;


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (index < 180) {

            currentPriceHashMapLinkedList.add(currentPriceHashMap);
            index += 1;
        } else {
            currentPriceHashMapLinkedList.clear();
            index = 0;
            currentPriceHashMapLinkedList.add(currentPriceHashMap);
        }

        HashMap<String,Double> increaseRateHashMap = new HashMap<String, Double>();
        ValueComparator bvc = new ValueComparator(increaseRateHashMap);
        TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
        TreeMap<String, Double> reverse_sorted_map = new TreeMap<String,Double>(Collections.reverseOrder(bvc));
        HashMap<String, String> firstHashMap = currentPriceHashMapLinkedList.getFirst();
        Log.d(TAG, "currentPriceHM.size: " + currentPriceHashMapLinkedList.size());
        if (currentPriceHashMapLinkedList.size() > 1){

            for (String symbol : symbolArray){
                Log.d(TAG, "symbol: " + symbol);
                Log.d(TAG, "firstHashMap.get(symbol): " + firstHashMap.get(symbol));
                Double firstPrice = Double.parseDouble(firstHashMap.get(symbol));
                Log.d(TAG, "currentPriceHashMap.get(symbol): " + currentPriceHashMap.get(symbol));
                Double secondPrice = Double.parseDouble(currentPriceHashMap.get(symbol));
                Double increaseRate = ((secondPrice - firstPrice)/firstPrice) * 100;
                Double increaseRateRounded = Math.round(increaseRate * 100.0) / 100.0;
                increaseRateHashMap.put(symbol, increaseRateRounded);
            }


            sorted_map.putAll(increaseRateHashMap);
            reverse_sorted_map.putAll(increaseRateHashMap);
            Log.d(TAG, "sortedmap key: " + sorted_map.firstKey());
            Log.d(TAG, "sortedmap value: " + sorted_map.get(sorted_map.firstKey()));
            Log.d(TAG, "sortedmap: " + sorted_map);


        }


        rain.com.rain.SortedMap sortedMap = new rain.com.rain.SortedMap(sorted_map, reverse_sorted_map);

        return sortedMap;
    }

    public void parseKlinesJsonResponse(String response, KlinesListener klinesListener){
        long utcTimeStamp = 0;

        try {
            JSONArray jsonArray = new JSONArray(response);
            Log.d(TAG, "jsonArray length: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                //Log.d(TAG, "jsonArray: " + jsonArray.getJSONArray(i).toString());
                //Log.d(TAG, "jsonArray closing price: " + jsonArray.getJSONArray(i).get(1).toString());
                //hundredDayPriceAvg += Double.parseDouble(jsonArray.getJSONArray(i).get(4).toString());
                klinesLinkedList.add(Double.parseDouble(jsonArray.getJSONArray(i).get(4).toString()));
                utcTimeStamp = Long.parseLong(jsonArray.getJSONArray(i).get(0).toString());
                hunDayPriceAvg += Double.parseDouble(jsonArray.getJSONArray(i).get(4).toString());
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (utcTimeStamp < (localToGMT() - 1800000L)){
            klinesListener.repeatKlinesRequest(utcTimeStamp + 1800000L);
        } else {
            klinesListener.onSuccess();
            //calculateSma();
            Log.d(TAG, "hunDayPriceAvg: " + hunDayPriceAvg/klinesLinkedList.size());
        }

    }

    public void parseDefaultKlinesJsonResponse(String response, KlinesListener klinesListener, String symbol){
        klinesLinkedList.clear();

        try {
            JSONArray jsonArray = new JSONArray(response);
            Log.d(TAG, "jsonArray length: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                //Log.d(TAG, "jsonArray: " + jsonArray.getJSONArray(i).toString());
                //Log.d(TAG, "jsonArray closing price: " + jsonArray.getJSONArray(i).get(1).toString());
                klinesLinkedList.add(Double.parseDouble(jsonArray.getJSONArray(i).get(4).toString()));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        calculateSma(symbol, klinesListener);

    }

    private void calculateSma(String symbol, KlinesListener klinesListener) {
        Double[] closePriceArray = klinesLinkedList.toArray(new Double[klinesLinkedList.size()]);
        double[] d = ArrayUtils.toPrimitive(closePriceArray);
        ArrayUtils.reverse(d);
        simpleSMA.calculateSimpleMovingAverage(d, klinesListener, symbol);
    }

    public static long localToGMT() {
        return (new Date().getTime());
    }

    public List<String> parseSymbolsJsonResponse(String response) {
        List<String> symbolList = new ArrayList<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < jsonArray.length(); i++) {

            try {
                String symbol = jsonArray.getJSONObject(i).getString("symbol");
                symbolList.add(symbol);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return symbolList;
    }
}

class ValueComparator implements Comparator<String> {
    Map<String, Double> base;

    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
