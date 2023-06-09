package com.project.passlock;

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
import android.os.Handler;
import android.os.Looper;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ListView lv;//category list view
    ArrayList<Category> categoriesList;
    CategoriesAdapter categoriesAdapter;
    Category lastSelected;//representing an item(Category) from the list view
    FirebaseDatabase firebaseDatabase;
    DatabaseReference firebaseDatabaseRef;
    private FirebaseAuth firebaseAuth;
    TextView textViewUserName;
    String fname;//will read current user's name from firebase
    FloatingActionButton floatingActionButton;
    int itemPosition;//position of category in the list view
    int numberOfCategories;
    PendingIntent pending_intent;
    AlarmManager alarm_manager;
    Switch switchView;
    boolean doubleBackToExitPressedOnce = false;//default state for double-tap-to-exit

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_main);

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance("https://paock-2a77c-default-rtdb.europe-west1.firebasedatabase.app");
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabaseRef = FirebaseDatabase.getInstance().getReference();

        supportInvalidateOptionsMenu();
        biometricAuthentication();

        welcomeUserMessage();

        NavigationViewSettings();
        notificationSettings();

        listViewAndAdapterSettings();

        loadCategoriesFromFirebase();
    }

    private void listViewAndAdapterSettings() {
        lv = (ListView) findViewById(R.id.lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelected = categoriesAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, PasswordsActivity.class);
                intent.putExtra("categoryName", lastSelected.getTitle());
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        categoriesList = new ArrayList<Category>();
        //phase 3 - create adapter
        categoriesAdapter = new CategoriesAdapter(this, 0, 0, categoriesList);
        //phase 4 reference to listview
        lv = (ListView) findViewById(R.id.lv);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
                lastSelected = categoriesAdapter.getItem(position);
                itemPosition = position;
                // Show the pop-up menu
                //showPopupMenu(view);
                return true;
            }
        });
        lv.setAdapter(categoriesAdapter);
    }

    public void welcomeUserMessage(){
        //welcome message with user's name
        textViewUserName = findViewById(R.id.user_name);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        DatabaseReference usersRef = firebaseDatabase.getReference("Users/" + uid + "/firstname");//קורא את הערך שנמצא בfirstname לפי הpath, ומשנה את הודעת הwelcome
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fname = snapshot.getValue(String.class);
                    textViewUserName.setText("Welcome, " + fname + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error retrieving user's first name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == floatingActionButton) {
            Intent intent = new Intent(this, EditActivity.class);
            startActivityForResult(intent, 1);
        }
    }

    private void notificationSettings() {
        notificationChannel();
        Intent intent = new Intent(this, Notification_reciever.class);
        intent.putExtra("context", getApplicationContext().toString());

        pending_intent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarm_manager = (AlarmManager)getSystemService(ALARM_SERVICE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drawer, menu);
        return true;
    }
    public void NavigationViewSettings(){
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
                    Intent intent = new Intent(MainActivity.this, PasswordGenerator.class);
                    startActivity(intent);
                }
                if (item.getItemId() == R.id.nav_strength) {
                    Intent intent = new Intent(MainActivity.this, StrengthCheck.class);
                    startActivity(intent);
                }
                if (item.getItemId() == R.id.nav_logout) {
                    firebaseAuth.signOut();
                    Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, SigninActivity.class);
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
                    //set_notification_alarm(24 * 60 * 60 * 1000);
                    set_notification_alarm(0);
                } else {
                    cancel_notification_alarm();
                }
            }
        });
    }

    private void loadCategoriesFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        DatabaseReference passwordsRef = firebaseDatabaseRef
                .child("Users")
                .child(uid)
                .child("Passwords")
                .child("Categories");
        passwordsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoriesList.clear();

                // Retrieve the number of categories
                if (dataSnapshot.hasChild("number of categories")) {
                    numberOfCategories = dataSnapshot.child("number of categories").getValue(Integer.class);
                }


                for (int i = 0; i < numberOfCategories; i++) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.getKey().equals("number of categories")) {
                            continue;
                        }
                        String categoryTitle = dataSnapshot.child(Integer.toString(i)).child("category").getValue(String.class);
                        Category categoryItem = new Category(categoryTitle);
                        categoriesList.add(categoryItem);
                    }
                    categoriesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error loading categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {//comes from edit mode
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "data updated", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "canceled", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == 1) {//comes from add mode
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "data updated", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "canceled", Toast.LENGTH_LONG).show();
            }
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
                    ClipData clip = ClipData.newPlainText("category", lastSelected.getTitle());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (item.getItemId() == R.id.edit) {
                    /*Intent intent = new Intent(MainActivity.this, EditActivity.class);
                    intent.putExtra("title", lastSelected.getTitle());
                    intent.putExtra("position", itemPosition);
                    startActivityForResult(intent, 0);*/
                    Toast.makeText(MainActivity.this, "In construction", Toast.LENGTH_SHORT).show();
                }
                /*if (item.getItemId() == R.id.delete) {
                    deleteCategory(itemPosition);
                    // Handle option 2 click (Delete)
                    lastSelected = categoriesAdapter.getItem(itemPosition);
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    // Remove category from the database
                    DatabaseReference passwordRef = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Users")
                            .child(uid)
                            .child("Passwords")
                            .child("Categories")
                            .child(String.valueOf(itemPosition));
                    passwordRef.removeValue();

                    // Updates number of categories
                    DatabaseReference passwordNumRef = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Users")
                            .child(uid)
                            .child("Passwords")
                            .child("Categories")
                            .child("number of categories");
                    passwordNumRef.setValue(numberOfCategories-1);
                    updateIndexes();
                    lastSelected=categoriesAdapter.getItem(itemPosition);
                    categoriesAdapter.remove(lastSelected);
                    categoriesAdapter.notifyDataSetChanged();
                    return true;
                }*/
                return false;
            }
        });
        popupMenu.show();
    }

    public void updateIndexes(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(uid)
                .child("Passwords")
                .child("Categories");

        int indexToDelete = itemPosition;

        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> categorySnapshots = dataSnapshot.getChildren();

                // Store the categories in a list
                List<DataSnapshot> categoriesList = new ArrayList<>();
                for (DataSnapshot categorySnapshot : categorySnapshots) {
                    categoriesList.add(categorySnapshot);
                }

                // Update the indexes of the remaining categories
                for (int i = indexToDelete; i < categoriesList.size(); i++) {
                    DataSnapshot categorySnapshot = categoriesList.get(i);
                    String categoryKey = String.valueOf(i - 1); // Calculate the new index
                    String categoryValue = categorySnapshot.getValue(String.class);

                    // Update the category index in the Firebase database
                    categoriesRef.child(categoryKey).setValue(categoryValue);
                }

                // Remove the last category entry since its index has changed
                categoriesRef.child(String.valueOf(categoriesList.size() - 1)).removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to update database data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteCategory(int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference categoryRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(uid)
                .child("Passwords")
                .child("Categories")
                .child(String.valueOf(position));

        categoryRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Category deleted successfully, now update "number of categories"
                    DatabaseReference numOfCategoriesRef = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Users")
                            .child(uid)
                            .child("Passwords")
                            .child("Categories")
                            .child("number of categories");

                    numOfCategoriesRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            Integer numberOfCategories = mutableData.getValue(Integer.class);
                            if (numberOfCategories != null) {
                                mutableData.setValue(numberOfCategories - 1);
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                            if (committed) {
                                Toast.makeText(getApplicationContext(), "Category deleted", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(), "Error updating data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Error updating data", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    private void biometricAuthentication() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
                finish();
                startActivity(getIntent());
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                        .show();

            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication login")
                .setSubtitle("Log in using your fingerprint or PIN").setDeviceCredentialAllowed(true)
                //.setNegativeButtonText("Use account password")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    public void onBackPressed() {//דורש לחיצה כפולה על 'חזור' בשביל לא לצאת בטעות מהאפליקציה
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();

        //מאפס את הקאונטר אם המשתמש לא לחץ ליציאה תוך 2 שניות
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

}