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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    int backpress;
    boolean mainInitiated;
    boolean €to$status = true;
    double anwser;
    String anwserString;
    double doubleDollar;
    double doubleEuro;
    private Handler mHandler = new Handler(); // Met een handler kan je een dingen plannen op een thread, bijv. dan kan je een method maken met na vijf seconden laat een pop-up zien

    public double AUDRate;
    public double BGNRate;
    public double BRLRate;
    public double CADRate;
    public double CHFRate;
    public double CNYRate;
    public double CZKRate;
    public double DKKRate;
    public double GBPRate;
    public double HKDRate;
    public double HRKRate;
    public double HUFRate;
    public double IDRRate;
    public double ILSRate;
    public double INRRate;
    public double JPYRate;
    public double KRWRate;
    public double MXNRate;
    public double MYRRate;
    public double NOKRate;
    public double NZDRate;
    public double PHPRate;
    public double PLNRate;
    public double RONRate;
    public double SEKRate;
    public double SGDRate;
    public double THBRate;
    public double TRYRate;
    public double USDRate;
    public double ZARRate;
    public double RUBRate;
    public double EURRate;

    public String activeCurrency2 = "EUR";
    public ListView Currencies;
    public ListView Currencies3;
    public boolean listViewTrue = false;

    public boolean listViewTrue2 = false;
    public TextView vanCurrency2;
    public TextView toCurrency2;
    public String activeCurrency = "USD";
    public TextView vanCurrency;
    public TextView toCurrency;
    public boolean activeEdittext;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate is een ingebouwde method die wordt uitgevoerd als je de app opstart
        super.onCreate(savedInstanceState);
        SharedPreferences settings = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mainInitiated = settings.getBoolean("storedMainInitiated", false); // haalt de Boolean storedMainInitiated uit het geheugen

        if (mainInitiated) { // als die Boolean true is, deze is true als de gebruiker wel eens op continue heeft gedrukt op het title scherm
            setContentView(R.layout.activity_main);  // zet de layout naar de layout met de converter
            startDownloadIfNetworkTrue(); //start het downloaden van de actieve currency's
            loadAds(); // start het laden van de advertentie

            TextView editText1;
            TextView editText2;
            editText1 = (EditText)findViewById(R.id.editTextDollar);
            editText2 = (EditText)findViewById(R.id.editTextEuro);
            editText1.setOnEditorActionListener(new TextView.OnEditorActionListener() { // dit wordt uitgevoerd als je in het linker text veldje aan het typen bent en je drukt op enter op je toetsenbord
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        calculate(null); // hij voert calculate uit
                    }
                    return false;
                }
            });

            editText2.setOnEditorActionListener(new TextView.OnEditorActionListener() { // dit wordt uitgevoerd als je in het rechter text veldje aan het typen bent en je drukt op enter op je toetsenbord
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        calculate(null); // hij voert calculate uit
                    }
                    return false;
                }
            });


        } else { // als de gebruiker nog nooit op continue heeft geklikt
            setContentView(R.layout.activity_titlescreen); // zet de layout naar de titlescreen
            loadAds(); // start het laden van de advertentie's

        }


        vanCurrency = (TextView)findViewById(R.id.vancurrency);


        toCurrency = (TextView)findViewById(R.id.tocurrency);
    }

    public void loadAds() { // laad een test advertentie op het scherm, op de Play store versie is dit natuurlijk een echte advertentie

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onBackPressed() { //onBackPress is een ingebouwde Android Method die wordt uitgevoerd als je op de terug knop drukt
        backpress ++; // als je een keer klikt dan wordt backpress 1 hoger
        if (backpress == 1) { // en wordt een pop-up laten zien dat als je nog een keer klikt je de app afsluit
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();

        }
        if (backpress > 1) { // de app wordt afgesloten als de gebruiker meer dan een keer heeft geklikt
            this.finish();
        }
        mHandler.postDelayed(new Runnable() {
            public void run() {
                backpress = 0;
            }
        }, 3000); // na 3000 milli seconden wordt backpress weer nul gemaakt.
    }

    private boolean isNetworkAvailable() { // deze method kijkt of je internet hebt en returnt dat
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void startDownloadIfNetworkTrue() { // In deze method worden de wisselkoersen gedownload

        if (isNetworkAvailable()) { // Als je internet hebt
            if (activeCurrency2 == "EUR") { // en Euro de actieve valuta is
                DownloadTask task = new DownloadTask();
                task.execute("http://api.fixer.io/latest"); // download dan de wisselkoersen voor euro
            } else if (activeCurrency2 == "USD") { // en Dollar de actieve valuta is
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=USD"); // download dan de wisselkoersen voor dollar
            } else if (activeCurrency2 == "AUD") { // enz.
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=AUD"); // enz.
            } else if (activeCurrency2 == "BGN") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=BGN");
            } else if (activeCurrency2 == "BRL") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=BRL");
            } else if (activeCurrency2 == "CAD") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=CAD");
            } else if (activeCurrency2 == "CHF") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=CHF");
            } else if (activeCurrency2 == "CNY") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=CNY");
            } else if (activeCurrency2 == "CZK") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=CZK");
            } else if (activeCurrency2 == "DKK") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=DKK");
            } else if (activeCurrency2 == "GBP") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=GBP");
            } else if (activeCurrency2 == "HKD") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=HKD");
            } else if (activeCurrency2 == "HRK") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=HRK");
            } else if (activeCurrency2 == "HUF") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=HUF");
            } else if (activeCurrency2 == "IDR") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=IDR");
            } else if (activeCurrency2 == "ILS") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=ILS");
            } else if (activeCurrency2 == "INR") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=INR");
            } else if (activeCurrency2 == "JPY") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=JPY");
            } else if (activeCurrency2 == "KRW") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=KRW");
            } else if (activeCurrency2 == "MXN") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=MXN");
            } else if (activeCurrency2 == "MYR") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=MYR");
            } else if (activeCurrency2 == "NOK") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=NOK");
            } else if (activeCurrency2 == "NZD") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=NZD");
            } else if (activeCurrency2 == "PHP") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=PHP");
            } else if (activeCurrency2 == "PLN") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=PLN");
            } else if (activeCurrency2 == "RON") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=RON");
            } else if (activeCurrency2 == "RUB") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=RUB");
            } else if (activeCurrency2 == "SEK") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=SEK");
            } else if (activeCurrency2 == "SGD") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=SGD");
            } else if (activeCurrency2 == "THB") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=THB");
            } else if (activeCurrency2 == "TRY") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=TRY");
            } else if (activeCurrency2 == "ZAR") {
                DownloadTask taskUSD = new DownloadTask();
                taskUSD.execute("http://api.fixer.io/latest?base=ZAR");
            }


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

                /* try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("rates.json", Context.MODE_PRIVATE)); // Dit zet de wisselkoersen in een bestand zodat ze later weer gelezen kunnen worden als je geen internet hebt
                    outputStreamWriter.write(result); // dit gebruiken we niet omdat het erg moeilijk bleek te zijn om dit te implementeren met alle verschillende valuta
                    outputStreamWriter.close();

                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                } */

                try {
                    JSONObject rates = new JSONObject(result); // Hier wordt een object gemaakt van de result String

                    String AUDRateTemp = rates.getJSONObject("rates").getString("AUD"); // de AUD string wordt uit het object gehaald
                    AUDRate = Double.parseDouble(AUDRateTemp); // en omgezet in een Double zodat er mee gerekent kan worden

                    String BGNRateTemp = rates.getJSONObject("rates").getString("BGN");
                    BGNRate = Double.parseDouble(BGNRateTemp);

                    String BRLRateTemp = rates.getJSONObject("rates").getString("BRL");
                    BRLRate = Double.parseDouble(BRLRateTemp);

                    String CADRateTemp = rates.getJSONObject("rates").getString("CAD");
                    CADRate = Double.parseDouble(CADRateTemp);

                    String CHFRateTemp = rates.getJSONObject("rates").getString("CHF");
                    CHFRate = Double.parseDouble(CHFRateTemp);

                    String CNYRateTemp = rates.getJSONObject("rates").getString("CNY");
                    CNYRate = Double.parseDouble(CNYRateTemp);

                    String CZKRateTemp = rates.getJSONObject("rates").getString("CZK");
                    CZKRate = Double.parseDouble(CZKRateTemp);

                    String DKKRateTemp = rates.getJSONObject("rates").getString("DKK");
                    DKKRate = Double.parseDouble(DKKRateTemp);

                    String GBPRateTemp = rates.getJSONObject("rates").getString("GBP");
                    GBPRate = Double.parseDouble(GBPRateTemp);

                    String HKDRateTemp = rates.getJSONObject("rates").getString("HKD");
                    HKDRate = Double.parseDouble(HKDRateTemp);

                    String HRKRateTemp = rates.getJSONObject("rates").getString("HRK");
                    HRKRate = Double.parseDouble(HRKRateTemp);

                    String HUFRateTemp = rates.getJSONObject("rates").getString("HUF");
                    HUFRate = Double.parseDouble(HUFRateTemp);

                    String IDRRateTemp = rates.getJSONObject("rates").getString("IDR");
                    IDRRate = Double.parseDouble(IDRRateTemp);

                    String ILSRateTemp = rates.getJSONObject("rates").getString("ILS");
                    ILSRate = Double.parseDouble(ILSRateTemp);

                    String INRRateTemp = rates.getJSONObject("rates").getString("INR");
                    INRRate = Double.parseDouble(INRRateTemp);

                    String JPYRateTemp = rates.getJSONObject("rates").getString("JPY");
                    JPYRate = Double.parseDouble(JPYRateTemp);

                    String KRWRateTemp = rates.getJSONObject("rates").getString("KRW");
                    KRWRate = Double.parseDouble(KRWRateTemp);

                    String MXNRateTemp = rates.getJSONObject("rates").getString("MXN");
                    MXNRate = Double.parseDouble(MXNRateTemp);

                    String MYRRateTemp = rates.getJSONObject("rates").getString("MYR");
                    MYRRate = Double.parseDouble(MYRRateTemp);

                    String NOKRateTemp = rates.getJSONObject("rates").getString("NOK");
                    NOKRate = Double.parseDouble(NOKRateTemp);

                    String NZDRateTemp = rates.getJSONObject("rates").getString("NZD");
                    NZDRate = Double.parseDouble(NZDRateTemp);

                    String PHPRateTemp = rates.getJSONObject("rates").getString("PHP");
                    PHPRate = Double.parseDouble(PHPRateTemp);

                    String PLNRateTemp = rates.getJSONObject("rates").getString("PLN");
                    PLNRate = Double.parseDouble(PLNRateTemp);

                    String RONRateTemp = rates.getJSONObject("rates").getString("RON");
                    RONRate = Double.parseDouble(RONRateTemp);


                    String RUBRateTemp = rates.getJSONObject("rates").getString("RUB");
                    RUBRate = Double.parseDouble(RUBRateTemp);


                    String SEKRateTemp = rates.getJSONObject("rates").getString("SEK");
                    SEKRate = Double.parseDouble(SEKRateTemp);


                    String SGDRateTemp = rates.getJSONObject("rates").getString("SGD");
                    SGDRate = Double.parseDouble(SGDRateTemp);


                    String THBRateTemp = rates.getJSONObject("rates").getString("THB");
                    THBRate = Double.parseDouble(THBRateTemp);


                    String TRYRateTemp = rates.getJSONObject("rates").getString("TRY");
                    TRYRate = Double.parseDouble(TRYRateTemp);


                    String USDRateTemp = rates.getJSONObject("rates").getString("USD");
                    USDRate = Double.parseDouble(USDRateTemp);


                    String ZARRateTemp = rates.getJSONObject("rates").getString("ZAR");
                    ZARRate = Double.parseDouble(ZARRateTemp);


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

    public String offlineRates() { // In deze method wordt het offline bestand uitgelezen en in een String gezet. Dit gebruiken we niet, omdat het te moeilijk was om te implementeren met alle verschillende valuta

        String ret = "";

        try {
            InputStream inputStream = openFileInput("rates.json");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            Toast.makeText(MainActivity.this, "For first time use, you need to be connected to the internet.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;

    }

    public void viewer(View view){ // deze mehtod wordt uigevoerd als je op de active currency klik
        if(!listViewTrue)
            toCurrency = (TextView)findViewById(R.id.toCurrency);

            toCurrency.setText("");
            toCurrency.setVisibility(View.INVISIBLE); // de tekst wordt dan weggehaald
        listViewer(); // en de method waarin de lijst met valuta staan wordt opgeroepen


       }
    public void viewer2(View view){ // het zelfde als bij view1(View view)
        if(!listViewTrue2) {
            vanCurrency2 = (TextView)findViewById(R.id.vanCurrency);
            vanCurrency2.setText("");
            vanCurrency2.setVisibility(View.INVISIBLE);
            listViewer2();

        }
    }


    public void calculate(View view) { //Deze method wordt uitgevoerd als je op calculate drukt of als je op enter op het toetsenbord klikt.
        EditText editText1 = (EditText) findViewById(R.id.editTextEuro);//Hier maakt hij een prive variable aan die gelinkt is aan het edit text euro field in de design view.
        EditText editText2 = (EditText) findViewById(R.id.editTextDollar);//Hier maakt hij een prive variable aan die gelinkt is aan het edit text dollar field in de design view.
        if (€to$status) {
            if (editText1.getText().toString().equals("")) {//Als het veld leeg is dan geeft hij een foutmelding omdat je een waarde in moet geven.
                Toast.makeText(MainActivity.this, "You need to enter a number in the " + activeCurrency2 + " field.", Toast.LENGTH_LONG).show();
            } else {
                if (USDRate == 0) {//Als de USD rate  0 is dan zijn de wisselkoersen niet gedownload en moet dit opnieuw gebeuren.
                    Toast.makeText(MainActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                } else if (activeCurrency == activeCurrency2) {//Sls de valuta's aan elkaar gelijk zijn heeft het niet veel zin om het uit te rekenen en geeft hij een error.
                    Toast.makeText(MainActivity.this, "You need to choose two different currencies.", Toast.LENGTH_SHORT).show();
                } else if (activeCurrency == "AUD") {//Sls de gekozen currency gelijk is aan AUD dan zal hij de volgende reeks opdrachten uitvoeren.
                    doubleEuro = Double.valueOf(editText1.getText().toString());//hij kijkt eerst wat er is ingevuld in het euro veld.
                    anwser = doubleEuro * AUDRate;//Dan doet hij het vermenigvuldigen met de rate.
                    anwser = (double)Math.round(anwser * 100d) / 100d; //Afronden van het getal naar centen.
                    anwserString = String.valueOf(anwser);//Een string maken van het afgeronde getal.
                    editText2.setText(anwserString);//Het antwoord in vullen in het andere veld en dan komt het antwoord op het beeldscherm te zien.
                } else if (activeCurrency == "BGN") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * BGNRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "BRL") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * BRLRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "CAD") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * CADRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "CHF") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * CHFRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "CNY") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * CNYRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "CZK") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * CZKRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "DKK") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * DKKRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "GBP") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * GBPRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "HKD") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * HKDRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "HRK") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * HRKRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "HUF") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * HUFRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "IDR") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * IDRRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "ILS") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * ILSRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "INR") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * INRRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "JPY") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * JPYRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "KRW") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * KRWRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "MXN") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * MXNRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "MYR") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * MYRRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "NOK") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * NOKRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "NZD") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * NZDRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "PHP") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * PHPRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "PLN") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * PLNRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "RON") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * RONRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "RUB") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * RUBRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "SEK") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * SEKRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "SGD") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * SGDRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "THB") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * THBRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "TRY") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * TRYRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "USD") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * USDRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                } else if (activeCurrency == "ZAR") {
                    doubleEuro = Double.valueOf(editText1.getText().toString());
                    anwser = doubleEuro * ZARRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText2.setText(anwserString);
                }


            }
        } else {
            if (editText2.getText().toString().equals("")) {//hier doe je het tegenovergestelde als in het vorige gedeelte
                Toast.makeText(MainActivity.this, "You need to enter a number in the " + activeCurrency + " field.", Toast.LENGTH_LONG).show();//hij Kijkt of er iets is ingevuld.
            } else {
                if (USDRate == 0) {//Hier controleerd hij of de rates zijn gedownload
                    Toast.makeText(MainActivity.this, "The exchange rates have not yet been downloaded, press the refresh button.", Toast.LENGTH_SHORT).show();
                } else if (activeCurrency == activeCurrency2){//Als ze aan elkaar gelijk zijn dan kan je het niet berekenen want er komt dan toch hetzelfde uit.
                    Toast.makeText(MainActivity.this, "You need to choose two different currencies.", Toast.LENGTH_SHORT).show();
                } else if (activeCurrency == "AUD") {//
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / AUDRate;//Hier gebeurd het tegenovergestelde van het vorige want je deelt nu door de koers. Maar daarna zet hij het weer in een string en zie je het weer op het display.
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "BGN") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / BGNRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "BRL") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / BRLRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "CAD") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / CADRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "CHF") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / CHFRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "CNY") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / CNYRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "CZK") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / CZKRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "DKK") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / DKKRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "GBP") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / GBPRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "HKD") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / HKDRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "HRK") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / HRKRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "HUF") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / HUFRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "IDR") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / IDRRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "ILS") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / ILSRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "INR") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / INRRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "JPY") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / JPYRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "KRW") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / KRWRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "MXN") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / MXNRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "MYR") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / MYRRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "MYR") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / MYRRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "NOK") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / NOKRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "NZD") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / NZDRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "PHP") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / PHPRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "PLN") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / PLNRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "RON") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / RONRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "RUB") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / RUBRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "SEK") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / SEKRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "SGD") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / SGDRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "THB") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / THBRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "TRY") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / TRYRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "USD") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / USDRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "ZAR") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / ZARRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);
                } else if (activeCurrency == "EUR") {
                    doubleDollar = Double.valueOf(editText2.getText().toString());
                    anwser = doubleDollar / EURRate;
                    anwser = (double)Math.round(anwser * 100d) / 100d;
                    anwserString = String.valueOf(anwser);
                    editText1.setText(anwserString);

                }


            }
        }
    }


    public void switchCurrency(View view) {//dit is de functie voor het pijltje, je kan hier selecteren welke kant hij operekent.
        Button currencySwitch = (Button) findViewById(R.id.currencySwitch);
        if (€to$status) {
            currencySwitch.setText("<");
            €to$status = false;
        } else {
            currencySwitch.setText(">");
            €to$status = true;
        }
    }


    public void listViewer2() {

        Currencies = (ListView) findViewById(R.id.Currency2);//Hier maakt hij een prive variabele die wordt gelinkt aan de list view in de design view.

        final ArrayList<String> Currencies2 = new ArrayList<String>();//Er wordt een arraylist aangemaakt om alle valuta's in op te slaan.

        Currencies2.add("AUD");
        Currencies2.add("BGN");
        Currencies2.add("BRL");
        Currencies2.add("CAD");
        Currencies2.add("CHF");
        Currencies2.add("CNY");
        Currencies2.add("CZK");
        Currencies2.add("DKK");
        Currencies2.add("EUR");
        Currencies2.add("GBP");
        Currencies2.add("HKD");
        Currencies2.add("HRK");
        Currencies2.add("HUF");
        Currencies2.add("IDR");
        Currencies2.add("ILS");
        Currencies2.add("INR");
        Currencies2.add("JPY");
        Currencies2.add("KRW");
        Currencies2.add("MXN");
        Currencies2.add("MYR");
        Currencies2.add("NOK");
        Currencies2.add("NZD");
        Currencies2.add("PHP");
        Currencies2.add("PLN");
        Currencies2.add("RON");
        Currencies2.add("RUB");
        Currencies2.add("SEK");
        Currencies2.add("SGD");
        Currencies2.add("THB");
        Currencies2.add("TRY");
        Currencies2.add("USD");
        Currencies2.add("ZAR");

        ArrayAdapter<String> arrayDing2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Currencies2);//Deze array wordt gelinkt als een "Adapter".
        Currencies.setAdapter(arrayDing2); // de valuta's worden in de lijst gezet
        Currencies.setVisibility(View.VISIBLE); // de lijst word zichtbaar gemaakt
        Currencies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { // als de gebruiker op een valuta klikt

                activeCurrency2 = Currencies2.get(position); // Zet de activecurrency2 naar de valuta waar is op geklikt
                startDownloadIfNetworkTrue(); // start het downloaden van de wisselkoersen
                vanCurrency = (TextView)findViewById(R.id.vanCurrency);
                vanCurrency.setText(activeCurrency2); // zet de active valuta tekst naar de activeCurrency
                TextView text1 = (TextView) findViewById(R.id.vancurrency);
                text1.setText(activeCurrency2);
                Currencies.setVisibility(View.INVISIBLE); // maakt de lijst ontzichtbaar
                listViewTrue2 = false;
                vanCurrency.setVisibility(View.VISIBLE); // en maakt de active valuta tekst zichtbaar

            }
        });

    }


    public void listViewer() { // deze method is het zelfde als listViewer2() maar is het dan de rechter lijst

        Currencies3 = (ListView) findViewById(R.id.Currency);

        final ArrayList<String> Currencies2 = new ArrayList<String>();

        Currencies2.add("AUD");
        Currencies2.add("BGN");
        Currencies2.add("BRL");
        Currencies2.add("CAD");
        Currencies2.add("CHF");
        Currencies2.add("CNY");
        Currencies2.add("CZK");
        Currencies2.add("DKK");
        Currencies2.add("EUR");
        Currencies2.add("GBP");
        Currencies2.add("HKD");
        Currencies2.add("HRK");
        Currencies2.add("HUF");
        Currencies2.add("IDR");
        Currencies2.add("ILS");
        Currencies2.add("INR");
        Currencies2.add("JPY");
        Currencies2.add("KRW");
        Currencies2.add("MXN");
        Currencies2.add("MYR");
        Currencies2.add("NOK");
        Currencies2.add("NZD");
        Currencies2.add("PHP");
        Currencies2.add("PLN");
        Currencies2.add("RON");
        Currencies2.add("RUB");
        Currencies2.add("SEK");
        Currencies2.add("SGD");
        Currencies2.add("THB");
        Currencies2.add("TRY");
        Currencies2.add("USD");
        Currencies2.add("ZAR");


        ArrayAdapter<String> arrayDing2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Currencies2);

        Currencies3.setAdapter(arrayDing2);
        Currencies3.setVisibility(View.VISIBLE);
        Currencies3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activeCurrency = Currencies2.get(position);
                toCurrency = (TextView)findViewById(R.id.toCurrency);
                toCurrency.setText(activeCurrency);
                toCurrency.setVisibility(View.VISIBLE);
                TextView text2 = (TextView) findViewById(R.id.tocurrency);
                text2.setText(activeCurrency);
                Currencies3.setVisibility(View.INVISIBLE);
                listViewTrue = false;


            }
        });

    }


    public void titleScreenAdvance(View view) { // deze method wordt uitgevoerd als je op de "Continue" knop klikt op het title scherm
        setContentView(R.layout.activity_main); // zet de layout naar de layout waar de converter is
        SharedPreferences settings = getSharedPreferences("prefs", 0); // initializeerd de preferences
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("storedMainInitiated", true).commit(); // zet de Boolean storedMainInitiated naar true, deze wordt gebruikt om te kijken of al wel eens op continue hebt gedrukt om zo het title scherm over te slaan

        startDownloadIfNetworkTrue(); // start het downloaden van de activeCurrency's
        loadAds(); // laad de advertentie's


        TextView editText1;
        TextView editText2;
        editText1 = (EditText)findViewById(R.id.editTextDollar);
        editText2 = (EditText)findViewById(R.id.editTextEuro);
        editText1.setOnEditorActionListener(new TextView.OnEditorActionListener() { // dit wordt uitgevoerd als je in het linker text veldje aan het typen bent en je drukt op enter op je toetsenbord
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    calculate(null); // hij voert calculate uit
                }
                return false;
            }
        });

        editText2.setOnEditorActionListener(new TextView.OnEditorActionListener() { // dit wordt uitgevoerd als je in het rechter text veldje aan het typen bent en je drukt op enter op je toetsenbord
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    calculate(null); // hij voert calculate uit
                }
                return false;
            }
        });


    }
}

