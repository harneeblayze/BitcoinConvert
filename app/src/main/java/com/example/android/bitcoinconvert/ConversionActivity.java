package com.example.android.bitcoinconvert;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class ConversionActivity extends AppCompatActivity {
    TextView btcCurrencyName, ethCurrencyName, bitcoinTextView, ethereumTextView;
    EditText btcCurrencyEditText, ethCurrencyEditText;
    Currency currency;
    String currencyName ;
    double btcValue, ethValue, btcInvValue, ethInvValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);
        btcCurrencyName = (TextView) findViewById(R.id.currency_name);
        ethCurrencyName = (TextView)findViewById(R.id.currencyName);

        btcCurrencyEditText = (EditText) findViewById(R.id.currencyBtcEditText);
        bitcoinTextView = (TextView) findViewById(R.id.bitcoinTextView);

        ethCurrencyEditText = (EditText) findViewById(R.id.currencyEthEditText);
        ethereumTextView = (TextView) findViewById(R.id.ethreumTextView);

        if(getIntent() != null) {
            currency = getIntent().getParcelableExtra("passed-currency");
            currencyName = currency.getCountry();
            btcValue = currency.getBitcoin();
            btcInvValue = 1/btcValue;
            ethValue = currency.getEthereum();
            ethInvValue = 1/ethValue;

            btcCurrencyName.setText(currencyName);
            ethCurrencyName.setText(currencyName);

            addEditTextListener(btcCurrencyEditText,bitcoinTextView,btcInvValue);
            addEditTextListener(ethCurrencyEditText, ethereumTextView, ethInvValue);
        }
    }

    private void addEditTextListener(final EditText startEditText, final TextView endEditText,final double conversion){
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length() >0) {
                    double amount = Double.valueOf(charSequence.toString());
                    endEditText.setText(String.valueOf(amount * conversion));
                    if(charSequence.toString().equals(".")){
                        endEditText.setText("");
                    }
                }
                else {
                    endEditText.setText("");
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        };
        startEditText.addTextChangedListener(textWatcher);
    }

}
