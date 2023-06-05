package com.project.passlock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PasswordsActivity extends AppCompatActivity implements View.OnClickListener {
    ListView lv;
    ArrayList<Password> passwordsList;
    PasswordAdapter passwordsAdapter;
    Password lastSelected;
    FloatingActionButton floatingActionButton;

    DatabaseReference firebaseDatabase;
    int itemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords);

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
                intent.putExtra("position", position);
                startActivityForResult(intent, 0);

            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
                lastSelected = passwordsAdapter.getItem(position);
                itemPosition = position;
                // Show the pop-up menu
                showPopupMenu(view);
                return true;
            }
        });

        /*Password p1 = new Password("Youtube", "password");
        Password p2 = new Password("Facebook", "password");
        Password p3 = new Password("Discord", "password");

        //phase 2 - add to array list
        passwordsList = new ArrayList<Password>();
        passwordsList.add(p1);
        passwordsList.add(p2);
        passwordsList.add(p3);*/

        passwordsList = new ArrayList<Password>();
        // Initialize the adapter
        passwordsAdapter = new PasswordAdapter(this, 0, 0, passwordsList);
        // Set the adapter to the ListView
        lv.setAdapter(passwordsAdapter);
        // Load passwords from Firebase database
        loadPasswordsFromFirebase();

    }

    private void loadPasswordsFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        DatabaseReference passwordsRef = firebaseDatabase
                .child("Users")
                .child(uid)
                .child("Passwords")
                .child("Social networks");

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
                passwordsAdapter.notifyDataSetChanged();
                Toast.makeText(this, "data updated", Toast.LENGTH_LONG).show();*/
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "canceled", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onClick(View v) {
        if (v == floatingActionButton) {
            Intent intent = new Intent(this, PasswordEditActivity.class);
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
                } else if (item.getItemId() == R.id.delete) {
                    // Handle option 2 click (Delete)
                    lastSelected = passwordsAdapter.getItem(itemPosition);
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    // Remove title and password from the database
                    DatabaseReference passwordRef = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Users")
                            .child(uid)
                            .child("Passwords")
                            .child("Social networks")
                            .child(Integer.toString(itemPosition));
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
}
