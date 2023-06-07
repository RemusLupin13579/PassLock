package com.project.passlock;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class StrengthCheck extends AppCompatActivity {

    private EditText editTextPassword;
    private View colorBar;
    private ProgressBar dynamicProgressBar;

    ImageView indicator_weak, indicator_meduim, indicator_strong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strength_check);

        indicator_weak = findViewById(R.id.indicator_weak);
        indicator_meduim = findViewById(R.id.indicator_medium);
        indicator_strong = findViewById(R.id.indicator_strong);


        dynamicProgressBar = findViewById(R.id.dynamicProgressBar);

        editTextPassword = findViewById(R.id.editTextPassword);
        colorBar = findViewById(R.id.colorBar);

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                dynamicProgressBar.setProgress(dynamicProgressBar.getProgress()+1);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

    }

    private void updatePasswordStrength(String password) {
        int strengthPercentage = calculatePasswordStrength(password);
        int staticColor;

        // Set the progress bar color based on the strength percentage
        if (strengthPercentage < 33) {
            staticColor = getResources().getColor(R.color.weakColor);
            indicator_weak.setVisibility(View.VISIBLE);
            indicator_meduim.setVisibility(View.INVISIBLE);
            indicator_strong.setVisibility(View.INVISIBLE);
        } else if (strengthPercentage < 66) {
            staticColor = getResources().getColor(R.color.mediumColor);
            indicator_weak.setVisibility(View.INVISIBLE);
            indicator_meduim.setVisibility(View.VISIBLE);
            indicator_strong.setVisibility(View.INVISIBLE);
        } else if (strengthPercentage > 66) {
            staticColor = getResources().getColor(R.color.strongColor);
            indicator_weak.setVisibility(View.INVISIBLE);
            indicator_meduim.setVisibility(View.INVISIBLE);
            indicator_strong.setVisibility(View.VISIBLE);
        }
        else {
            staticColor = getResources().getColor(R.color.weakColor);
            indicator_weak.setVisibility(View.VISIBLE);
            indicator_meduim.setVisibility(View.INVISIBLE);
            indicator_strong.setVisibility(View.INVISIBLE);
        }

        // Update the color of the static color bar
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{staticColor, staticColor}
        );
        colorBar.setBackground(gradientDrawable);

    }



    private int calculatePasswordStrength(String password) {
        int strengthPercentage = 0;

        // Check for password length
        if (password.length() >= 6) {
            strengthPercentage += 25;
        }

        // Check for uppercase letters
        if (containsUppercaseLetter(password)) {
            strengthPercentage += 25;
        }

        // Check for lowercase letters
        if (containsLowercaseLetter(password)) {
            strengthPercentage += 25;
        }

        // Check for numbers
        if (containsNumber(password)) {
            strengthPercentage += 25;
        }

        return strengthPercentage;
    }

    private boolean containsUppercaseLetter(String password) {
        return !password.equals(password.toLowerCase());
    }

    private boolean containsLowercaseLetter(String password) {
        return !password.equals(password.toUpperCase());
    }

    private boolean containsNumber(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

}