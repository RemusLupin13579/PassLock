package com.project.passlock;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    int mode = 0;//0=add mode, 1=edit mode
    ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMenuDrawerBinding binding;

    TextView textViewUserName;
    String fname;

    FloatingActionButton floatingActionButton;
    int itemPosition;
    DatabaseReference firebaseDatabaseRef;
    int numberOfCategories;

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

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance("https://paock-2a77c-default-rtdb.europe-west1.firebasedatabase.app");
        firebaseDatabase = FirebaseDatabase.getInstance();
        supportInvalidateOptionsMenu();
        firebaseDatabaseRef = FirebaseDatabase.getInstance().getReference();

        lv = (ListView) findViewById(R.id.lv);
        progressDialog = new ProgressDialog(this);


        //welcome messages with user's name
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
                    Intent intent = new Intent(MainActivity.this, PasswordGenerator.class);
                    startActivity(intent);
                }
                if (item.getItemId() == R.id.nav_logout) {
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


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelected = categoriesAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, PasswordsActivity.class);
                intent.putExtra("categoryName", lastSelected.getTitle());
                intent.putExtra("position", position);
                //startActivity(intent);
                startActivityForResult(intent, 0);

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
                showPopupMenu(view);
                return true;
            }
        });
        lv.setAdapter(categoriesAdapter);
        loadCategoriesFromFirebase();
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
    public void onClick(View v) {
        if (v == floatingActionButton) {
            Intent intent = new Intent(this, EditActivity.class);
            startActivityForResult(intent, 1);
        }
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
                    Intent intent = new Intent(MainActivity.this, EditActivity.class);
                    intent.putExtra("title", lastSelected.getTitle());
                    intent.putExtra("position", itemPosition);
                    startActivityForResult(intent, 0);
                }
                if (item.getItemId() == R.id.delete) {
                    // Handle option 2 click (Delete)
                    lastSelected = categoriesAdapter.getItem(itemPosition);
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    // Remove title and password from the database
                    DatabaseReference passwordRef = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Users")
                            .child(uid)
                            .child("Passwords")
                            .child("Categories")
                            .child(String.valueOf(itemPosition))
                            .child("category");
                    passwordRef.removeValue();

                    lastSelected=categoriesAdapter.getItem(itemPosition);
                    categoriesAdapter.remove(lastSelected);
                    categoriesAdapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

}