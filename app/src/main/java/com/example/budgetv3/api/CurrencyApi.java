package com.example.budgetv3.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CurrencyApi {
    private static final String API_KEY = "3fdadd9c8416675327e7ce97"; // You'll paste your API key here
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private static final String PAIR_CONVERSION_URL = "/pair/";

    public interface CurrencyCallback {
        void onSuccess(List<String> currencies);
        void onError(String error);
    }

    public interface ConversionCallback {
        void onSuccess(double convertedAmount);
        void onError(String error);
    }

    public static void getCurrencies(CurrencyCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                URL url = new URL(BASE_URL + API_KEY + "/codes");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                List<String> currencies = new ArrayList<>();
                
                // Parse the supported_codes array
                if (jsonResponse.has("supported_codes")) {
                    for (int i = 0; i < jsonResponse.getJSONArray("supported_codes").length(); i++) {
                        String code = jsonResponse.getJSONArray("supported_codes")
                                .getJSONArray(i).getString(0);
                        String name = jsonResponse.getJSONArray("supported_codes")
                                .getJSONArray(i).getString(1);
                        currencies.add(code + " - " + name);
                    }
                }

                handler.post(() -> callback.onSuccess(currencies));

            } catch (Exception e) {
                handler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public static void convertCurrency(String fromCurrency, String toCurrency, double amount, ConversionCallback callback) {
        Log.d("CurrencyApi", "Converting " + amount + " from " + fromCurrency + " to " + toCurrency);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // First get the conversion rate
                String rateUrl = BASE_URL + API_KEY + PAIR_CONVERSION_URL + fromCurrency + "/" + toCurrency + "/1";
                Log.d("CurrencyApi", "Getting rate from URL: " + rateUrl);
                URL url = new URL(rateUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                double conversionRate = jsonResponse.getDouble("conversion_result");
                Log.d("CurrencyApi", "Conversion rate from " + fromCurrency + " to " + toCurrency + ": " + conversionRate);

                // Validate the conversion rate
                if (conversionRate <= 0) {
                    throw new IllegalStateException("Invalid conversion rate: " + conversionRate);
                }

                // Calculate the converted amount
                double convertedAmount = amount * conversionRate;
                Log.d("CurrencyApi", "Converted " + amount + " " + fromCurrency + " to " + convertedAmount + " " + toCurrency);

                handler.post(() -> callback.onSuccess(convertedAmount));

            } catch (Exception e) {
                String error = e.getMessage();
                Log.e("CurrencyApi", "Error converting currency: " + error, e);
                handler.post(() -> callback.onError(error));
            }
        });
    }
}
