package com.example.monumentdetection1;
import com.example.monumentdetection1.R.id;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;

import android.os.Bundle;

import android.view.MenuItem;
import android.widget.Toolbar;


import com.google.android.material.appbar.MaterialToolbar;

public class main extends AppCompatActivity {
    MaterialToolbar topAppBar;

    @SuppressLint("NonConstantResourceId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        topAppBar = findViewById(id.topAppBar);

        // Gestion des clics sur les éléments de menu

        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == id.camera){
                startActivity(new Intent(main.this, MonumentActivity.class));
            }
            return false;
        });


    }



}
