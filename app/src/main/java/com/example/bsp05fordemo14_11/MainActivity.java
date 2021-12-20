package com.example.bsp05fordemo14_11;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    static final String COMPLETED_ONBOARDING_PREF_NAME = "onboardingCompletion";

    private Button button_capture;
    private Button button_profile;

    private TextView textview_data;
    private Bitmap bitmap;

    private ImageView image_result;
    private ImageView image_popup;
    private TextView text_popup;
    private RelativeLayout popup_window_layout;

    private static final int REQUEST_CAMERA_CODE = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_capture = findViewById(R.id.button_capture);
        button_profile = findViewById(R.id.button_profile);
        textview_data = findViewById(R.id.text_data);
        image_result = findViewById(R.id.image_result);

        SharedPreferences sharedPreferences = getSharedPreferences(COMPLETED_ONBOARDING_PREF_NAME, 0);

        if (!sharedPreferences.getBoolean(this.COMPLETED_ONBOARDING_PREF_NAME, false)) {
            // The user hasn't seen the OnboardingSupportFragment yet, so show it
            startActivity(new Intent(getApplicationContext(),OnboardingActivity.class));
        }


        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, REQUEST_CAMERA_CODE);
        }

        button_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
            }
        });

        button_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    getTextFromImage(bitmap);
                    image_result.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getTextFromImage(Bitmap bitmap){
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if (!recognizer.isOperational()){
            Toast.makeText(MainActivity.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
        }
        else{
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0; i<textBlockSparseArray.size();i++){
                TextBlock textBlock = textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");
            }

            //textview_data.setText(stringBuilder.toString());
            button_capture.setText("Retake");
            //detectKeyword((String) textview_data.getText());
            detectKeyword((String) stringBuilder.toString());
        }
    }

    private void detectKeyword(String text) {

        /* https://stackoverflow.com/questions/5944987/how-to-create-a-popup-window-popupwindow-in-android */
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);
        ImageButton close_popup = popupView.findViewById(R.id.close_popup);
        image_popup = popupView.findViewById(R.id.image_popup);
        text_popup = popupView.findViewById(R.id.text_popup);
        popup_window_layout = popupView.findViewById(R.id.popup_window_layout);


        ArrayList<String> userAllergens = ProfileActivity.returnUserAllergens();
        Map<String, String[]> allergenKeywords = ProfileActivity.returnAllergens();

        Integer counter = 0;

        for(Map.Entry<String, String[]> entry : allergenKeywords.entrySet())
        {
            String key = entry.getKey();

            if(userAllergens.contains(key))
            {
                for (String value : entry.getValue())
                {
                    if (text.toLowerCase(Locale.ROOT).contains(value))
                    {
                        counter += 1;
                        String modifyText;
                        modifyText = text_popup.getText().toString();
                        modifyText = modifyText + String.format("Product contains %s!\n", key);
                        text_popup.setText(modifyText);
                        userAllergens.remove(new String(key));
                        break;
                    }
                }
            }
        }
        if(counter==0){
            text_popup.setText("Product does not seem to contain any of the selected allergens!");
            image_popup.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_24));
            popup_window_layout.setBackgroundColor(getResources().getColor(R.color.green_pastel));

        }else{
            image_popup.setImageDrawable(getDrawable(R.drawable.ic_baseline_warning_24));
            popup_window_layout.setBackgroundColor(getResources().getColor(R.color.red_pastel));
        }



        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(image_result, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        close_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }
}