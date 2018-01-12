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
    boolean firstDownloadFinished;

    boolean leftToRightStatus = true;

    private HashMap<String, Double> rates;

    public String activeCurrencyLeft = "EUR";
    public String activeCurrencyRight = "USD";
    public ListView currencyListLeft;
    public ListView currencyListRight;
    public TextView selectedCurrencyLeft;
    public TextView selectedCurrencyRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rates = new HashMap();

        SharedPreferences settings = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        boolean mainInitiated = settings.getBoolean("storedMainInitiated", false);

        if (mainInitiated) {
            setupMain();
        } else {
            setContentView(R.layout.activity_titlescreen);
        }

        selectedCurrencyLeft = findViewById(R.id.currencyLeft);
        selectedCurrencyRight = findViewById(R.id.currencyRight);
    }

    public void titleScreenAdvance(View view) {
        // Set mainInitiated to true
        SharedPreferences settings = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("storedMainInitiated", true).commit();

        setupMain();
    }

    public void setupMain() {
        setContentView(R.layout.activity_main);

        downloadRates();

        // Setup the editTexts
        selectedCurrencyLeft = findViewById(R.id.currencyLeft);
        selectedCurrencyRight = findViewById(R.id.currencyRight);
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

    // The initializing of the currencyLists are done when the user clicks on the selectedCurrency for the first time this session
    // This cannot be done in setupMain() because downloadRates() runs async, therefore it might not have finished downloading the rates and available currencies
    public void initiateCurrencyLists() {
        currencyListLeft = findViewById(R.id.currencyListLeft);
        currencyListRight = findViewById(R.id.currencyListRight);

        ArrayList<String> currencyList = new ArrayList<String>();
        for (String currencyName : rates.keySet())
            currencyList.add(currencyName);
        Collections.sort(currencyList);

        // Left
        ArrayAdapter<String> currencyListLeftAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, currencyList);
        currencyListLeft.setAdapter(currencyListLeftAdapter);
        currencyListLeft.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activeCurrencyLeft = (String) currencyListLeft.getAdapter().getItem(position);
                selectedCurrencyLeft.setText(activeCurrencyLeft);
                currencyListLeft.setVisibility(View.INVISIBLE);
                selectedCurrencyLeft.setVisibility(View.VISIBLE);
                downloadRates();
            }
        });
        // Right
        ArrayAdapter<String> currencyListRightAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, currencyList);
        currencyListRight.setAdapter(currencyListRightAdapter);
        currencyListRight.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activeCurrencyRight = (String) currencyListRight.getAdapter().getItem(position);
                selectedCurrencyRight.setText(activeCurrencyRight);
                currencyListRight.setVisibility(View.INVISIBLE);
                selectedCurrencyRight.setVisibility(View.VISIBLE);
                downloadRates();
            }
        });

        currencyListsInitialized = true;
    }

    private boolean baseOpenCurrencyList() {
        if (!firstDownloadFinished) {
            Toast.makeText(MainActivity.this, "The first download has not yet finished\nPlease wait a few seconds", Toast.LENGTH_LONG).show();
            return false;
        } else if (!currencyListsInitialized) {
            initiateCurrencyLists();
        }
        return true;
    }

    public void openCurrencyListLeft(View view) {
        if (baseOpenCurrencyList()) {
            selectedCurrencyLeft.setVisibility(View.INVISIBLE);
            currencyListLeft.setVisibility(View.VISIBLE);
        }
    }

    public void openCurrencyListRight(View view) {
        if (baseOpenCurrencyList()) {
            selectedCurrencyRight.setVisibility(View.INVISIBLE);
            currencyListRight.setVisibility(View.VISIBLE);
        }
    }

    public void loadAds() { // testing ad
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);
    }

    public void downloadRates() {
        if (isNetworkAvailable()) {
            DownloadTask task = new DownloadTask();
            task.execute("http://api.fixer.io/latest?base=" + activeCurrencyLeft);
        } else {
            Toast.makeText(MainActivity.this, "You are not connected the internet,", Toast.LENGTH_SHORT).show();
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
                    JSONObject JSONResult = new JSONObject(result);
                    JSONObject currencyRates = JSONResult.getJSONObject("rates");
                    JSONArray currencyNames = currencyRates.names();
                    for (int i = 0; i < currencyRates.length(); i++) {
                        String currentCurrencyName = currencyNames.getString(i);
                        Double currentCurrencyRate = new Double(currencyRates.getString(currencyNames.getString(i)));
                        rates.put(currentCurrencyName, currentCurrencyRate);
                    }
                    rates.put(JSONResult.getString("base"), -1.0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                firstDownloadFinished = true;
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void calculate(View view) {
        EditText editText1 = findViewById(R.id.editTextLeft);
        EditText editText2 = findViewById(R.id.editTextRight);

        if (rates.isEmpty()) // If the hashmap is empty then the rates are not downloaded
            Toast.makeText(MainActivity.this, "Error.", Toast.LENGTH_SHORT).show();
        else if (activeCurrencyRight == activeCurrencyLeft)
            Toast.makeText(MainActivity.this, "You need to choose two different currencies.", Toast.LENGTH_SHORT).show();
        else if (leftToRightStatus) {
            if (editText1.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, "You need to enter a number in the " + activeCurrencyLeft + " field.", Toast.LENGTH_LONG).show();
            } else {
                double doubleEuro = Double.valueOf(editText1.getText().toString());
                double answer = doubleEuro * rates.get(activeCurrencyRight);
                answer = (double) Math.round(answer * 100d) / 100d;
                String answerString = String.valueOf(answer);
                editText2.setText(answerString);
            }
        } else {
            if (editText2.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, "You need to enter a number in the " + activeCurrencyRight + " field.", Toast.LENGTH_LONG).show();//hij Kijkt of er iets is ingevuld.
            } else {
                double doubleDollar = Double.valueOf(editText2.getText().toString());
                double answer = doubleDollar / rates.get(activeCurrencyRight);
                answer = (double) Math.round(answer * 100d) / 100d;
                String answerString = String.valueOf(answer);
                editText1.setText(answerString);
            }
        }
    }

    public void switchCurrency(View view) {
        Button currencySwitch = findViewById(R.id.currencySwitch);
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
        if (backpress > 1)
            this.finish();
        else if (backpress > 0)
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
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

