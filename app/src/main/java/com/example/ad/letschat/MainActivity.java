package com.example.ad.letschat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mToolbar;
    private ViewPager mViewPage;
    private SectionsPageAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mViewPage = (ViewPager) findViewById(R.id.main_tabPager);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lets Chat");
        mSectionsPagerAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        mViewPage.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPage);

        if(mAuth.getCurrentUser() !=null)
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser curr_user = mAuth.getCurrentUser();
        if(curr_user == null){
            startActivity(new Intent(MainActivity.this,StartActivity.class));
            finish();
        }else{
            mUserRef.child("online").setValue("true");
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser curr_user = mAuth.getCurrentUser();
        if(curr_user!=null)
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this , StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch (item.getItemId()){
             case R.id.main_logout_btn:
                 mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
                 FirebaseAuth.getInstance().signOut();
                 sendToStart();
                 return true;
             case R.id.main_account_setting:
                 startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                 return true;
             case R.id.main_all_users:
                 startActivity(new Intent(MainActivity.this,UsersActivity.class));
                 return true;

                 default:
                     return true;
         }

    }
}
