package com.project.passlock;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DailyTip extends AppCompatActivity {
    TextView tvdailyTip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_tip);

        tvdailyTip = findViewById(R.id.tvDailyTip);
    }
}