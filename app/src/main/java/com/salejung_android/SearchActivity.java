package com.salejung_android;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.google.firebase.storage.FirebaseStorage;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



/**
 * Created by xotjr on 2017-11-09.
 */

public class SearchActivity extends AppCompatActivity {

    Locale systemLocale = null;
    String BASE_URL = "https://salejung-dev.herokuapp.com/latlng/post/";

    String lat = null;
    String lng = null;

    FirebaseStorage storage = null;

    final String LAT_NULL = "no lat";
    final String LNG_NULL = "no lng";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        systemLocale = getApplicationContext().getResources().getConfiguration().locale;

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);
        lat = sharedPreferences.getString("lat", LAT_NULL);
        lng = sharedPreferences.getString("lng", LNG_NULL);

        if(lat == LAT_NULL || lat == null) {
            Log.e("SearchActivity", "lat is null");
        }

        if(lng == LNG_NULL || lng == null) {
            Log.e("SearchActivity", "lng is null");
        }



        HashMap<String, String>  params = new HashMap<>();
        params.put("lat", lat);
        params.put("lng", lng);

        storage = FirebaseStorage.getInstance();

        if (storage == null)
            Log.e("SearchActivity", "storage is null");

        LatLngPostTask task = new LatLngPostTask();
        task.execute(params);


    }

    class LatLngPostTask extends AsyncTask<Map<String, String>, Integer, String> {

        protected String doInBackground(Map<String, String>... params) {
            String response = send(params[0], BASE_URL);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RecyclerView rvItems = (RecyclerView) findViewById(R.id.rvContacts);
            List<Photo> allContacts = null;

            try {
                allContacts = Photo.createContactsList(jsonArray.length(), 0, jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (allContacts == null)
                Log.e("SearchActivity", "allContacts is null");

            final PhotosAdapter adapter = new PhotosAdapter(allContacts, storage);
            rvItems.setAdapter(adapter);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this);
            rvItems.setLayoutManager(linearLayoutManager);
            final List<Photo> finalAllContacts = allContacts;

            EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
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

