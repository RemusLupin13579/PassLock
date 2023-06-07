package com.project.passlock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

interface CategoryEditIndexCallback {
    void onCategoryIndexRetrieved(int passwordIndex);
}

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etTitle;
    Button btnSave, btnCancel;
    private DatabaseReference firebaseDatabase;
    int mode = 0;//0=add mode, 1=edit mode
    int position;
    String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        etTitle = (EditText) findViewById(R.id.etTitle);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        //connect to intent if its edit mode
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            mode = 1;
            position = intent.getExtras().getInt("position");
            title = intent.getExtras().getString("title");
            etTitle.setText(title);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Result", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View v) {
        if (btnSave == v) {//option 2 - save the data and go to first screen
            if (mode == 0) {//add mode
                if (etTitle.getText().toString().length() > 0) {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
                    getCategoryIndex(new CategoryEditCallback() {//קורא את הערך של מספר קטגוריות שמורות, מחזיר 0 אם אין ערך כזה
                        @Override
                        public void onCategoryIndexRetrieved(int categoryIndex) {
                            /*firebaseDatabase.child("Users")
                                    .child(uid)
                                    .child("Passwords")
                                    .child("Categories")
                                    .child(Integer.toString(categoryIndex))
                                    .setValue(etTitle.getText().toString());*/
                            firebaseDatabase.child("Users")
                                    .child(uid)
                                    .child("Passwords")
                                    .child("Categories")
                                    .child(Integer.toString(categoryIndex))
                                    .child("category")
                                    .setValue(etTitle.getText().toString());

                            firebaseDatabase.child("Users")
                                    .child(uid)
                                    .child("Passwords")
                                    .child("Categories")
                                    .child("number of categories")
                                    .setValue(categoryIndex + 1);
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                            /*firebaseDatabase.child("Users")
                                    .child(uid)
                                    .child("Passwords")
                                    .child("Categories")
                                    .child(Integer.toString(categoryIndex)).child("title").setValue(etTitle.getText().toString());
                            firebaseDatabase.child("Users")
                                    .child(uid)
                                    .child("Passwords")
                                    .child("Categories")
                                    .child("number of categories")
                                    .setValue(categoryIndex + 1);
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();*/
                        }
                    });
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
                }
            }
            if (mode == 1 && btnSave == v) {//edit mode
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
                firebaseDatabase
                        .child("Users")
                        .child(uid)
                        .child("Passwords")
                        .child("Categories")
                        .child(String.valueOf(position))
                        .child("category")
                        .setValue(etTitle.getText().toString());

                firebaseDatabase
                        .child("Users")
                        .child(uid)
                        .child("Passwords")
                        .child("Categories")
                        .child(String.valueOf(position)).child("category").setValue(etTitle.getText().toString()); new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error == null) {
                            Toast.makeText(getApplicationContext(), "Category updated successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent();
                            intent.putExtra("oldTitle", etTitle.getText().toString());
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to update category", Toast.LENGTH_LONG).show();
                        }
                    }
                };

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();


            }
        } else if (btnCancel == v)//option 2 - cancel-  and go to first screen

        {
            setResult(RESULT_CANCELED, null);
            finish();
        }
    }




    public void getCategoryIndex(final CategoryEditCallback callback) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = databaseReference.child("Users")
                .child(uid)
                .child("Passwords")
                .child("Categories")
                .child("number of categories");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int categoryIndex = 0; // Default value if the snapshot doesn't exist

                if (snapshot.exists()) {
                    categoryIndex = snapshot.getValue(Integer.class);
                }

                callback.onCategoryIndexRetrieved(categoryIndex);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "error retrieving user's data", Toast.LENGTH_SHORT).show();
            }
        });

    }

}

