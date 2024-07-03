package com.example.monumentdetection1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class init extends AppCompatActivity {
    private Button signup;
     private Button signin;

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        signup=findViewById(R.id.signup);
        signin=findViewById(R.id.signin);

        signup.setOnClickListener(
                v -> startActivity(new Intent(init.this, Register.class)));

        signin.setOnClickListener(v -> startActivity(new Intent(init.this, MainActivity.class)));
    }
}