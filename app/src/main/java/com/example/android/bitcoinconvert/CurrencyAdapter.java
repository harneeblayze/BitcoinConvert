package com.example.android.bitcoinconvert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by HARNY on 11/4/2017.
 */

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {
    Context mContext;
    ArrayList<Currency> currencies;
    SharedPreferences sharedPreferences;
    String selectedCountries;

    public CurrencyAdapter(Context context, ArrayList<Currency> currencies){
        mContext = context;
        this.currencies = currencies;
        sharedPreferences = mContext.getSharedPreferences("currency", MODE_PRIVATE);
        selectedCountries = sharedPreferences.getString(Constants.SHOWN_COUNTRY_LIST_KEY, "");
    }
    @Override
    public CurrencyAdapter.CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.each_item_view, parent,false);
        return new CurrencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CurrencyAdapter.CurrencyViewHolder holder, int position) {
        final Currency currency = currencies.get(position);
        String country = currencies.get(position).getCountry();
        double btc = currencies.get(position).getBitcoin();
        double eth = currencies.get(position).getEthereum();
        holder.countryTextView.setText(country);
        holder.btcTextView.setText(String.valueOf(btc));
        holder.ethTextView.setText(String.valueOf(eth));
        holder.itemView.setTag(" "+country+" ");
        if(!selectedCountries.contains(country)){
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
        }
        else{
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ConversionActivity.class);
                intent.putExtra("passed-currency",currency);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    public ArrayList<Currency> swapCurrencies(ArrayList<Currency> arrayList){
        if(currencies == arrayList) return  null;
        ArrayList<Currency> temp = currencies;
        currencies = arrayList;
        if(arrayList != null) this.notifyDataSetChanged();
        notifyDataSetChanged();
        return temp;
    }
    public class CurrencyViewHolder extends RecyclerView.ViewHolder {
        TextView countryTextView;
        TextView btcTextView;
        TextView ethTextView;
        public CurrencyViewHolder(View itemView) {
            super(itemView);
            countryTextView = itemView.findViewById(R.id.currency_type);
            btcTextView = itemView.findViewById(R.id.bitcoin_text_view);
            ethTextView = itemView.findViewById(R.id.ethereum_text_view);
        }
    }
}
