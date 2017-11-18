package com.salejung_android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by xotjr on 2017-11-17.
 */

public class BoardActivity extends AppCompatActivity {

    final String TAG = "BoardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_LOW));
            } else {
                Log.e(TAG, "notificationManager is null");
            }
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            String photoFilePath = (String) getIntent().getExtras().get("photoFilePath");
            String detail = (String) getIntent().getExtras().get("detail");

            if (photoFilePath == null) {
                Log.e(TAG, "photoFilePath is null");
            }

            if (detail == null) {
                Log.e(TAG, "detail is null");
            }

            Log.d(TAG, "photoFilePath : " + photoFilePath);
            Log.d(TAG, "detail : " + detail);

            final ImageView photoView = (ImageView) findViewById(R.id.imgView1);
            final TextView detailView = (TextView) findViewById(R.id.detail1);

            FirebaseStorage mStorage = FirebaseStorage.getInstance();;
            // Create a storage reference from our app
            StorageReference storageRef = mStorage.getReference();
            // Create a reference with an initial file path

            StorageReference pathReference = null;
            if (photoFilePath != null) {
                pathReference = storageRef.child(photoFilePath);
            }

            GlideApp.with(this)
                    .load(pathReference)
                    .into(photoView);

            detailView.setText(detail);
        }
        // [END handle_data_extras]
    }
}
