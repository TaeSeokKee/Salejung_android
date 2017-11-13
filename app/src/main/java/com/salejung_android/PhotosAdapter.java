package com.salejung_android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Created by xotjr on 2017-11-12.
 */

public class PhotosAdapter extends
        RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

    // Store a member variable for the contacts
    private List<Photo> mContacts;

    private Context mContext;

    FirebaseStorage mStorage = null;

    // Pass in the contact array into the constructor
    public PhotosAdapter(List<Photo> contacts, FirebaseStorage _storage) {

        if(contacts == null)
            Log.e("PhotoAdapter error", "contacts is null");

        if(_storage == null)
            Log.e("PhotoAdapter error", "_storage is null");

        mContacts = contacts;
        mStorage = _storage;

    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public ImageView imgView;
        public TextView textView1;
        public TextView textView2;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            imgView = (ImageView) itemView.findViewById(R.id.imgView1);
            textView1 = (TextView) itemView.findViewById(R.id.price1);
            textView2 = (TextView) itemView.findViewById(R.id.detail1);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if (mContext == null){
            Log.e("PhotoAdapter error", "mContext is null");
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View contactView = inflater.inflate(R.layout.item_contact, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Photo contact = mContacts.get(position);

        ImageView imgView = viewHolder.imgView;

        // Create a storage reference from our app
        StorageReference storageRef = mStorage.getReference();

        if (storageRef == null)
            Log.e("PhotoAdapter error", "storageRef is null");

        String country = contact.getCountry();

        if (country == null)
            Log.e("PhotoAdapter error", "country is null");

        String date = contact.getDate();

        if (date == null)
            Log.e("PhotoAdapter error", "date is null");

        String year = date.substring(0,4);
        String month = date.substring(4,6);
        String day = date.substring(6,8);

        // Create a reference with an initial file path
        StorageReference pathReference = storageRef.child("images/" + country + "/" + year + "/" + month + "/" + day + "/" + contact.getPhotoURL());

        // Load the image using Glide
        GlideApp.with(mContext)
                .load(pathReference)
                .into(imgView);

        TextView textView1 = viewHolder.textView1;
        TextView textView2 = viewHolder.textView2;
        textView1.setText(contact.getPrice());
        textView2.setText(contact.getDetail());

    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

}