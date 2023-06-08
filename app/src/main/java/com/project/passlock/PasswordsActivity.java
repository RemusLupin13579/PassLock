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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PasswordsActivity extends AppCompatActivity implements View.OnClickListener {

    String categoryName;
    ListView lv;
    ArrayList<Password> passwordsList;
    PasswordAdapter passwordsAdapter;
    Password lastSelected;
    FloatingActionButton floatingActionButton;

    DatabaseReference firebaseDatabase;
    int categoryPosition;
    int selectedPosition;
    TextView tvCategory;

    int passwordPosition;

    PendingIntent pending_intent;
    AlarmManager alarm_manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_passwords);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        Switch switchView;
        notificationChannel();
        Intent intent = new Intent(this, Notification_reciever.class);
        intent.putExtra("context", getApplicationContext().toString());

        pending_intent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);


        // Get the category name from the intent
        categoryName = getIntent().getStringExtra("categoryName");
        categoryPosition = getIntent().getIntExtra("position", -1);

        tvCategory = (TextView) findViewById(R.id.tvCategory);
        tvCategory.setText(categoryName);
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);

        lv = (ListView) findViewById(R.id.Passlv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelected = passwordsAdapter.getItem(position);
                Intent intent = new Intent(PasswordsActivity.this, PasswordEditActivity.class);
                intent.putExtra("title", lastSelected.getTitle());
                intent.putExtra("password", lastSelected.getPassword());
                intent.putExtra("passPosition", position);
                intent.putExtra("categoryPosition", categoryPosition);
                intent.putExtra("categoryName", categoryName);
                intent.putExtra("editMode", "true");

                startActivityForResult(intent, 0);

            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
                lastSelected = passwordsAdapter.getItem(position);
                passwordPosition = position;
                // Show the pop-up menu
                showPopupMenu(view);
                return true;
            }
        });

        passwordsList = new ArrayList<Password>();
        // Initialize the adapter
        passwordsAdapter = new PasswordAdapter(this, 0, 0, passwordsList);
        // Set the adapter to the ListView
        lv.setAdapter(passwordsAdapter);
        // Load passwords from Firebase database
        loadPasswordsFromFirebase();


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

    private void loadPasswordsFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        DatabaseReference passwordsRef = firebaseDatabase
                .child("Users").child(uid).child("Passwords").child("Categories").child(String.valueOf(categoryPosition)).child(categoryName);
        passwordsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                passwordsList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Skip the "number of passwords" value
                    if (snapshot.getKey().equals("number of passwords")) {
                        continue;
                    }

                    String title = snapshot.child("title").getValue(String.class);
                    String password = snapshot.child("password").getValue(String.class);

                    Password passwordItem = new Password(title, password);
                    passwordsList.add(passwordItem);
                }

                passwordsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PasswordsActivity.this, "Error loading passwords", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.password_add_item, menu);
        for (int j = 0; j < menu.size(); j++) {
            MenuItem item = menu.getItem(j);
            item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {//comes from edit mode
            if (resultCode == RESULT_OK) {
                /*String title = data.getExtras().getString("title");
                String password = data.getExtras().getString("password");
                lastSelected.setTitle(title);
                lastSelected.setPassword(password);
                passwordsAdapter.notifyDataSetChanged();*/
                Toast.makeText(this, "data updated", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "canceled", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == 1) {//comes from add mode
            if (resultCode == RESULT_OK) {
                /*String title = data.getExtras().getString("title");
                String password = data.getExtras().getString("password");
                Password passwordItem = new Password(title, password);
                passwordsList.add(passwordItem);
                passwordsAdapter.notifyDataSetChanged();*/
                Toast.makeText(this, "Password added", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "canceled", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onClick(View v) {
        if (v == floatingActionButton) {
            Intent intent = new Intent(this, PasswordEditActivity.class);
            intent.putExtra("category", categoryName);
            intent.putExtra("position", categoryPosition);
            intent.putExtra("addMode", "true");
            startActivityForResult(intent, 1);
        }
    }
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle menu item click
                if (item.getItemId() == R.id.copy) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("password", lastSelected.getPassword());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(PasswordsActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (item.getItemId() == R.id.edit){
                    Intent intent = new Intent(PasswordsActivity.this, PasswordEditActivity.class);
                    intent.putExtra("title", lastSelected.getTitle());
                    intent.putExtra("password", lastSelected.getPassword());
                    intent.putExtra("passPosition", passwordPosition);
                    intent.putExtra("categoryPosition", categoryPosition);
                    intent.putExtra("categoryName", categoryName);
                    intent.putExtra("editMode", "true");

                    //intent.putExtra("title", lastSelected.getTitle());
                    //intent.putExtra("password", lastSelected.getPassword());
                    //intent.putExtra("position", categoryPosition);
                    startActivityForResult(intent, 0);
                }
                if (item.getItemId() == R.id.delete) {
                    // Handle option 2 click (Delete)
                    lastSelected = passwordsAdapter.getItem(categoryPosition);
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    // Remove title and password from the database
                    DatabaseReference passwordRef = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Users")
                            .child(uid)
                            .child("Passwords")
                            .child("Social networks")
                            .child(Integer.toString(categoryPosition));
                    passwordRef.removeValue();

                    passwordsAdapter.remove(lastSelected);
                    passwordsAdapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
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

