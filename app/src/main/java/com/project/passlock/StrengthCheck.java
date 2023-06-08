package com.project.passlock;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class StrengthCheck extends AppCompatActivity {

    private EditText editTextPassword;
    private View colorBar;
    private ProgressBar dynamicProgressBar;

    ImageView indicator_weak, indicator_meduim, indicator_strong;
    PendingIntent pending_intent;
    AlarmManager alarm_manager;
    Switch switchView;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_strength);

        resourcesReferencesToId();//findviewbyid for all the relevant resources

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

        NavigationViewSettings();
        notificationSettings();
    }

    private void resourcesReferencesToId() {
        indicator_weak = findViewById(R.id.indicator_weak);
        indicator_meduim = findViewById(R.id.indicator_medium);
        indicator_strong = findViewById(R.id.indicator_strong);

        dynamicProgressBar = findViewById(R.id.dynamicProgressBar);

        editTextPassword = findViewById(R.id.editTextPassword);
        colorBar = findViewById(R.id.colorBar);
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
    private void NavigationViewSettings() {
        //navigation drawer settings
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
                }
                if (item.getItemId() == R.id.nav_passGenerator) {
                    Intent intent = new Intent(getApplicationContext(), PasswordGenerator.class);
                    startActivity(intent);
                }
                if (item.getItemId() == R.id.nav_strength) {
                    Intent intent = new Intent(getApplicationContext(), StrengthCheck.class);
                    startActivity(intent);
                }
                if (item.getItemId() == R.id.nav_logout) {
                    firebaseAuth.signOut();
                    Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                    startActivity(intent);
                    finish();
                }
                DrawerLayout drawerLayout = findViewById(R.id.nav_drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        View headerView = navigationView.getHeaderView(0);
        switchView = headerView.findViewById(R.id.sw);
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    set_notification_alarm(24 * 60 * 60 * 1000);
                } else {
                    cancel_notification_alarm();
                }
            }
        });
    }


    private void notificationSettings() {
        notificationChannel();
        Intent intent = new Intent(this, Notification_reciever.class);
        intent.putExtra("context", getApplicationContext().toString());

        pending_intent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    private void notificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Security Tip";
            String description = "security tip";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Notification", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void set_notification_alarm(long interval) {
        long triggerAtMillis = System.currentTimeMillis() + interval;//24 שעות מהשעה הנוכחית

        /*
        Calendar calendar = Calendar.getInstance(); // Get an instance of the Calendar
        // Set the calendar to midnight (0:0)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        */

        // Schedule the alarm based on the SDK version
        if (Build.VERSION.SDK_INT >= 23) {
            alarm_manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pending_intent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm_manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending_intent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm_manager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending_intent);
        } else {
            alarm_manager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending_intent);
        }
    }

    public void cancel_notification_alarm() {
        alarm_manager.cancel(pending_intent);
    }
}