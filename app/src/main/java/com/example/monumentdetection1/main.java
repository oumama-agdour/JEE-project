package com.example.monumentdetection1;
import com.example.monumentdetection1.R.id;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;

import android.os.Bundle;

import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;


import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class main extends AppCompatActivity {
    MaterialToolbar topAppBar;
    private MonumentAdapter monumentAdapter;
    private List<Monument> monumentList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FirebaseFirestore db;

    @SuppressLint("NonConstantResourceId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        topAppBar = findViewById(id.topAppBar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        monumentAdapter = new MonumentAdapter(monumentList, this);
        recyclerView.setAdapter(monumentAdapter);
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        loadMonuments();

        // Gestion des clics sur les éléments de menu

        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == id.camera){
                startActivity(new Intent(main.this, CameraActivity.class));
            }
            return false;
        });
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == id.search){
                startActivity(new Intent(main.this, MonumentActivity.class));
            }
            return false;
        });


    }
    private void loadMonuments() {
        db.collection("monuments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Monument monument = document.toObject(Monument.class);
                            monumentList.add(monument);
                        }
                        monumentAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(main.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(main.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                });
    }

}
