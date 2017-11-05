package com.example.android.bitcoinconvert;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HARNY on 11/4/2017.
 */

public class Currency implements Parcelable {
    String country;
    double bitcoin;
    double ethereum;

    public Currency(String country, double bitcoin, double ethereum) {
        this.country = country;
        this.bitcoin = bitcoin;
        this.ethereum = ethereum;
    }

    public String getCountry() {

        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getBitcoin() {
        return bitcoin;
    }

    public void setBitcoin(double bitcoin) {
        this.bitcoin = bitcoin;
    }

    public double getEthereum() {
        return ethereum;
    }

    public void setEthereum(double ethereum) {
        this.ethereum = ethereum;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.country);
        dest.writeDouble(this.bitcoin);
        dest.writeDouble(this.ethereum);
    }

    protected Currency(Parcel in) {
        this.country = in.readString();
        this.bitcoin = in.readDouble();
        this.ethereum = in.readDouble();
    }

    public static final Parcelable.Creator<Currency> CREATOR = new Parcelable.Creator<Currency>() {
        @Override
        public Currency createFromParcel(Parcel source) {
            return new Currency(source);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };
}
