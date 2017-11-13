package com.salejung_android;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xotjr on 2017-11-12.
 */

public class Photo {
    private String mPhotoURL;
    private String mPrice;
    private String mDetail;
    private String mDate;
    private String mCountry;

    public Photo(String photoURL, String price, String detail, String date, String country) {
        mPhotoURL = photoURL;
        mPrice = price;
        mDetail = detail;
        mDate = date;
        mCountry = country;
    }

    public String getPhotoURL() {
        return mPhotoURL;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getDetail() { return mDetail; }

    public String getDate() { return mDate; }

    public String getCountry() { return mCountry; }


    public static List<Photo> createContactsList(int numContacts, int offset, JSONArray jsonArray) throws JSONException {
        List<Photo> contacts = new ArrayList<Photo>();

        for (int i = 0; i <= numContacts-1; i++) {
            contacts.add(new Photo(
                    jsonArray.getJSONObject(i).getString("photo"),
                    jsonArray.getJSONObject(i).getString("price"),
                    jsonArray.getJSONObject(i).getString("detail"),
                    jsonArray.getJSONObject(i).getString("date"),
                    jsonArray.getJSONObject(i).getString("country")
                    ));

            Log.d("json", contacts.get(i).getPhotoURL());
            Log.d("json", contacts.get(i).getPrice());
            Log.d("json", contacts.get(i).getDetail());
            Log.d("json", contacts.get(i).getDate());
            Log.d("json", contacts.get(i).getCountry());
        }

        return contacts;
    }
}