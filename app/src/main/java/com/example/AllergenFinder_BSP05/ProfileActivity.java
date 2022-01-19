package com.example.AllergenFinder_BSP05;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
    SwitchCompat switch_moluscs;
    SwitchCompat switch_fish;
    SwitchCompat switch_soy;
    SwitchCompat switch_nuts;

    TextView textView_test;

    private static List<String> userAllergens;
    static Map<String, String[]> allergenKeywords = new HashMap<String, String[]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        button_confirm = findViewById(R.id.button_confirm);

        userAllergens = new ArrayList<String>();
        Map<SwitchCompat, String> allergenTypes = new HashMap<SwitchCompat, String>();

        switch_gluten = findViewById(R.id.switch_gluten);
        switch_lactose = findViewById(R.id.switch_lactose);
        switch_eggs = findViewById(R.id.switch_eggs);
        switch_moluscs = findViewById(R.id.switch_moluscs);
        switch_fish = findViewById(R.id.switch_fish);
        switch_soy = findViewById(R.id.switch_soy);
        switch_nuts = findViewById(R.id.switch_nuts);

        allergenTypes.put(switch_gluten, "gluten");
        allergenTypes.put(switch_lactose, "lactose");
        allergenTypes.put(switch_eggs, "eggs");
        allergenTypes.put(switch_moluscs, "moluscs");
        allergenTypes.put(switch_fish, "fish");
        allergenTypes.put(switch_soy, "soy");
        allergenTypes.put(switch_nuts, "nuts");

        allergenKeywords.put("gluten", new String[] {"gluten", "wheat", "flour", "barley", "buckwheat", "buck-wheat", "rye", "cereal"});
        allergenKeywords.put( "lactose", new String[] {"lactose", "milk", "butter", "buttermilk", "casein", "cheese", "cream", "custard", "ice-cream", "sour-cream", "whey", "yogurt"});
        allergenKeywords.put( "eggs", new String[] {"eggs", "poultry", "egg-white", "yolk"});
        allergenKeywords.put( "moluscs", new String[] {"moluscs"});
        allergenKeywords.put( "fish", new String[] {"fish", "eel", "globfish", "mackerel", "percifomes", "salmon", "bass", "bream", "trout", "tuna", "tetraodontiformes", "shellfish", "shell"});
        allergenKeywords.put( "soy", new String[] {"soy", "soybeans", "soy-beans"});
        allergenKeywords.put( "nuts", new String[] {"nuts", "peanut", "almond", "chestnut", "ginkgo", "pecan", "walnut", "nut"});

        textView_test = findViewById(R.id.textView_test);


        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        boolean silent_gluten = settings.getBoolean("switch_gluten", false);
        switch_gluten.setChecked(silent_gluten);
        if (switch_gluten.isChecked()){ userAllergens.add("gluten");}

        boolean silent_lactose = settings.getBoolean("switch_lactose", false);
        switch_lactose.setChecked(silent_lactose);
        if (switch_lactose.isChecked()){ userAllergens.add("lactose");}

        boolean silent_eggs = settings.getBoolean("switch_eggs", false);
        switch_eggs.setChecked(silent_eggs);
        if (switch_eggs.isChecked()){ userAllergens.add("eggs");}

        boolean silent_moluscs = settings.getBoolean("switch_moluscs", false);
        switch_moluscs.setChecked(silent_moluscs);
        if (switch_moluscs.isChecked()){ userAllergens.add("moluscs");}

        boolean silent_fish = settings.getBoolean("switch_fish", false);
        switch_fish.setChecked(silent_fish);
        if (switch_fish.isChecked()){ userAllergens.add("fish");}

        boolean silent_soy = settings.getBoolean("switch_soy", false);
        switch_soy.setChecked(silent_soy);
        if (switch_soy.isChecked()){ userAllergens.add("soy");}

        boolean silent_nuts = settings.getBoolean("switch_nuts", false);
        switch_nuts.setChecked(silent_nuts);
        if (switch_nuts.isChecked()){ userAllergens.add("nuts");}


        refreshAllergens();


        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });


        for(Map.Entry<SwitchCompat,String> entry : allergenTypes.entrySet()){
            entry.getKey().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                    if(isChecked){
                        userAllergens.add(entry.getValue());
                    }
                    else{
                        userAllergens.remove(entry.getValue());
                    }
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("switch_"+entry.getValue(), isChecked);
                    editor.apply();
                    refreshAllergens();
                }
            });
        }
    }

    public void refreshAllergens(){
        StringBuffer sb = new StringBuffer();

        if (!userAllergens.isEmpty()) {
            for (String s : userAllergens) {
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

    public static ArrayList<String> returnUserAllergens(){ return (ArrayList<String>) userAllergens; }

    public static Map<String, String[]> returnAllergens(){
        return allergenKeywords;
    }
}
