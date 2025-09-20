package com.ex.realcv;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import 	java.util.concurrent.ExecutionException;

//custom
import com.ex.realcv.Func.LibCv;

// Java util

//cameraX
import androidx.camera.core.Camera;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.lifecycle.LifecycleOwner;
import androidx.camera.view.PreviewView;

// Executor (스레드 실행기)


public class FaceActivity  extends MainActivity {

    private static final String TAG = "OpenCV";

    private final String[] REQ_PERMS = new String[]{Manifest.permission.CAMERA};
    private static final int REQ_CODE = 1001;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    //사진첩 열기
    private ActivityResultLauncher<Intent> galleryLauncher;

    //init custom Func


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face);

        //add button eventListener
        //--choose
        findViewById(R.id.ChooseBtn).setOnClickListener(v -> {

            ImageView imageView = findViewById(R.id.fa_imageView);
        /*    galleryLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                            //Uri selectedImageUri = result.getData().getData();
                            //imageView.setImageURI(selectedImageUri); // ✅ 바로 보여주기
                        }
                    }
           ); */

           // SetUPImage();
        });
        //--scan
        findViewById(R.id.ScanBtn).setOnClickListener(v -> {
            if (allGranted()) {
                startCamera();
            } else {
                ActivityCompat.requestPermissions(this, REQ_PERMS, REQ_CODE);
            }
        });
        //--back
        findViewById(R.id.BackBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });



        // 1) 이미지 불러오기 (drawable/sample.jpg)
       // Bitmap srcBmp = BitmapFactory.decodeResource(getResources(), R.drawable.bluepolo);
       // Bitmap outlined = LibCv.outlineBlueCloth(srcBmp);
      //  ((ImageView)findViewById(R.id.fa_imageView)).setImageBitmap(outlined);

        //Camera


    }

    private boolean allGranted() {
        for (String p : REQ_PERMS)
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }


    private void SetUPImage(){

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }


    private void startCamera() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        PreviewView viewFinder = findViewById(R.id.previewView);
        viewFinder.setVisibility(View.VISIBLE);
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
    }

}
