package com.salejung_android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.Locale;


/**
 * Created by xotjr on 2017-11-09.
 */

public class SearchActivity extends AppCompatActivity {

    Locale systemLocale = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        systemLocale = getApplicationContext().getResources().getConfiguration().locale;

    }

}
