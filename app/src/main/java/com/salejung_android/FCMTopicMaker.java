package com.salejung_android;

import android.util.Log;


/**
 * Created by xotjr on 2017-11-17.
 */

public class FCMTopicMaker {

    private final String TAG = "FCMTopicMaker";
    private Double mLng;
    private Double mLat;

    public FCMTopicMaker(Double lng, Double lat) {
        mLng = lng;
        mLat = lat;
    }

    public String makeTopic() {
        /*
                    Make new topic to subscribe FCM.
                    Topic is coordinate value. Sum of string lng , lat.
                    FCM notification send massage to near place.
                     Topic is divided by 500m distnace which is coordinate 0.005.
                    */
        float mLng_round = (float) (Math.round(mLng*100d) / 100d);
        float mLat_round = (float) (Math.round(mLat*100d) / 100d);

        float mLng_ceil = (float) ((int)(mLng * 100) / 100.0);
        float mLat_ceil = (float) ((int)(mLat * 100) / 100.0);

        Log.d(TAG, Double.toString(mLng));
        Log.d(TAG, Double.toString(mLat));
        Log.d(TAG, Float.toString(mLng_round));
        Log.d(TAG, Float.toString(mLat_round));
        Log.d(TAG, Float.toString(mLng_ceil));
        Log.d(TAG, Float.toString(mLat_ceil));

        float mLng_for_fcm_topic;
        float mLat_for_fcm_topic;

        if (mLng_ceil == mLng_round) {
            mLng_for_fcm_topic = mLng_ceil + (float)0.000;
        } else {
            if (mLng_ceil >= 0){
                mLng_for_fcm_topic = mLng_ceil + (float)0.0050;
            } else {
                mLng_for_fcm_topic = mLng_ceil - (float)0.0050;
            }
        }

        if (mLat_ceil == mLat_round) {
            mLat_for_fcm_topic = mLat_ceil + (float)0.000;
        } else {
            if (mLat_ceil >= 0){
                mLat_for_fcm_topic = mLat_ceil + (float)0.005;
            } else {
                mLat_for_fcm_topic = mLat_ceil - (float)0.005;
            }
        }

        mLng_for_fcm_topic = (float) ((int)(mLng_for_fcm_topic * 1000) / 1000.0);
        mLat_for_fcm_topic = (float) ((int)(mLat_for_fcm_topic * 1000) / 1000.0);

        StringBuilder strBuilder_lng = new StringBuilder(Float.toString(mLng_for_fcm_topic));
        StringBuilder strBuilder_lat = new StringBuilder(Float.toString(mLat_for_fcm_topic));

        return strBuilder_lng.append("_").append(strBuilder_lat).toString();
    }
}
