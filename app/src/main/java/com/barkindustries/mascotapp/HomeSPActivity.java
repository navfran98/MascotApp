package com.barkindustries.mascotapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.mapbox.mapboxsdk.Mapbox;

public class HomeSPActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private String serviceType;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mapbox.getInstance(this, getString(R.string.access_token));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sp);

        serviceType = getIntent().getStringExtra("Type");

        Toolbar toolbar = findViewById(R.id.toolbar_sp);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_sp);
        NavigationView navigationView = findViewById(R.id.nav_view_sp);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home_sp, R.id.nav_clients_sp, R.id.nav_maps_sp, R.id.nav_chat_with_vets)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_sp);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_logout_sp:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeSPActivity.this, LogInActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_sp, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_sp);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public String getServiceType() {
        return serviceType;
    }
}
