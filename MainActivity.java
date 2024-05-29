package com.example.plant_leaf;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    Timer timer;
    Button btnStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
        });


    }
}