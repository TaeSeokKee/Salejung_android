package com.salejung_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by xotjr on 2017-11-09.
 */

public class SearchActivity extends AppCompatActivity {

    Locale systemLocale = null;
    String BASE_URL = "https://salejung-dev.herokuapp.com/latlng/post/";
    RequestQueue queue = null;

    String lat = null;
    String lng = null;
    JSONArray jsonArray = null;

    FirebaseStorage storage = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        systemLocale = getApplicationContext().getResources().getConfiguration().locale;

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);
        lat = sharedPreferences.getString("lat", "no lng");
        lng = sharedPreferences.getString("lng", "no lng");


        HashMap<String, String>  params = new HashMap<>();
        params.put("lat", lat);
        params.put("lng", lng);

        storage = FirebaseStorage.getInstance();

        LatLngPostTask task = new LatLngPostTask();
        task.execute(params);


    }

    class LatLngPostTask extends AsyncTask<Map<String, String>, Integer, String> {

        protected String doInBackground(Map<String, String>... params) {
            String response = send(params[0], BASE_URL);

            try {
                jsonArray = new JSONArray(response);
                //json.getJSONObject(0);
                //Log.i("test4", json.getJSONObject(0).getString("photo"));
                //Log.i("test5", json.getJSONObject(0).getString("price"));
                //Log.i("test6", json.getJSONObject(0).getString("detail"));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("IMAGE_INFO_GET2", response);

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            RecyclerView rvItems = (RecyclerView) findViewById(R.id.rvContacts);
            List<Photo> allContacts = null;
            try {
                allContacts = Photo.createContactsList(jsonArray.length(), 0, jsonArray);
                Log.d("test2222", allContacts.get(0).getPhotoURL());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final PhotosAdapter adapter = new PhotosAdapter(allContacts, storage);
            rvItems.setAdapter(adapter);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this);
            rvItems.setLayoutManager(linearLayoutManager);
            final List<Photo> finalAllContacts = allContacts;

            EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    //Log.i("TEST1", "hello world1");
                    final int curSize = adapter.getItemCount();
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemRangeInserted(curSize, finalAllContacts.size() - 1);
                        }
                    });
                }
            };
            rvItems.addOnScrollListener(scrollListener);
        }
    }

    private String send (Map < String, String > map, String addr){
        String response = "";

        try {
            URL url = new URL(addr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            if (map != null) {
                OutputStream os = conn.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                bw.write(getPostString(map));
                bw.flush();
                bw.close();
                os.close();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                while ((line = br.readLine()) != null)
                    response += line;
            }

            conn.disconnect();
        } catch (MalformedURLException me) {
            me.printStackTrace();
            return me.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return response;
    }

    private String getPostString(Map<String, String> map) {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            try {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ue) {
                ue.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

}

