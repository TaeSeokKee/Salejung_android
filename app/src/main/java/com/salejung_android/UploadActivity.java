package com.salejung_android;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;




/**
 * Created by tae seok ki on 2017-11-04.
 */


public class UploadActivity extends AppCompatActivity {

    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    static final int REQUEST_TAKE_PHOTO = 1;
    public static String BASE_URL = "https://salejung-dev.herokuapp.com/photos/api/";
    String mCurrentPhotoPath = null;
    ImageView imgView = null;
    Uri photoURI = null;
    String user_uuid = null;
    String filefield = "photo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // if image clicked, take picture, get photo image and fill view with photo image.
        imgView = findViewById(R.id.imgView);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Todo : add function that when upload photo, user can select photo in external storage.
                // Now, User can upload image only by taking picture

                dispatchTakePictureIntent();
            }
        });

        // if button clicked, upload image and related information.
        final Button btn = findViewById(R.id.btn_upload_stage_2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Todo : not only imagePath, need also price, comment, lat and lng's null check.
                if (photoURI != null) {
                    try {
                        imageUpload();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Image not uploaded!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // get photo image and fill the view with photo image.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                if (photoURI != null) {
                    imgView.setImageURI(photoURI);
                } else {
                    Toast.makeText(getApplicationContext(), "imageCaptureUri not saved!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        user_uuid = id(UploadActivity.this);
        String imageFileName = user_uuid + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.salejung_android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    public synchronized static String id(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    private void imageUpload() throws IOException {
        Map<String, String> params = new HashMap<>();
        EditText priceText = findViewById(R.id.price);
        EditText detailText = findViewById(R.id.detail);
        Log.d("price", priceText.getText().toString());
        Log.d("detail", detailText.getText().toString());
        Log.d("user", user_uuid);
        params.put("user", user_uuid);
        params.put("price", priceText.getText().toString());
        params.put("detail", detailText.getText().toString());

        SharedPreferences sharedPreferences = getSharedPreferences("setting", 0);
        params.put("lat", sharedPreferences.getString("lat", "no lat"));
        params.put("lng", sharedPreferences.getString("lng", "no lng"));

        UploadTask task = new UploadTask();
        task.execute(params);
    }


    class UploadTask extends AsyncTask<Map<String, String>, Integer, String> {

        protected String doInBackground(Map<String, String>... params) {
            HttpURLConnection connection;
            DataOutputStream outputStream;
            InputStream inputStream;

            String twoHyphens = "--";
            String boundary = "*****" + "salejung" + "*****";
            String lineEnd = "\r\n";

            String result = "";

            String[] q = mCurrentPhotoPath.split("/");
            int idx = q.length - 1;

            try {
                File file = new File(mCurrentPhotoPath);
                FileInputStream fileInputStream = new FileInputStream(file);

                URL url = new URL(BASE_URL);
                connection = (HttpURLConnection) url.openConnection();

                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + q[idx] + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: " + "image/jpeg" + lineEnd);
                outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

                outputStream.writeBytes(lineEnd);


                // Upload POST photo
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                outputStream.write(imageBytes);

                outputStream.writeBytes(lineEnd);

                // Upload POST user
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "user" + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(params[0].get("user"));
                outputStream.writeBytes(lineEnd);

                // Upload POST price
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "price" + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(params[0].get("price"));
                outputStream.writeBytes(lineEnd);

                // Upload POST detail
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "detail" + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                // Todo : if use writeUTF function when post detail string to server, trash code is added at word's front.
                // So used write function. and need to test more specific. writeUTF function might has bug.
                outputStream.write(params[0].get("detail").getBytes("UTF-8"));
                outputStream.writeBytes(lineEnd);

                // Upload POST lat
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "lat" + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(params[0].get("lat"));
                outputStream.writeBytes(lineEnd);

                // Upload POST lng
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "lng" + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(params[0].get("lng"));
                outputStream.writeBytes(lineEnd);


                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                int responseCode = connection.getResponseCode();

                if (200 <= responseCode && responseCode <= 299) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder response = new StringBuilder();
                String currentLine;

                while ((currentLine = in.readLine()) != null)
                    response.append(currentLine);

                in.close();
                result = response.toString();

                fileInputStream.close();
                inputStream.close();
                outputStream.flush();
                outputStream.close();

            } catch (Exception e) {
                // Todo : exception handle
            }
            return result;
        }

        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }
}


