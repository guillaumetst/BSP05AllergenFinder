package com.example.bsp05fordemo14_11;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "switch_preferences";
    Button button_confirm;
    SwitchCompat switch_gluten;
    SwitchCompat switch_lactose;
    SwitchCompat switch_eggs;

    TextView textView_test;

    private static List<String> allergens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        allergens = new ArrayList<String>();

        button_confirm = findViewById(R.id.button_confirm);
        switch_gluten = findViewById(R.id.switch_gluten);
        switch_lactose = findViewById(R.id.switch_lactose);
        switch_eggs = findViewById(R.id.switch_eggs);

        textView_test = findViewById(R.id.textView_test);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean silent_gluten = settings.getBoolean("switch_gluten", false);
        switch_gluten.setChecked(silent_gluten);
        //Log.d("PREF_SWITCH_GLUTEN", String.valueOf(silent_gluten));
        //Log.d("REAL_SWITCH_GLUTEN", String.valueOf(switch_gluten));
        if (switch_gluten.isChecked()){ allergens.add("gluten");}

        boolean silent_lactose = settings.getBoolean("switch_lactose", false);
        switch_lactose.setChecked(silent_lactose);
        if (switch_lactose.isChecked()){ allergens.add("lactose");}

        boolean silent_eggs = settings.getBoolean("switch_eggs", false);
        switch_eggs.setChecked(silent_eggs);
        if (switch_eggs.isChecked()){ allergens.add("eggs");}


        refreshAllergens();


        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        switch_gluten.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(isChecked){
                    allergens.add("gluten");
                }
                else{
                    allergens.remove("gluten");
                }
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("switch_gluten", isChecked);
                editor.apply();
                refreshAllergens();
            }
        });

        switch_lactose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(isChecked){
                    allergens.add("lactose");
                }
                else{
                    allergens.remove("lactose");
                }
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("switch_lactose", isChecked);
                editor.apply();
                refreshAllergens();
            }
        });

        switch_eggs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(isChecked){
                    allergens.add("eggs");
                }
                else{
                    allergens.remove("eggs");
                }
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("switch_eggs", isChecked);
                editor.apply();
                refreshAllergens();
            }
        });
    }

    public void refreshAllergens(){
        StringBuffer sb = new StringBuffer();

        if (!allergens.isEmpty()) {
            for (String s : allergens) {
                sb.append(s);
                sb.append(",");
            }
            String str = sb.toString();

            textView_test.setText(str);
        }
        else {
            textView_test.setText("No allergens have been set!");

        }
    }
}
