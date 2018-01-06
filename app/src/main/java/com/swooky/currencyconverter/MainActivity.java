package com.swooky.currencyconverter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    int backpress;
    boolean currencyListsInitialized;

    boolean leftToRightStatus = true;

    private HashMap<String, Double> rates;

    public String activeCurrencyLeft = "EUR";
    public String activeCurrencyRight = "USD";
    public ListView currencyListLeft;
    public ListView currencyListRight;
    public TextView fromCurrency;
    public TextView toCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rates = new HashMap();

        SharedPreferences settings = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        boolean mainInitiated = settings.getBoolean("storedMainInitiated", false);

        if (mainInitiated) { // als die Boolean true is, deze is true als de gebruiker wel eens op continue heeft gedrukt op het title scherm
            setupMain();
        } else {
            setContentView(R.layout.activity_titlescreen); // zet de layout naar de titlescreen
        }

        fromCurrency = (TextView) findViewById(R.id.vanCurrency);
        toCurrency = (TextView) findViewById(R.id.toCurrency);
    }

    public void titleScreenAdvance(View view) { // deze method wordt uitgevoerd als je op de "Continue" knop klikt op het title scherm
        SharedPreferences settings = getSharedPreferences("prefs", 0); // initializeerd de preferences
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("storedMainInitiated", true).commit(); // zet de Boolean storedMainInitiated naar true, deze wordt gebruikt om te kijken of al wel eens op continue hebt gedrukt om zo het title scherm over te slaan

        setupMain();
    }

    public void setupMain() {
        setContentView(R.layout.activity_main);  // zet de layout naar de layout met de converter

        downloadRates();

        fromCurrency = (TextView) findViewById(R.id.vanCurrency);
        toCurrency = (TextView) findViewById(R.id.toCurrency);
        // Setup the editTexts
        TextView editText2 = (EditText) findViewById(R.id.editTextLeft);
        TextView editText1 = (EditText) findViewById(R.id.editTextRight);

        editText1.setOnEditorActionListener(new TextView.OnEditorActionListener() { // dit wordt uitgevoerd als je in het linker text veldje aan het typen bent en je drukt op enter op je toetsenbord
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
                    calculate(null);
                return false;
            }
        });
        editText2.setOnEditorActionListener(new TextView.OnEditorActionListener() { // executing calculate when you press enter while typing in editTextRight
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
                    calculate(null);
                return false;
            }
        });

        loadAds();
    }

    public void initiateCurrencyLists() {
        currencyListLeft = (ListView) findViewById(R.id.currencyL);
        currencyListRight = (ListView) findViewById(R.id.currencyR);

        ArrayList<String> currenciesList = new ArrayList<String>();

        for (String currencyName : rates.keySet())
            currenciesList.add(currencyName);

        Collections.sort(currenciesList);

        ArrayAdapter<String> currencyListLeftAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, currenciesList);
        currencyListLeft.setAdapter(currencyListLeftAdapter);

        ArrayAdapter<String> currencyListRightAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, currenciesList);
        currencyListRight.setAdapter(currencyListRightAdapter);

        // Left
        currencyListLeft.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activeCurrencyLeft = (String) currencyListLeft.getAdapter().getItem(position);
                fromCurrency.setText(activeCurrencyLeft);
                currencyListLeft.setVisibility(View.INVISIBLE);
                fromCurrency.setVisibility(View.VISIBLE);
                downloadRates();
            }
        });

        // Right
        currencyListRight.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activeCurrencyRight = (String) currencyListRight.getAdapter().getItem(position);
                toCurrency.setText(activeCurrencyRight);
                currencyListRight.setVisibility(View.INVISIBLE);
                toCurrency.setVisibility(View.VISIBLE);
                downloadRates();
            }
        });
        currencyListsInitialized = true;
    }

    public void loadAds() { // testing ad
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);
    }

    public void downloadRates() { // In deze method worden de wisselkoersen gedownload
        if (isNetworkAvailable()) { // Als je internet hebt
            DownloadTask task = new DownloadTask(); // download dan de wisselkoersen voor dollar
            task.execute("http://api.fixer.io/latest?base=" + activeCurrencyLeft);
        } else { // als je geen internet hebt
            Toast.makeText(MainActivity.this, "You are not connected the internet,", Toast.LENGTH_SHORT).show();  // komt er een pop-up dat je geen internet hebt
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                try {
                    JSONObject JSONResult = new JSONObject(result); // Hier wordt een object gemaakt van de result String
                    JSONObject currenciesRates = JSONResult.getJSONObject("rates");
                    JSONArray currencyNames = currenciesRates.names();
                    for (int i = 0; i < currenciesRates.length(); i++) {
                        String currentCurrencyName = currencyNames.getString(i);
                        Double currentCurrencyRate = new Double(currenciesRates.getString(currencyNames.getString(i)));
                        rates.put(currentCurrencyName, currentCurrencyRate);
                    }
                    rates.put(JSONResult.getString("base"), -1.0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void openCurrencyListLeft(View view) {
        if (!currencyListsInitialized)
            initiateCurrencyLists();
        fromCurrency.setVisibility(View.INVISIBLE);
        currencyListLeft.setVisibility(View.VISIBLE);
    }

    public void openCurrencyListRight(View view) {
        if (!currencyListsInitialized)
            initiateCurrencyLists();
        toCurrency.setVisibility(View.INVISIBLE);
        currencyListRight.setVisibility(View.VISIBLE);
    }

    public void calculate(View view) {
        EditText editText1 = (EditText) findViewById(R.id.editTextLeft);
        EditText editText2 = (EditText) findViewById(R.id.editTextRight);

        if (rates.isEmpty()) // If the hashmap is empty then the rates are not downloaded
            Toast.makeText(MainActivity.this, "Error.", Toast.LENGTH_SHORT).show();
        else if (activeCurrencyRight == activeCurrencyLeft)
            Toast.makeText(MainActivity.this, "You need to choose two different currencies.", Toast.LENGTH_SHORT).show();
        else if (leftToRightStatus) {
            if (editText1.getText().toString().equals("")) {//Als het veld leeg is dan geeft hij een foutmelding omdat je een waarde in moet geven.
                Toast.makeText(MainActivity.this, "You need to enter a number in the " + activeCurrencyLeft + " field.", Toast.LENGTH_LONG).show();
            } else {
                double doubleEuro = Double.valueOf(editText1.getText().toString());
                double answer = doubleEuro * rates.get(activeCurrencyRight);
                answer = (double) Math.round(answer * 100d) / 100d;
                String answerString = String.valueOf(answer);
                editText2.setText(answerString);
            }
        } else {
            if (editText2.getText().toString().equals("")) {//hier doe je het tegenovergestelde als in het vorige gedeelte
                Toast.makeText(MainActivity.this, "You need to enter a number in the " + activeCurrencyRight + " field.", Toast.LENGTH_LONG).show();//hij Kijkt of er iets is ingevuld.
            } else {
                double doubleDollar = Double.valueOf(editText2.getText().toString());
                double answer = doubleDollar / rates.get(activeCurrencyRight);//Hier gebeurd het tegenovergestelde van het vorige want je deelt nu door de koers. Maar daarna zet hij het weer in een string en zie je het weer op het display.
                answer = (double) Math.round(answer * 100d) / 100d;
                String answerString = String.valueOf(answer);
                editText1.setText(answerString);
            }
        }
    }

    public void switchCurrency(View view) {//dit is de functie voor het pijltje, je kan hier selecteren welke kant hij operekent.
        Button currencySwitch = (Button) findViewById(R.id.currencySwitch);
        if (leftToRightStatus) {
            currencySwitch.setText("<");
            leftToRightStatus = false;
        } else {
            currencySwitch.setText(">");
            leftToRightStatus = true;
        }
    }

    @Override
    public void onBackPressed() {
        backpress++;
        if (backpress > 0)
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
        else if (backpress > 1)
            this.finish();
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                backpress = 0;
            }
        }, 3000);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

