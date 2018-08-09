package rain.com.rain;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.JsonReader;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static rain.com.rain.MainActivity.symbolBreakoutMap;

/**
 * Created by MSI\mliu on 06/04/18.
 */

public class Parser {
    String TAG = "Parse";
    public static int index = 0;
    String[] symbolArray;
    private boolean isSymbolArrayInit = false;
    public static LinkedList<HashMap<String, String>> currentPriceHashMapLinkedList = new LinkedList<>();
    private LinkedList<Double> klinesOpenLinkedList = new LinkedList<>();
    private LinkedList<Double> klinesHighLinkedList = new LinkedList<>();
    private LinkedList<Double> klinesLowLinkedList = new LinkedList<>();
    private LinkedList<Double> klinesCloseLinkedList = new LinkedList<>();
    private LinkedList<Double> klinesVolumeLinkedList = new LinkedList<>();
    private double hunDayPriceAvg = 0;
    private SimpleMovingAverageExample simpleSMA = new SimpleMovingAverageExample();
    private AdxDmModel adxDmModel = new AdxDmModel();
    private HashMap<String, Balance> balanceHashMap = new HashMap<>();


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
                klinesCloseLinkedList.add(Double.parseDouble(jsonArray.getJSONArray(i).get(4).toString()));
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
            Log.d(TAG, "hunDayPriceAvg: " + hunDayPriceAvg/klinesCloseLinkedList.size());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void parseDefaultKlinesJsonResponse(String response, SmaListener smaListener, String symbol){
        klinesOpenLinkedList.clear();
        klinesCloseLinkedList.clear();
        klinesHighLinkedList.clear();
        klinesLowLinkedList.clear();
        klinesVolumeLinkedList.clear();

        try {
            JSONArray jsonArray = new JSONArray(response);
            Log.d(TAG, "jsonArray length: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                //Log.d(TAG, "jsonArray: " + jsonArray.getJSONArray(i).toString());
                //Log.d(TAG, "jsonArray closing price: " + jsonArray.getJSONArray(i).get(4).toString());
                klinesOpenLinkedList.add(Double.parseDouble(jsonArray.getJSONArray(i).get(1).toString()));
                klinesHighLinkedList.add(Double.parseDouble(jsonArray.getJSONArray(i).get(2).toString()));
                klinesLowLinkedList.add(Double.parseDouble(jsonArray.getJSONArray(i).get(3).toString()));
                klinesCloseLinkedList.add(Double.parseDouble(jsonArray.getJSONArray(i).get(4).toString()));
                klinesVolumeLinkedList.add(Double.parseDouble(jsonArray.getJSONArray(i).get(5).toString()));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        calculateSma(symbol, smaListener);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void calculateSma(String symbol, SmaListener smaListener) {
        Log.d(TAG, "calculateSma");
        Double[] openPriceArray = klinesOpenLinkedList.toArray(new Double[klinesOpenLinkedList.size()]);
        Double[] highPriceArray = klinesHighLinkedList.toArray(new Double[klinesHighLinkedList.size()]);
        Double[] lowPriceArray = klinesLowLinkedList.toArray(new Double[klinesLowLinkedList.size()]);
        Double[] closePriceArray = klinesCloseLinkedList.toArray(new Double[klinesCloseLinkedList.size()]);
        Double[] volumePriceArray = klinesVolumeLinkedList.toArray(new Double[klinesVolumeLinkedList.size()]);
        Log.d(TAG, "klinesOpenLinkedList.Size: " + klinesOpenLinkedList.size());
        Log.d(TAG, "klinesHighLinkedList.Size: " + klinesHighLinkedList.size());
        Log.d(TAG, "klinesLowLinkedList.Size: " + klinesLowLinkedList.size());
        Log.d(TAG, "klinesCloseLinkedList.Size: " + klinesCloseLinkedList.size());
        Log.d(TAG, "klinesVolumeLinkedList.Size: " + klinesVolumeLinkedList.size());

        double[] openPricePrimArray = ArrayUtils.toPrimitive(openPriceArray);
        double[] highPricePrimArray = ArrayUtils.toPrimitive(highPriceArray);
        double[] lowPricePrimArray = ArrayUtils.toPrimitive(lowPriceArray);
        double[] closePricePrimArray = ArrayUtils.toPrimitive(closePriceArray);
        double[] volumePricePrimArray = ArrayUtils.toPrimitive(volumePriceArray);
//        ArrayUtils.reverse(highPricePrimArray);
//        ArrayUtils.reverse(lowPricePrimArray);
//        ArrayUtils.reverse(closePricePrimArray);
//        ArrayUtils.reverse(volumePricePrimArray);
//        simpleSMA.calculateSimpleMovingAverage(highPricePrimArray, lowPricePrimArray, closePricePrimArray, volumePricePrimArray, klinesListener, symbol);


        //adxDmModel.calculateSupportResistance(highPricePrimArray, lowPricePrimArray, closePricePrimArray, volumePricePrimArray, adxListener, symbol);
        //adxDmModel.calculatePumpPercent(highPricePrimArray, lowPricePrimArray, openPricePrimArray, closePricePrimArray, volumePricePrimArray, smaListener, symbol);
        //adxDmModel.calculateSma(highPricePrimArray, lowPricePrimArray, openPricePrimArray, closePricePrimArray, volumePricePrimArray, smaListener, symbol);
        //adxDmModel.calculateRSI(highPricePrimArray, lowPricePrimArray, openPricePrimArray, closePricePrimArray, volumePricePrimArray, smaListener, symbol);
        //adxDmModel.calculateWaveC(highPricePrimArray, lowPricePrimArray, openPricePrimArray, closePricePrimArray, volumePricePrimArray, smaListener, symbol);
        //adxDmModel.calculateReversalBand(highPricePrimArray, lowPricePrimArray, openPricePrimArray, closePricePrimArray, volumePricePrimArray, smaListener, symbol);
        adxDmModel.calculateReversalBandOptimization(highPricePrimArray, lowPricePrimArray, openPricePrimArray, closePricePrimArray, volumePricePrimArray, smaListener, symbol);
        //adxDmModel.calculatePSar(highPricePrimArray, lowPricePrimArray, openPricePrimArray, closePricePrimArray, volumePricePrimArray, smaListener, symbol);
        //adxDmModel.calculateStochiastic(highPricePrimArray, lowPricePrimArray, openPricePrimArray, closePricePrimArray, volumePricePrimArray, smaListener, symbol);
//        adxDmModel.calculateBol(highPricePrimArray, lowPricePrimArray, closePricePrimArray, volumePricePrimArray, adxListener, symbol);
        //adxDmModel.calculateBolOptimization(highPricePrimArray, lowPricePrimArray, closePricePrimArray, volumePricePrimArray, adxListener, symbol);
        //adxDmModel.calculateAdxDi(highPricePrimArray, lowPricePrimArray, closePricePrimArray, volumePricePrimArray, adxListener, symbol);

        //adxDmModel.calculateBolCLoseOptimization(highPricePrimArray, lowPricePrimArray, closePricePrimArray, volumePricePrimArray, adxListener, symbol);

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
                if (symbol.contains("BTC") && !symbol.contains("HOT")){
                    symbolList.add(symbol);
                    symbolBreakoutMap.put(symbol, false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return symbolList;
    }

    public void parseSendBuyResponse(String response, BuyListener buyListener) {
        Log.d(TAG, "BuyResponse: " + response);

        try {
            JSONObject obj = new JSONObject(response);

            JSONArray jsonArray = obj.getJSONArray("asks");
            Log.d(TAG, "jsonArray length: " + jsonArray.length());
            Log.d(TAG, "askPrice JsonArray: " + jsonArray.get(0));
            jsonArray = (JSONArray)jsonArray.get(0);
            Log.d(TAG, "askPrice: " + jsonArray.get(0));
            Double askPriceDouble = Double.parseDouble(jsonArray.get(0).toString());
            buyListener.onSuccess(askPriceDouble);


        } catch (JSONException e) {
            buyListener.onFailure("Failure when parsing SendBuyResponse");
            e.printStackTrace();
        }
    }

    public void parseSendSellResponse(String response, BuyListener buyListener) {
        Log.d(TAG, "SellResponse: " + response);

        try {
            JSONObject obj = new JSONObject(response);

            JSONArray jsonArray = obj.getJSONArray("bids");
            Log.d(TAG, "jsonArray length: " + jsonArray.length());
            Log.d(TAG, "askPrice JsonArray: " + jsonArray.get(0));
            jsonArray = (JSONArray)jsonArray.get(0);
            Log.d(TAG, "askPrice: " + jsonArray.get(0));
            Double askPriceDouble = Double.parseDouble(jsonArray.get(0).toString());
            buyListener.onSuccess(askPriceDouble);


        } catch (JSONException e) {
            buyListener.onFailure("Failure when parsing SendBuyResponse");
            e.printStackTrace();
        }
    }

    public Long parseServerTimeResponse(String response, ServerTimeListener serverTimeListener) {
        Log.d(TAG, "SeverTimeResponse: " + response);
        Long serverTime = 0L;

        try {
            JSONObject obj = new JSONObject(response);
            serverTime = obj.getLong("serverTime");
            Log.d(TAG, "serverTime: " + serverTime);
            //serverTimeListener.onSuccess(serverTime);


        } catch (JSONException e) {
            serverTimeListener.onFailure("Failure when parsing SendBuyResponse");
            e.printStackTrace();
        }

        return serverTime;

    }

    public HashMap<String, Balance> parseAccountInfoJsonResponse(String response, AccountInfoListener accountInfoListener) {
        try {
            balanceHashMap.clear();
            JSONObject obj = new JSONObject(response);
            JSONArray jsonArray = obj.getJSONArray("balances");

            for(int i=0;i<jsonArray.length();i++)
            {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String asset = jsonObject1.optString("asset");
                double freeCoin = jsonObject1.optDouble("free");
                Balance balance = new Balance();
                balance.setAsset(asset);
                balance.setFreeCoin(freeCoin);
                balanceHashMap.put(asset, balance);
            }

            return balanceHashMap;
        } catch (JSONException e) {
            accountInfoListener.onFailure("Failure when parsing parseAccountInfoJsonResponse");
            e.printStackTrace();
            return null;
        }

    }

    public List<Long> parseCheckOpenOrderResponse(String response, OpenOrderListener openOrderListener) {
        List<Long> cancelBuyOrderIdList = new ArrayList<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < jsonArray.length(); i++) {

            try {
                String side = jsonArray.getJSONObject(i).optString("side");
                if (side.equalsIgnoreCase("BUY")){
                    cancelBuyOrderIdList.add(jsonArray.getJSONObject(i).optLong("orderId"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return cancelBuyOrderIdList;
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
