package com.swooky.currencyconverter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;




public class MainActivity extends AppCompatActivity {

    int backpress;
    boolean mainInitiated;
    boolean €to$status = true;

    double USDRate;

    double anwser;
    String anwserString;
    double doubleDollar;
    double doubleEuro;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mainInitiated = settings.getBoolean("storedMainInitiated", false);


        if (mainInitiated) {
            setContentView(R.layout.activity_main);
            startDownloadIfNetworkTrue();

        } else {
            setContentView(R.layout.activity_titlescreen);
        }
    }

    @Override
    public void onBackPressed() {
        backpress = (backpress + 1);
        if (backpress == 1) {
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();

        }
        if (backpress > 1) {
            this.finish();
        }
        mHandler.postDelayed(new Runnable() {
            public void run() {
                backpress = 0;
            }
        }, 3000);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void startDownloadIfNetworkTrue(){

        if (isNetworkAvailable()) {
            Toast.makeText(MainActivity.this, "Starting to download latest conversion rates.", Toast.LENGTH_SHORT).show();
            DownloadTask task = new DownloadTask();
            task.execute("http://api.fixer.io/latest");
        }

        else {
            //Toast.makeText(MainActivity.this, "You are not connected the internet,", Toast.LENGTH_SHORT).show();

            actualRates();
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
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("rates.json", Context.MODE_PRIVATE));
                    outputStreamWriter.write(result);
                    outputStreamWriter.close();


                    /* JSONObject rates = new JSONObject(result);


                    String USDRateTemp = rates.getJSONObject("rates").getString("USD");
                    Log.i("USD", USDRateTemp);
                    USDRate = Double.parseDouble(USDRateTemp);  */

                    Log.i("it works", "it works");
                    Log.i("it works",result);
                }
                catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }

                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            Toast.makeText(MainActivity.this, "Downloading has finished and the conversion rates have been stored locally.", Toast.LENGTH_SHORT).show();
            actualRates();


        }
    }

    public String offlineRates() {

        String ret = "";

        try {
            InputStream inputStream = openFileInput("rates.json");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            Toast.makeText(MainActivity.this, "For first time use, you need to be connected to the internet.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;

    }

    public void actualRates() {
        try {

            JSONObject rates = new JSONObject(offlineRates());


            String AUDRateTemp = rates.getJSONObject("rates").getString("AUD");
            Log.i("AUD", AUDRateTemp);
            double AUDRate = Double.parseDouble(AUDRateTemp);

            String BGNRateTemp = rates.getJSONObject("rates").getString("BGN");
            Log.i("BGN", BGNRateTemp);
            double BGNRate = Double.parseDouble(BGNRateTemp);

            String BRLRateTemp = rates.getJSONObject("rates").getString("BRL");
            Log.i("BRL", BRLRateTemp);
            double BRLRate = Double.parseDouble(BRLRateTemp);

            String CADRateTemp = rates.getJSONObject("rates").getString("CAD");
            Log.i("CAD", CADRateTemp);
            double CADRate = Double.parseDouble(CADRateTemp);

            String CHFRateTemp = rates.getJSONObject("rates").getString("CHF");
            Log.i("CHF", CHFRateTemp);
            double CHFRate = Double.parseDouble(CHFRateTemp);

            String CNYRateTemp = rates.getJSONObject("rates").getString("CNY");
            Log.i("CNY", CNYRateTemp);
            double CNYRate = Double.parseDouble(CNYRateTemp);

            String CZKRateTemp = rates.getJSONObject("rates").getString("CZK");
            Log.i("CZK", CZKRateTemp);
            double CZKRate = Double.parseDouble(CZKRateTemp);

            String DKKRateTemp = rates.getJSONObject("rates").getString("DKK");
            Log.i("DKK", DKKRateTemp);
            double DKKRate = Double.parseDouble(DKKRateTemp);

            String GBPRateTemp = rates.getJSONObject("rates").getString("GBP");
            Log.i("GBP", GBPRateTemp);
            double GBPRate = Double.parseDouble(GBPRateTemp);

            String HKDRateTemp = rates.getJSONObject("rates").getString("HKD");
            Log.i("HKD", HKDRateTemp);
            double HKDRate = Double.parseDouble(HKDRateTemp);

            String HRKRateTemp = rates.getJSONObject("rates").getString("HRK");
            Log.i("HRK", HRKRateTemp);
            double HRKRate = Double.parseDouble(HRKRateTemp);

            String HUFRateTemp = rates.getJSONObject("rates").getString("HUF");
            Log.i("HUF", HUFRateTemp);
            double HUFRate = Double.parseDouble(HUFRateTemp);

            String IDRRateTemp = rates.getJSONObject("rates").getString("IDR");
            Log.i("IDR", IDRRateTemp);
            double IDRRate = Double.parseDouble(IDRRateTemp);

            String ILSRateTemp = rates.getJSONObject("rates").getString("ILS");
            Log.i("ILS", ILSRateTemp);
            double ILSRate = Double.parseDouble(ILSRateTemp);

            String INRRateTemp = rates.getJSONObject("rates").getString("INR");
            Log.i("INR", INRRateTemp);
            double INRRate = Double.parseDouble(INRRateTemp);

            String JPYRateTemp = rates.getJSONObject("rates").getString("JPY");
            Log.i("JPY", JPYRateTemp);
            double JPYRate = Double.parseDouble(JPYRateTemp);

            String KRWRateTemp = rates.getJSONObject("rates").getString("KRW");
            Log.i("KRW", KRWRateTemp);
            double KRWRate = Double.parseDouble(KRWRateTemp);

            String MXNRateTemp = rates.getJSONObject("rates").getString("MXN");
            Log.i("MXN", MXNRateTemp);
            double MXNRate = Double.parseDouble(MXNRateTemp);

            String MYRRateTemp = rates.getJSONObject("rates").getString("MYR");
            Log.i("MYR", MYRRateTemp);
            double MYRRate = Double.parseDouble(MYRRateTemp);

            String NOKRateTemp = rates.getJSONObject("rates").getString("NOK");
            Log.i("NOK", NOKRateTemp);
            double NOKRate = Double.parseDouble(NOKRateTemp);

            String NZDRateTemp = rates.getJSONObject("rates").getString("NZD");
            Log.i("NZD", NZDRateTemp);
            double NZDRate = Double.parseDouble(NZDRateTemp);

            String PHPRateTemp = rates.getJSONObject("rates").getString("PHP");
            Log.i("PHP", PHPRateTemp);
            double PHPRate = Double.parseDouble(PHPRateTemp);

            String PLNRateTemp = rates.getJSONObject("rates").getString("PLN");
            Log.i("PLN", PLNRateTemp);
            double PLNRate = Double.parseDouble(PLNRateTemp);

            String RONRateTemp = rates.getJSONObject("rates").getString("RON");
            Log.i("RON", RONRateTemp);
            double RONRate = Double.parseDouble(RONRateTemp);


            String RUBRateTemp = rates.getJSONObject("rates").getString("RUB");
            Log.i("RUB", RUBRateTemp);
            double RUBRate = Double.parseDouble(RUBRateTemp);


            String SEKRateTemp = rates.getJSONObject("rates").getString("SEK");
            Log.i("SEK", SEKRateTemp);
            double SEKRate = Double.parseDouble(SEKRateTemp);


            String SGDRateTemp = rates.getJSONObject("rates").getString("SGD");
            Log.i("SGD", SGDRateTemp);
            double SGDRate = Double.parseDouble(SGDRateTemp);


            String THBRateTemp = rates.getJSONObject("rates").getString("THB");
            Log.i("THB", THBRateTemp);
            double THBRate = Double.parseDouble(THBRateTemp);


            String TRYRateTemp = rates.getJSONObject("rates").getString("TRY");
            Log.i("TRY", TRYRateTemp);
            double TRYRate = Double.parseDouble(TRYRateTemp);


            String USDRateTemp = rates.getJSONObject("rates").getString("USD");
            Log.i("USD", USDRateTemp);
            USDRate = Double.parseDouble(USDRateTemp);


            String ZARRateTemp = rates.getJSONObject("rates").getString("ZAR");
            Log.i("ZAR", ZARRateTemp);
            double ZARRate = Double.parseDouble(ZARRateTemp);



        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void calculate(View view) {
        EditText editTextEuro = (EditText) findViewById(R.id.editTextEuro);
        EditText editTextDollar = (EditText) findViewById(R.id.editTextDollar);
        if (€to$status) {
            if (editTextEuro.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, "You need to enter a number in the Euro field.", Toast.LENGTH_LONG).show();
            } else {
                if (USDRate == 0) {
                    Toast.makeText(MainActivity.this, "The exchange rates have not yet been downloaded, press the refresh button.", Toast.LENGTH_SHORT).show();
                }
                else {
                    doubleEuro = Double.valueOf(editTextEuro.getText().toString());
                    anwser = doubleEuro * USDRate;
                    anwserString = String.valueOf(anwser);
                    editTextDollar.setText(anwserString);
                }
            }
        } else {
            if (editTextDollar.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, "You need to enter a number in the Dollar field.", Toast.LENGTH_LONG).show();
            } else {
                if (USDRate == 0) {
                    Toast.makeText(MainActivity.this, "The exchange rates have not yet been downloaded, press the refresh button.", Toast.LENGTH_SHORT).show();
                }
                else {
                    doubleDollar = Double.valueOf(editTextDollar.getText().toString());
                    anwser = doubleDollar / USDRate;
                    anwserString = String.valueOf(anwser);
                    editTextEuro.setText(anwserString);
                }
            }
        }
    }

    public void switchCurrency(View view) {
        Button currencySwitch = (Button) findViewById(R.id.currencySwitch);
        if (€to$status) {
            currencySwitch.setText("<");
            €to$status = false;
        } else {
            currencySwitch.setText(">");
            €to$status = true;
        }
    }

    public void retryDownload(View view) {
        if (isNetworkAvailable()) {
            Toast.makeText(MainActivity.this, "Starting to download current conversion rates.", Toast.LENGTH_SHORT).show();
            DownloadTask task = new DownloadTask();
            task.execute("http://api.fixer.io/latest");
        }

        else {
            Toast.makeText(MainActivity.this, "You are not connected to the internet.", Toast.LENGTH_SHORT).show();
        }
    }

    public void titleScreenAdvance(View view) {
        setContentView(R.layout.activity_main);
        SharedPreferences settings = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("storedMainInitiated", true).commit();
        startDownloadIfNetworkTrue();
    }
}

