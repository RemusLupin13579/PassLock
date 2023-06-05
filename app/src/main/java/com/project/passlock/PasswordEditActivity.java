package com.project.passlock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

interface PasswordIndexCallback {
    void onPasswordIndexRetrieved(int passwordIndex);
}

public class PasswordEditActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etTitle, etPassword;
    Button btnSave, btnCancel;
    private DatabaseReference firebaseDatabase;
    private int passwordIndex = -1;
    int num_of_passwords;
    int mode = 0;//0=add mode, 1=edit mode
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_edit);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        etPassword = (EditText) findViewById(R.id.etPassword);
        etTitle = (EditText) findViewById(R.id.etTitle);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        //connect to intent if its edit mode
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            mode = 1;
            String title = intent.getExtras().getString("title");
            String password = intent.getExtras().getString("password");
            position = intent.getExtras().getInt("position");
            etTitle.setText(title);
            etPassword.setText(password);
        }
    }

    @Override
    public void onClick(View v) {
        if (btnSave == v)//option 1 - save the data and go to first screen
        {
            if (mode == 0) {//add mode
                if (etTitle.getText().toString().length() > 0) {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
                    getPasswordIndex(new PasswordIndexCallback() {//קורא את הערך של מספר סיסמאות שמורות, מחזיר 0 אם אין ערך כזה
                        @Override
                        public void onPasswordIndexRetrieved(int passwordIndex) {
                            firebaseDatabase.child("Users").child(uid).child("Passwords").child("Social networks")
                                    .child(Integer.toString(passwordIndex)).child("title").setValue(etTitle.getText().toString());
                            firebaseDatabase.child("Users").child(uid).child("Passwords").child("Social networks")
                                    .child(Integer.toString(passwordIndex)).child("password").setValue(etPassword.getText().toString());
                            firebaseDatabase.child("Users").child(uid).child("Passwords").child("Social networks")
                                    .child("number of passwords").setValue(passwordIndex + 1);
                        }
                    });

                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            } else if (mode==1) {//edit mode
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
                getPasswordIndex(new PasswordIndexCallback() {
                    @Override
                    public void onPasswordIndexRetrieved(int passwordIndex) {
                        DatabaseReference passwordRef = firebaseDatabase
                                .child("Users")
                                .child(uid)
                                .child("Passwords")
                                .child("Social networks")
                                .child(String.valueOf(position));

                        passwordRef.child("title").setValue(etTitle.getText().toString());
                        passwordRef.child("password").setValue(etPassword.getText().toString());

                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        } else if (btnCancel == v)//option 2 - cancel-  and go to first screen
        {
            setResult(RESULT_CANCELED, null);
            finish();

        }
    }

    public void getPasswordIndex(final PasswordIndexCallback callback) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = databaseReference.child("Users").child(uid).child("Passwords").child("Social networks").child("number of passwords");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int passwordIndex = 0; // Default value if the snapshot doesn't exist

                if (snapshot.exists()) {
                    passwordIndex = snapshot.getValue(Integer.class);
                }

                callback.onPasswordIndexRetrieved(passwordIndex);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "error retrieving user's data", Toast.LENGTH_SHORT).show();
            }
        });
    }


}