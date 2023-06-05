package com.project.passlock;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.passlock.databinding.ActivityMenuDrawerBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ListView lv;
    ArrayList<Category> categoriesList;
    CategoriesAdapter categoriesAdapter;
    Category lastSelected;
    /*private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;*/

    private Button customButtonSignUp;

    private EditText customEditTextEmail, customEditTextPassword, customEditTextFirstName, customEditTextLastName;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userRef;
    Dialog d;
    int mode = 0;// 0 = Sign up, 1 = Sign in
    ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMenuDrawerBinding binding;

    TextView textViewUserName;
    String fname;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance("https://paock-2a77c-default-rtdb.europe-west1.firebasedatabase.app");
        firebaseDatabase = FirebaseDatabase.getInstance();
        supportInvalidateOptionsMenu();

        lv = (ListView) findViewById(R.id.lv);
        progressDialog = new ProgressDialog(this);


        /*DatabaseReference myRef = firebaseDatabase.getReference();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                fname = user.getFirstname();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
        //Intent intent=getIntent();
        //name = intent.getExtras().getString("fname");
        textViewUserName = findViewById(R.id.user_name);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        DatabaseReference usersRef = firebaseDatabase.getReference("Users/" + uid + "/firstname");//קורא את הערך שנמצא בfirstname לפי הpath, ומשנה את הודעת הwelcome
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fname = snapshot.getValue(String.class);
                    textViewUserName.setText("Hello, " + fname + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error retrieving user's first name", Toast.LENGTH_SHORT).show();
            }
        });



        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
                }
                if(id == R.id.nav_passGenerator){
                    Intent intent = new Intent(MainActivity.this, PasswordGenerator.class);
                    startActivity(intent);
                }
                if(id == R.id.nav_strength){
                    Intent intent = new Intent(MainActivity.this, PasswordGenerator.class);
                    startActivity(intent);
                }
                if(id == R.id.nav_logout){
                    firebaseAuth.signOut();
                    Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
                    item.setTitle("Login");
                    Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                    startActivity(intent);
                    finish();
                }
                DrawerLayout drawerLayout = findViewById(R.id.nav_drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });


        /*executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
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

        biometricPrompt.authenticate(promptInfo);*/


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelected = categoriesAdapter.getItem(position);
                if(position == 0){
                    Intent intent = new Intent(MainActivity.this, PasswordsActivity.class);
                    startActivity(intent);
                }
                //Intent intent = new Intent(MainActivity.this, EditActivity.class);
                //intent.putExtra("title", lastSelected.getTitle());
                //startActivity(intent);
                //startActivityForResult(intent, 0);

            }
        });

        Category c1 = new Category("Social networks");
        Category c2 = new Category("Banks");
        Category c3 = new Category("Work");

        //phase 2 - add to array list
        categoriesList = new ArrayList<Category>();
        categoriesList.add(c1);
        categoriesList.add(c2);
        categoriesList.add(c3);

        //phase 3 - create adapter
        categoriesAdapter = new CategoriesAdapter(this, 0, 0, categoriesList);
        //phase 4 reference to listview
        lv = (ListView) findViewById(R.id.lv);
        lv.setAdapter(categoriesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drawer, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onClick(View view) {

    }
}