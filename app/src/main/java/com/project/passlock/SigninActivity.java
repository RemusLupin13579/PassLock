package com.project.passlock;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executor;

public class SigninActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText customEditTextEmail, customEditTextPassword, customEditTextFirstName, customEditTextLastName;
    private Button customButtonSignUp;
    TextView newRegister;
    private Button customButtonSignIn;
    Dialog d;
    ProgressDialog progressDialog1;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_layout);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance("https://paock-2a77c-default-rtdb.europe-west1.firebasedatabase.app");
        firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog1 = new ProgressDialog(this);
        customEditTextPassword = (EditText) findViewById(R.id.customEditTextPassword);
        customEditTextEmail = (EditText) findViewById(R.id.customEditTextEmail);
        customButtonSignIn = (Button) findViewById(R.id.customButtonSignIn);
        customButtonSignIn.setOnClickListener(this);
        newRegister = (TextView) findViewById(R.id.tvNewRegister);
        newRegister.setOnClickListener(this);


        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(SigninActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Authentication login")
                        .setSubtitle("Log in using your fingerprint or PIN").setDeviceCredentialAllowed(true)
                        //.setNegativeButtonText("Use account password")
                        .build();

                biometricPrompt.authenticate(promptInfo);
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
    public void onClick(View v) {
        if (v == customButtonSignIn) {
            if (customEditTextEmail.getText().toString() != null && customEditTextPassword.getText().toString() != null) {
                login();
            }
            else {
                Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        }
        if (v == newRegister) {
            createRegisterDialog();
        }
        if (v == customButtonSignUp) {
            if (customEditTextEmail != null && customEditTextPassword != null && customEditTextFirstName != null)
            register();
        }

    }

    private void login() {
        progressDialog1.setMessage("Registering Please Wait...");
        progressDialog1.show();

        firebaseAuth.signInWithEmailAndPassword(customEditTextEmail.getText().toString(), customEditTextPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(SigninActivity.this, "Log in successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SigninActivity.this, "Log in failed", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog1.dismiss();
                    }


                });
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
        d.show();
    }

    private void register() {

        progressDialog1.setMessage("Registering Please Wait...");
        progressDialog1.show();

        firebaseAuth.createUserWithEmailAndPassword(customEditTextEmail.getText().toString(), customEditTextPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SigninActivity.this, "Successfully registered", Toast.LENGTH_LONG).show();
                    addUserDetails();
                    Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SigninActivity.this, "Registration Error", Toast.LENGTH_LONG).show();

                }
                d.dismiss();
                progressDialog1.dismiss();

            }
        });
    }

    public void addUserDetails() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();//מקבל רפרנס לבסיס נתונים
        User user = new User(uid, customEditTextEmail.getText().toString(), customEditTextFirstName.getText().toString(), customEditTextLastName.getText().toString());//יוצר משתמש חדש
        dbRef.child("Users").child(uid).setValue(user);//מוסיף את המשתמש החדש לבסיס נתונים לפי הPath:יוזרז>uid>יוצר משתמש חדש
    }

}