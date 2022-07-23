package com.example.whatsup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mUserRef;
    private String currentUserID;

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Whats Up");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference();
        }

        mAuth = FirebaseAuth.getInstance();

        mViewPager = findViewById(R.id.tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager) ;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
        {
            sendToStart();
        }
        else
        {
            updateStatus("online");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            updateStatus("offline");
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            updateStatus("offline");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            updateStatus("offline");
        }
    }

    private void sendToStart()
    {
        Intent i = new Intent(MainActivity.this,StartActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_btn){
            updateStatus("offline");
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if (item.getItemId() == R.id.main_settings_btn){
            Intent i = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(i);
        }
        if (item.getItemId() == R.id.main_all_btn){
            Intent i = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(i);
        }

        return true;
    }

    private void updateStatus(String state)
    {
        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("online", state);

        currentUserID = mAuth.getCurrentUser().getUid();
        mUserRef.child("Users").child(currentUserID).updateChildren(onlineState);
    }

}
