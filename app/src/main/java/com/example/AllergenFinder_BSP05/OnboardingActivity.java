package com.example.AllergenFinder_BSP05;

import android.os.Bundle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {
    private Integer counter = 0;
    private Button onboarding_button;
    private ImageView onboarding_image;

    static final String COMPLETED_ONBOARDING_PREF_NAME = "onboardingCompletion";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        SharedPreferences sharedPreferences = getSharedPreferences(COMPLETED_ONBOARDING_PREF_NAME, 0);

        onboarding_button = findViewById(R.id.onboardingButton);
        onboarding_image = findViewById(R.id.onboardingImage);

        onboarding_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (counter == 0) {
                    onboarding_image.setImageDrawable(getDrawable(R.drawable.onboarding2));
                    counter += 1;
                } else if (counter == 1) {
                    onboarding_image.setImageDrawable(getDrawable(R.drawable.onboarding3));
                    counter += 1;
                } else if (counter == 2) {
                    onboarding_image.setImageDrawable(getDrawable(R.drawable.onboarding4));
                    onboarding_button.setText("I understand");
                    counter += 1;
                } else{
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(COMPLETED_ONBOARDING_PREF_NAME, true);
                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                }
            }
        });
    }
}




