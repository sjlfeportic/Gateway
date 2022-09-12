package com.viatom.a20ftest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.viatom.a20ftest.databinding.ActivityDispositivosBinding;

public class Dispositivos extends DraweBaseActivity {


    ActivityDispositivosBinding activityDispositivosBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDispositivosBinding = ActivityDispositivosBinding.inflate(getLayoutInflater());
        setContentView(activityDispositivosBinding.getRoot());
        allocateActivityTitle("Dispositivos");
    }
}