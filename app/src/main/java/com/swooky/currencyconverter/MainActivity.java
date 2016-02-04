package com.swooky.currencyconverter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int backpress;
    boolean mainInitiated;
    boolean €to$status = true;
    double $to€rate = 0.8928; // as of 02/04/16
    double €to$rate = 1.1201; // as of 02/04/16
    double anwser;
    String anwserString;
    double doubleDollar;
    double doubleEuro;
    private Handler mHandler = new Handler();

    public void calculate(View view) {
        EditText editTextEuro = (EditText) findViewById(R.id.editTextEuro);
        EditText editTextDollar = (EditText) findViewById(R.id.editTextDollar);
        if (€to$status) {
            if (editTextEuro.getText().toString().equals("")){
                Toast.makeText(MainActivity.this, "You need to enter a number in the Euro field", Toast.LENGTH_SHORT).show();
            }
            else {
                doubleEuro = Double.valueOf(editTextEuro.getText().toString());
                anwser = doubleEuro * €to$rate;
                anwserString = String.valueOf(anwser);
                editTextDollar.setText(anwserString);
            }
        }

        else {
            if (editTextDollar.getText().toString().equals("")){
                Toast.makeText(MainActivity.this, "You need to enter a number in the Dollar field", Toast.LENGTH_SHORT).show();
            }
            else {
                doubleDollar = Double.valueOf(editTextDollar.getText().toString());
                anwser = doubleDollar * $to€rate;
                anwserString = String.valueOf(anwser);
                editTextEuro.setText(anwserString);
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        mainInitiated = settings.getBoolean("storedMainInitiated", false);
        if (mainInitiated) {
            setContentView(R.layout.activity_main);
        }
        else {
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
}
