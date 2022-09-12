package com.viatom.a20ftest;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class DraweBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;

    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawe_base, null);
        FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(drawerLayout);

        Toolbar toolbar = drawerLayout.findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = drawerLayout.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.menu_drawer_open, R.string.menu_drawer_close );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.nav_dispositvos:
                startActivity(new Intent(this, Dispositivos.class));
                overridePendingTransition(0,0);
                break;
            case R.id.nav_health:
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0,0);
                break;
            case R.id.nav_history:
                startActivity(new Intent(this, LivehistoryActivity.class));
                overridePendingTransition(0,0);
                break;
            case R.id.nav_historyPr:
                startActivity(new Intent(this, LivePRhistory.class));
                overridePendingTransition(0,0);
                break;
        }

        return false;
    }
    protected void allocateActivityTitle(String titleString){

        if (getSupportActionBar() !=null){
            getSupportActionBar().setTitle(titleString);

        }
    }

}