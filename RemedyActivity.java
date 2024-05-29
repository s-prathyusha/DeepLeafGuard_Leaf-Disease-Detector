package com.example.plant_leaf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class RemedyActivity extends AppCompatActivity {

    TextView disease,cause,accuracy,remedy;
    Toolbar toolbar;
    ImageView leaf;
    String strDisease;
    Float strAccuracy;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remedy);

        // Set the default night mode to follow the system setting
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        leaf = findViewById(R.id.leaf);
        // Find the CardView
        CardView cardView = findViewById(R.id.card);

        // Set the background color of the CardView to white
        cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
        disease = findViewById(R.id.disease);
        cause = findViewById(R.id.cause);
        accuracy = findViewById(R.id.accuracy);
        remedy = findViewById(R.id.remedies);

        toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        if (getSupportActionBar() == null) {
            // Set the action bar only if it hasn't been supplied by the window decor
            setSupportActionBar(toolbar); // Replace "toolbar" with your Toolbar reference
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        strDisease = getIntent().getStringExtra("disease");
        strAccuracy = getIntent().getFloatExtra("accuracy",0);

        Bitmap image = getIntent().getParcelableExtra("image");
        leaf.setImageBitmap(image);
        disease.setText(strDisease);
        accuracy.setText(String.format("%s%%", strAccuracy));
        getRemedies();

    }

    private void getRemedies(){
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset(getApplicationContext()));
//            System.out.println(obj);
            JSONArray dis = obj.getJSONArray(strDisease);
            String causeString = dis.getJSONObject(0).getString("Causes");
            String remediesString = dis.getJSONObject(1).getString("Remedies");

//            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
//                // Dark mode
//                remedy.setTextColor(getResources().getColor(R.color.white)); // Change to your dark mode text color
//                cause.setTextColor(getResources().getColor(R.color.white));
//                disease.setTextColor(getResources().getColor(R.color.white)); // Change to your dark mode text color
//                accuracy.setTextColor(getResources().getColor(R.color.white));
//            }
            cause.setText(causeString);
            remedy.setText(remediesString);
        } catch (JSONException e) {
            e.printStackTrace();
            cause.setText("N/A");
            remedy.setText("N/A");
        }
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("causesremedies.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}