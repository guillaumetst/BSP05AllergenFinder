package com.example.bsp05fordemo14_11;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private Button button_capture;
    private Button button_profile;

    private ImageView image_result;
    private ImageView image_popup;
    private TextView text_popup;
    private RelativeLayout popup_window_layout;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private ImageCapture imageCapture;

    private static final int CAMERA_PERMISSION_CODE = 100;

    static final String COMPLETED_ONBOARDING_PREF_NAME = "onboardingCompletion";

    /* To be run on start-up */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        /* Button and Layout Setup */
        setContentView(R.layout.activity_main);

        button_capture = findViewById(R.id.button_capture);
        button_profile = findViewById(R.id.button_profile);
        image_result = findViewById(R.id.image_result);

        /* Onboarding Setup*/
        SharedPreferences sharedPreferences = getSharedPreferences(COMPLETED_ONBOARDING_PREF_NAME, 0);

        if (!sharedPreferences.getBoolean(COMPLETED_ONBOARDING_PREF_NAME, false)) {
            // The user hasn't seen the OnboardingSupportFragment yet, so show it
            startActivity(new Intent(getApplicationContext(),OnboardingActivity.class));
        }

        /* Camera Setup */
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        previewView = findViewById(R.id.preview_view);

        checkPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());

        image_result.setImageBitmap(null);

        button_capture.setOnClickListener(v -> capturePhoto());

        button_profile.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),ProfileActivity.class))
        );
    }

    public void checkPermission(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            } else {
                Toast.makeText(MainActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
    }

    private void capturePhoto() {
        String imagePath = getExternalCacheDir() + File.separator + System.currentTimeMillis() + ".jpg";
        File image = new File(imagePath);

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(image).build();

        imageCapture.takePicture(outputFileOptions, getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        Log.d("PHOTO", "onImageSaved: "+imagePath);
                        Toast.makeText(MainActivity.this, "Photo has been taken", Toast.LENGTH_SHORT).show();
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        bitmap = RotateBitmap(bitmap, 90);
                        image_result.setImageBitmap(bitmap);
                        getTextFromImage(bitmap);

                        if (image.exists()) {
                            if (image.delete()) {
                                System.out.println("file Deleted :" + imagePath);
                            } else {
                                System.out.println("file not Deleted :" + imagePath);
                            }
                        }
                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.d("PHOTO", "onError: " + error);
                        Toast.makeText(MainActivity.this, "Error taking photo: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

            button_capture.setText("Retake");
            Log.d("DETECTION", "Text: "+stringBuilder.toString());
            detectKeyword((String) stringBuilder.toString());
        }
    }

    private void detectKeyword(String text) {

        /* Inflate the layout of the popup window */
        /* https://stackoverflow.com/questions/5944987/how-to-create-a-popup-window-popupwindow-in-android */
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);
        ImageButton close_popup = popupView.findViewById(R.id.close_popup);
        image_popup = popupView.findViewById(R.id.image_popup);
        text_popup = popupView.findViewById(R.id.text_popup);
        popup_window_layout = popupView.findViewById(R.id.popup_window_layout);


        ArrayList<String> userAllergens = ProfileActivity.returnUserAllergens();
        ArrayList<String> detected = new ArrayList<>();
        Map<String, String[]> allergenKeywords = ProfileActivity.returnAllergens();
        int counter = 0;

        for(Map.Entry<String, String[]> entry : allergenKeywords.entrySet())
        {
            String key = entry.getKey();
            if(userAllergens.contains(key) && !detected.contains(key))
            {
                for (String value : entry.getValue()) {
                    if (text.toLowerCase(Locale.ROOT).contains(value))
                    {
                        counter += 1;
                        String modifyText;
                        modifyText = text_popup.getText().toString();
                        modifyText = modifyText + String.format("Product contains %s!\n", key);
                        text_popup.setText(modifyText);
                        detected.add(key);
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

        /* Create the popup window */
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        //boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height/*, focusable*/);

        /* Show the popup window */
        popupWindow.showAtLocation(image_result, Gravity.CENTER, 0, 0);

        /* Dismiss the popup window when touched */
        close_popup.setOnClickListener(v -> {
            popupWindow.dismiss();
            image_result.setImageBitmap(null);
        });
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        /* Camera selector */
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        /* Preview use case */
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        /* Image capture use case */
        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();

        /* Bind to lifecycle */
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}