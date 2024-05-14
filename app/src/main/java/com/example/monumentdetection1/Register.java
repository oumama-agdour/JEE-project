package com.example.monumentdetection1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    EditText etMail , etPasswd , etpasswdd;
    Button bregister ;
    FirebaseAuth mAuth;
    private static final String TAG = Register.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etMail =findViewById(R.id.email);
        etPasswd=findViewById(R.id.password);
        etpasswdd=findViewById(R.id.confirm_password);
        bregister=findViewById(R.id.signup);
        mAuth= FirebaseAuth.getInstance();

        bregister.setOnClickListener(v -> {
            String mail=etMail.getText().toString();
            String password=etPasswd.getText().toString();
            String passwordd=etpasswdd.getText().toString();


            if(TextUtils.isEmpty(mail)){
                Toast.makeText(getApplicationContext(),
                        "Please fill in the required fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(password)){
                Toast.makeText(getApplicationContext(),
                        "Please fill in the required fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(passwordd)){
                Toast.makeText(getApplicationContext(),
                        "Please confirm your password",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if(password.length()<6){
                Toast.makeText(getApplicationContext(),
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if(!password.equals(passwordd)){
                Toast.makeText(getApplicationContext(),
                        "Please enter correct password",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            signUp (mail,password);
        });

    }

    private void signUp(String mail, String password) {
        mAuth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            else {
                Log.d(TAG, "Errror");
            }
        });
    }
}