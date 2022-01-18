package com.example.bsp05fordemo14_11;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

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

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private ImageCapture imageCapture;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_WRITE_PERMISSION_CODE = 101;
    private static final int STORAGE_READ_PERMISSION_CODE = 102;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        button_capture = findViewById(R.id.button_capture);
        button_profile = findViewById(R.id.button_profile);
        textview_data = findViewById(R.id.text_data);
        image_result = findViewById(R.id.image_result);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        previewView = findViewById(R.id.preview_view);

        SharedPreferences sharedPreferences = getSharedPreferences(COMPLETED_ONBOARDING_PREF_NAME, 0);

        checkPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE);
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,STORAGE_WRITE_PERMISSION_CODE);

        if (!sharedPreferences.getBoolean(COMPLETED_ONBOARDING_PREF_NAME, false)) {
            // The user hasn't seen the OnboardingSupportFragment yet, so show it
            startActivity(new Intent(getApplicationContext(),OnboardingActivity.class));
        }

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();;
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());


        button_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
            }
        });

//        image_result.setImageBitmap(null);
//
//        button_capture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                capturePhoto();
//            }
//        });

        button_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
            }
        });
    }

    public void checkPermission(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
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
        else if (requestCode == STORAGE_WRITE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Storage Write Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Storage Write Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == STORAGE_READ_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Storage Write Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Storage Write Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String capturePhoto() {
        String imagePath = getExternalCacheDir() + File.separator + System.currentTimeMillis() + ".jpg";
        File image = new File(imagePath);

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(image).build();

        imageCapture.takePicture(outputFileOptions, getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        // insert your code here.
                        Log.d("PHOTO", "onImageSaved: "+imagePath);
                        Toast.makeText(MainActivity.this, "Photo has been taken", Toast.LENGTH_SHORT).show();
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        getTextFromImage(bitmap);
                        image_result.setImageBitmap(bitmap);
//                        image_result.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                        image_result.setRotation(90);
                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        // insert your code here.
                        Log.d("PHOTO", "onError: " + error);
                        Toast.makeText(MainActivity.this, "Error taking photo: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        return imagePath;
    }

//    private void capturePhoto() {
//
//        File photoDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        if(!photoDir.exists()){
//            this.finishAffinity();
//        }
//
//        File appPhotoDir = new File(photoDir + "/AllergenApp");
//
//        if(!appPhotoDir.exists())
//            appPhotoDir.mkdir();
//
//        Date date = new Date();
//        String timestamp = String.valueOf(date.getTime());
////        String photoFilePath = photoDir.getAbsolutePath() + "/" + timestamp + ".jpg";
//        String filename = "/" + timestamp + ".jpg";
//
//        File photoFile = new File(appPhotoDir, filename);
//
////        File photoFile = new File(photoFilePath);
//
//        imageCapture.takePicture(
//                new ImageCapture.OutputFileOptions.Builder(photoFile).build(),
//                getExecutor(),
//                new ImageCapture.OnImageSavedCallback() {
//                    @Override
//                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                        Toast.makeText(MainActivity.this, "Photo has been taken", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCaptureException exception) {
//                        Toast.makeText(MainActivity.this, "Error taking photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );
//
//        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
//        getTextFromImage(bitmap);
//        image_result.setImageBitmap(bitmap);

//        return photoFilePath;
// }

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
            if (requestCode == 7 && resultCode == RESULT_OK) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                image_result.setImageBitmap(bitmap);
            }
        }
    }

//    public void capturePicture() {
//        File photoFile = null;
//        try {
//            photoFile = createImageFile();
//        } catch (IOException ex) {
//            Toast.makeText(MainActivity.this, "Error creating photo File: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//        if (photoFile != null) {
//            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
//            imageCapture.takePicture(outputFileOptions, getExecutor(),
//                    new ImageCapture.OnImageSavedCallback() {
//                        @Override
//                        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
//                            getTextFromImage(outputFileResults);
//                        }
//
//                        @Override
//                        public void onError(ImageCaptureException error) {
//                            Toast.makeText(MainActivity.this, "Error taking photo: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//            );
//        }
//    }

//    String currentPhotoPath;
//
//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        currentPhotoPath = image.getAbsolutePath();
//        return image;
//    }


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
        //boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height/*, focusable*/);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(image_result, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        close_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                image_result.setImageBitmap(null);
            }
        });
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        // Camera selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        // Preview use case
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

}