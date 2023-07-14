package com.project.passlock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Random;

public class PasswordGenerator extends AppCompatActivity implements View.OnClickListener {

    TextView textViewPassword, tvHoraot;
    Button btnGenerate;
    String password;
    PendingIntent pending_intent;
    AlarmManager alarm_manager;
    Switch switchView;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_password_generator);

        firebaseAuth = FirebaseAuth.getInstance();

        btnGenerate = findViewById(R.id.btnGeneratePass);
        btnGenerate.setOnClickListener(this);

        tvHoraot = findViewById(R.id.tvHoraot);
        textViewPassword = findViewById(R.id.tvPass);
        textViewPassword.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("category", textViewPassword.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(PasswordGenerator.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;

            }
        });

        NavigationViewSettings();
        notificationSettings();
    }

    private static char[] generatePassword(int length) {
        String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String specialCharacters = "!@#$";
        String numbers = "1234567890";
        String combinedChars = capitalCaseLetters + lowerCaseLetters + specialCharacters + numbers;
        Random random = new Random();
        char[] password = new char[length];

        password[0] = lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
        password[1] = capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
        password[2] = specialCharacters.charAt(random.nextInt(specialCharacters.length()));
        password[3] = numbers.charAt(random.nextInt(numbers.length()));

        for(int i = 4; i< length ; i++) {
            password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }
        return password;
    }
    @Override
    public void onClick(View view) {
        password = String.valueOf(generatePassword(20));
        textViewPassword.setText(password);
        textViewPassword.setVisibility(View.VISIBLE);
        tvHoraot.setVisibility(View.VISIBLE);
    }

    private void notificationSettings() {
        notificationChannel();
        Intent intent = new Intent(this, Notification_reciever.class);
        intent.putExtra("context", getApplicationContext().toString());

        pending_intent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);
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