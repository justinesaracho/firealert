package com.example.raha_firealert;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.raha_firealert.Login;
import com.example.raha_firealert.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Dashboard extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private SharedPreferences myprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener((View v) -> {
//            Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
//        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("new_check",token);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                View view = Dashboard.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                View view = Dashboard.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_alert,
                R.id.nav_announcement,
                R.id.nav_alerts,
                R.id.nav_ViewReports,
//                R.id.nav_SystemLogs,
                R.id.nav_myprofile,
                R.id.nav_tips,
                R.id.nav_reportproblem,
                R.id.nav_signout
        )
                .setDrawerLayout(drawer)
                .build();

        myprofile = getSharedPreferences(Login.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String role = myprofile.getString("role","");


//        navigationView.getMenu().findItem(R.id.nav_myprofile).setVisible(false);


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        if (role.equals("administrator")){
//            PushNotifications.start(this, "7c8a90bf-9d1a-4d66-bdf1-a3cd437457bb");
//            PushNotifications.addDeviceInterest("alert");
            FirebaseMessaging.getInstance().subscribeToTopic("alert");
            navigationView.getMenu().findItem(R.id.nav_alert).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_myprofile).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_reportproblem).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_tips).setVisible(false);
            navController.navigate(R.id.nav_announcement);

        }
        else{
            FirebaseMessaging.getInstance().subscribeToTopic("announcement");
//            PushNotifications.start(this, "7c8a90bf-9d1a-4d66-bdf1-a3cd437457bb");
//            PushNotifications.addDeviceInterest("announcement");
            navigationView.getMenu().findItem(R.id.nav_ViewReports).setVisible(false);
//            navigationView.getMenu().findItem(R.id.nav_SystemLogs).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_alerts).setVisible(false);
        }


        String from_view = getIntent().getStringExtra("from_activity");
        if (from_view != null){
            if (from_view.equals("tips")){
                navController.navigate(R.id.nav_tips);
            }
            else if (from_view.equals("announcement")){
                navController.navigate(R.id.nav_announcement);
            }
            else if (from_view.equals("myprofile")){
                navController.navigate(R.id.nav_myprofile);
            }
            else if (from_view.equals("alert_notif") || from_view.equals("alertrespond")){
                navController.navigate(R.id.nav_alerts);
            }
            else if (from_view.equals("announcement_notif")){
                navController.navigate(R.id.nav_announcement);
            }
        }



        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        View hView = navigationView.getHeaderView(0);
        TextView tv_name = hView.findViewById(R.id.tv_name_id);
        TextView tv_email = hView.findViewById(R.id.tv_email_id);

        myprofile = getSharedPreferences(Login.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String name = myprofile.getString("name","");
        String email = myprofile.getString("email","");
        tv_name.setText(name);
        tv_email.setText(email);




        Log.d("check",myprofile.getString("id",""));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
