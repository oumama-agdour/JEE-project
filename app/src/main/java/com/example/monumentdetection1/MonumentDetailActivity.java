package com.example.monumentdetection1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.storage.FirebaseStorage;

public class MonumentDetailActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView nameTextView;
    private TextView descriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monument_detail);

        imageView = findViewById(R.id.monument_detail_image);
        nameTextView = findViewById(R.id.monument_detail_name);
        descriptionTextView = findViewById(R.id.monument_detail_description);

        // Get the data from the intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        String imageUrl = intent.getStringExtra("imageUrl");

        // Set the data to views
        nameTextView.setText(name);
        descriptionTextView.setText(description);
        downloadImage(imageUrl, imageView);
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