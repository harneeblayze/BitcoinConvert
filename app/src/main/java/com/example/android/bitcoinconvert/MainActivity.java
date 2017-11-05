package com.example.android.bitcoinconvert;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity  {
    public final String BASE_URL = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=BTC,ETH&tsyms=";
    public final String FIRST_QUERY = "DZD,ARS,BRL,XAF,CAD,CNY,CRC,EUR,INR,ILS,JPY,NGN,PEN,GBP,USD,QAR,GHC,NZD,PAB";
    public final String SECOND_QUERY = "HKD,NOK,EGP,SAR,ZMW";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor storageEditor;
    CurrencyAdapter currencyAdapter;
    RecyclerView mRecyclerView;
    FloatingActionButton addCardButton;
    ArrayList<Currency> allCurrencies;
    Context myContext;
    ProgressDialog progressDialog;

    ItemTouchHelper slider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allCurrencies = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.currency_recycler_view);
        addCardButton = (FloatingActionButton) findViewById(R.id.addCardFloatingButton);

        myContext = getApplicationContext();

        sharedPreferences = getSharedPreferences("currency", MODE_PRIVATE);
        storageEditor = sharedPreferences.edit();
        currencyAdapter = new CurrencyAdapter(myContext, allCurrencies);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(myContext);
        mRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setAdapter(currencyAdapter);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMax(100);
        progressDialog.setMessage("Cryptographically fetching info...");
        progressDialog.setTitle("Your Currencies");
        if(savedInstanceState != null){
            allCurrencies = savedInstanceState.getParcelableArrayList("currencies");
        }
        try {
            Long currentTime = System.currentTimeMillis();
            Long diff = Long.parseLong(String.valueOf( 15 * 60 * 1000));
            Long lastSavedTime = sharedPreferences.getLong(Constants.DATE_KEY,currentTime-diff );
            if(currentTime - lastSavedTime >= diff){
                getCurrencies(BASE_URL+FIRST_QUERY);

                if(sharedPreferences.getString(Constants.SHOWN_COUNTRY_LIST_KEY,"").length() < 3){
                    Snackbar.make(addCardButton,"No currency card created yet",Snackbar.LENGTH_INDEFINITE).show();
                }
            }
            else{
                ArrayList<Currency> currencies = new ArrayList<>();
                JSONObject result = new JSONObject(sharedPreferences.getString(Constants.CONVERSION_DATA_KEY_ONE," "));
                currencies.addAll(workOnResult(result));

                JSONObject result2 = new JSONObject(sharedPreferences.getString(Constants.CONVERSION_DATA_KEY_TWO," "));
                currencies.addAll(workOnResult(result2));
                currencyAdapter.swapCurrencies(currencies);
                progressDialog.cancel();
                Toast.makeText(myContext,"Swipe left or right to remove card",Toast.LENGTH_LONG).show();
                if(sharedPreferences.getString(Constants.SHOWN_COUNTRY_LIST_KEY,"").length() < 3){
                    Snackbar.make(addCardButton,"No currency card created yet",Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            restartActivity();
        }
        makeSlider();
        slider.attachToRecyclerView(mRecyclerView);
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorPopUp(MainActivity.this);
            }
        });

    }

    public void getCurrencies(String url) throws IOException{
        final ArrayList<Currency> resultCurrencies = new ArrayList<>();
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            storageEditor.putString(Constants.CONVERSION_DATA_KEY_ONE,response);
                            storageEditor.apply();
                            resultCurrencies.addAll(workOnResult(result));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject result = new JSONObject(sharedPreferences.getString(Constants.CONVERSION_DATA_KEY_ONE," "));
                    resultCurrencies.addAll(workOnResult(result));
                } catch (JSONException e) {
                    e.printStackTrace();
                    restartActivity();
                }
            }
        });

        StringRequest secondRequest = new StringRequest(Request.Method.GET, BASE_URL + SECOND_QUERY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            storageEditor.putString(Constants.CONVERSION_DATA_KEY_TWO, response);
                            resultCurrencies.addAll(workOnResult(result));
//                            currencyAdapter.swapCurrencies(resultCurrencies);
                            allCurrencies = resultCurrencies;
                            currencyAdapter = new CurrencyAdapter(myContext, resultCurrencies);
                            Toast.makeText(myContext,"Length of fetched array is "+resultCurrencies.size(),Toast.LENGTH_LONG).show();
                            mRecyclerView.setAdapter(currencyAdapter);
                            progressDialog.cancel();
                            long time = System.currentTimeMillis();
                            storageEditor.putLong(Constants.DATE_KEY,time);
                            storageEditor.apply();
                            currencyAdapter.notifyDataSetChanged();
                            restartActivity();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject result = new JSONObject(sharedPreferences.getString(Constants.CONVERSION_DATA_KEY_TWO," "));
                    resultCurrencies.addAll(workOnResult(result));
                    currencyAdapter.swapCurrencies(resultCurrencies);
                    progressDialog.cancel();
                    Toast.makeText(myContext,"No Internet, Using previous data",Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    restartActivity();
                }
            }
        });
        queue.add(stringRequest);
        queue.add(secondRequest);
    }

    public ArrayList<Currency> workOnResult(JSONObject jsonObject){
        ArrayList<Currency> myCurrencies = new ArrayList<>();
        Log.i("BTc response ", "bitcoin response"+jsonObject.toString());
        try {
            JSONObject btc = jsonObject.getJSONObject("BTC");
            JSONObject eth = jsonObject.getJSONObject("ETH");

            Log.i("BTc response ", "bitcoin response"+btc.toString());
            Log.i("ethereum response ", "ethereum response"+eth.toString());

            Iterator<?> keysBtc = btc.keys();
            Iterator<?> keysEth = eth.keys();
            while(keysBtc.hasNext() && keysEth.hasNext()){
                String key = (String) keysBtc.next();
                double btcValue = btc.getDouble(key);
                double ethValue = eth.getDouble(key);
                myCurrencies.add(new Currency(key,btcValue,ethValue));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return myCurrencies;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Currency> curr = allCurrencies;
        outState.putParcelableArrayList("currencies", curr);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showSelectorPopUp(final Activity context){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_country_layout);
        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.copyFrom(dialog.getWindow().getAttributes());
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0);

        final AppCompatSpinner spinner = dialog.findViewById(R.id.country_select_spinner);
        Button addButton = dialog.findViewById(R.id.add_country_button);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String country = spinner.getSelectedItem().toString().trim();
                int firstBracPos = country.indexOf("(");
                int secBracPos = country.indexOf(")");
                String count = " "+country.substring(firstBracPos+1,secBracPos) +" ";
                Log.i("Saving selected item", "Saved selected item ->"+count);
                String selectedCountries = sharedPreferences.getString(Constants.SHOWN_COUNTRY_LIST_KEY,"");
                if(!selectedCountries.contains(count)) {
                    storageEditor.putString(Constants.SHOWN_COUNTRY_LIST_KEY,selectedCountries+count);
                    storageEditor.apply();
                    restartActivity();
                }
            }
        });
    }
    public void makeSlider(){
        slider = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if(mRecyclerView.getAdapter() == currencyAdapter){
                    String country = (String)viewHolder.itemView.getTag();
                    String selectedCountries = sharedPreferences.getString(Constants.SHOWN_COUNTRY_LIST_KEY,"");
                    Log.i("All countries in there ", "all countries "+selectedCountries);
                    if(selectedCountries.contains(country)) {
                        storageEditor.putString(Constants.SHOWN_COUNTRY_LIST_KEY,selectedCountries.replace(country,""));
                        storageEditor.apply();
                        restartActivity();
                        Log.i("Deleting an item","Deleted the item  "+country);
                        restartActivity();
                    }
                }
            }
        });
    }

    public void restartActivity(){
        Intent intent = new Intent(myContext, MainActivity.class);
        finish();
        startActivity(intent);
    }
}
