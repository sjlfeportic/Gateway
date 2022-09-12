package com.viatom.a20ftest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.viatom.a20ftest.databinding.ActivityMedicoesBinding;

public class Medicoes extends DraweBaseActivity {


    ActivityMedicoesBinding activityMedicoesBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMedicoesBinding = ActivityMedicoesBinding.inflate(getLayoutInflater());
        setContentView(activityMedicoesBinding.getRoot());
        allocateActivityTitle("Medicoes");
    }
}