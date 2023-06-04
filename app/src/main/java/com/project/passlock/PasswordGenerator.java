package com.project.passlock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PasswordGenerator extends AppCompatActivity implements View.OnClickListener {

    TextView textViewPassword;
    Button btnGenerate;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_generator);

        btnGenerate = findViewById(R.id.btnGeneratePass);
        btnGenerate.setOnClickListener(this);
        textViewPassword = findViewById(R.id.tvPass);


    }

    @Override
    public void onClick(View view) {
        int Pass = (int)(Math.random() * 1000000000) + 99999999;
        password = Integer.toString(Pass);
        textViewPassword.setText(password);
        textViewPassword.setVisibility(View.VISIBLE);
    }
}