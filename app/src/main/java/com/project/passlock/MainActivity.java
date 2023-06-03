package com.project.passlock;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.passlock.databinding.ActivityMenuDrawerBinding;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ListView lv;
    ArrayList<Category> categoriesList;
    CategoriesAdapter categoriesAdapter;

    Category lastSelected;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    ConstraintLayout mMainLayout;

    private Button buttonSignUp;
    private Button buttonSignIn;

    private Button customButtonSignUp;
    private Button customButtonSignIn;

    private EditText customEditTextEmail, customEditTextPassword, customEditTextFirstName, customEditTextLastName;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userRef;
    Dialog d, d1;
    int mode = 0;// 0 = Sign up, 1 = Sign in
    ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMenuDrawerBinding binding;


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


        lv = (ListView) findViewById(R.id.lv);

        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(this);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignIn.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
            buttonSignIn.setText("Logout");
        else
            buttonSignIn.setText("Log In");

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
                }
                DrawerLayout drawerLayout = findViewById(R.id.nav_drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });


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


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelected = categoriesAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("title", lastSelected.getTitle());
                intent.putExtra("icon", Helper.bitmapToByteArray(lastSelected.getIcon()));
                //startActivity(intent);
                startActivityForResult(intent, 0);

            }
        });

        Category c1 = new Category("Social networks");
        Category c2 = new Category("Bank");
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
    public void onClick(View v) {
        if (v == buttonSignUp) {
            createRegisterDialog();
        } else if (v == customButtonSignUp) {
            register();
        } else if (v == buttonSignIn) {
            if (buttonSignIn.getText().toString().equals("Log In"))
                createLogInDialog();
            else if (buttonSignIn.getText().toString().equals("Logout")) {
                firebaseAuth.signOut();
                buttonSignIn.setText("Log In");
            }
        } else if (v == customButtonSignIn) {
            login();

        }

    }


    private void createLogInDialog() {
        d1 = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        d1.setContentView(R.layout.signin_layout);
        d1.setTitle("Sign In");
        d1.setCancelable(true);
        customEditTextPassword = (EditText) d1.findViewById(R.id.customEditTextPassword);
        customEditTextEmail = (EditText) d1.findViewById(R.id.customEditTextEmail);
        customButtonSignIn = (Button) d1.findViewById(R.id.customButtonSignIn);
        customButtonSignIn.setOnClickListener(this);
        d1.show();
    }

    public void createRegisterDialog() {
        d = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        d.setContentView(R.layout.signup_layout);
        d.setTitle("Sign Up");
        d.setCancelable(true);
        customEditTextPassword = (EditText) d.findViewById(R.id.customEditTextPassword);
        customEditTextEmail = (EditText) d.findViewById(R.id.customEditTextEmail);
        customEditTextFirstName = (EditText) d.findViewById(R.id.customEditTextFirstName);
        customEditTextLastName = (EditText) d.findViewById(R.id.customEditTextLastName);
        customButtonSignUp = (Button) d.findViewById(R.id.customButtonSignUp);
        customButtonSignUp.setOnClickListener(this);
        mode = 0;
        d.show();
    }

    private void register() {

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(customEditTextEmail.getText().toString(), customEditTextPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Successfully registered", Toast.LENGTH_LONG).show();
                    buttonSignIn.setText("Logout");
                    addUserDetails();
                } else {
                    Toast.makeText(MainActivity.this, "Registration Error", Toast.LENGTH_LONG).show();

                }
                d.dismiss();
                progressDialog.dismiss();

            }
        });
    }

    private void login() {
        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(customEditTextEmail.getText().toString(), customEditTextPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Log in successfully", Toast.LENGTH_SHORT).show();
                            buttonSignIn.setText("Logout");
                        } else {
                            Toast.makeText(MainActivity.this, "Log in failed", Toast.LENGTH_SHORT).show();
                        }
                        d1.dismiss();
                        progressDialog.dismiss();
                    }


                });
    }

    public void addUserDetails() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        User user = new User(uid, customEditTextEmail.getText().toString(), customEditTextFirstName.getText().toString(), customEditTextLastName.getText().toString(), "");
        userRef = firebaseDatabase.getReference("Users").push();
        user.key = userRef.getKey();
        userRef.setValue(user);

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


    /*@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }*/
}