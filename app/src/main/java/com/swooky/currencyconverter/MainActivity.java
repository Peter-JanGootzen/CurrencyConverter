package com.swooky.currencyconverter;

import android.content.SharedPreferences;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;




public class MainActivity extends AppCompatActivity {

    int backpress;
    boolean mainInitiated;
    boolean €to$status = true;
    double €to$rate;
    double anwser;
    String anwserString;
    double doubleDollar;
    double doubleEuro;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        mainInitiated = settings.getBoolean("storedMainInitiated", false);
        if (mainInitiated) {
            setContentView(R.layout.activity_main);
            DownloadTask task = new DownloadTask();
            task.execute("http://api.fixer.io/latest");
        } else {
            setContentView(R.layout.activity_titlescreen);
            editor.putBoolean("storedMainInitiated", false).commit();
        }
    }

    @Override
    public void onBackPressed() {
        backpress = (backpress + 1);
        if (backpress == 1) {
            Toast.makeText(this, " Press Back again to Exit ", Toast.LENGTH_SHORT).show();

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

            try {

                JSONObject rates = new JSONObject(result);

                String USD = rates.getJSONObject("rates").getString("USD");

                Log.i("USD", USD);

                €to$rate = Double.parseDouble(USD);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    public void calculate(View view) {
        EditText editTextEuro = (EditText) findViewById(R.id.editTextEuro);
        EditText editTextDollar = (EditText) findViewById(R.id.editTextDollar);
        if (€to$status) {
            if (editTextEuro.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, "You need to enter a number in the Euro field", Toast.LENGTH_LONG).show();
            } else {
                if (€to$rate == 0) {
                    Toast.makeText(MainActivity.this, "The exchange rates have not been downloaded, please wait for 10 seconds or restart the app ", Toast.LENGTH_SHORT).show();
                }
                else {
                    doubleEuro = Double.valueOf(editTextEuro.getText().toString());
                    anwser = doubleEuro * €to$rate;
                    anwserString = String.valueOf(anwser);
                    editTextDollar.setText(anwserString);
                }
            }
        } else {
            if (editTextDollar.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, "You need to enter a number in the Dollar field", Toast.LENGTH_LONG).show();
            } else {
                if (€to$rate == 0) {
                    Toast.makeText(MainActivity.this, "The exchange rates have not been downloaded, please wait for 10 seconds or restart the app ", Toast.LENGTH_SHORT).show();
                }
                else {
                    doubleDollar = Double.valueOf(editTextDollar.getText().toString());
                    anwser = doubleDollar / €to$rate;
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

    public void titleScreenAdvance(View view) {
        setContentView(R.layout.activity_main);
        SharedPreferences settings = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("storedMainInitiated", true).commit();
    }
}

