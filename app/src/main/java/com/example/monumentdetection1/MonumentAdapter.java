package com.example.monumentdetection1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MonumentAdapter extends RecyclerView.Adapter<MonumentAdapter.ViewHolder> {
    private List<Monument> monumentList;
    private static Context context;

    public MonumentAdapter(List<Monument> monumentList, Context context) {
        this.monumentList = monumentList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monument, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Monument monument = monumentList.get(position);
        holder.nameTextView.setText(monument.getName());

        String imageUrl = monument.getImageUrl();
        loadImageFromUrl(holder.imageView, imageUrl);

        holder.bind(monument);


    }

    @Override
    public int getItemCount() {
        return monumentList.size();
    }

    public void updateList(List<Monument> filteredList) {
        monumentList = filteredList;
        notifyDataSetChanged();
    }

    private void loadImageFromUrl(ImageView imageView, String imageUrl) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);

        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            imageView = itemView.findViewById(R.id.imageView);

            
        }
        public void bind(Monument monument) {
            nameTextView.setText(monument.getName());
            downloadImage(monument.getImageUrl(), imageView);

            // Add OnClickListener here
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MonumentDetailActivity.class);
                intent.putExtra("name", monument.getName());
                intent.putExtra("description", monument.getDescription());
                intent.putExtra("imageUrl", monument.getImageUrl());
                context.startActivity(intent);
            });


        }


        private void downloadImage(String imageUrl, ImageView imageView) {
            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                    .getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(bitmap);
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                    });
        }
    }

    
}


