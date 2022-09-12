package com.viatom.a20ftest;

import android.os.Bundle;

import com.viatom.a20ftest.databinding.ActivityDashboardBinding;


public class DashboardActivity extends DraweBaseActivity {

    ActivityDashboardBinding activityDashboardBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDashboardBinding = ActivityDashboardBinding.inflate(getLayoutInflater());
        allocateActivityTitle("Dashboard");
        setContentView(activityDashboardBinding.getRoot());

        

    }


}