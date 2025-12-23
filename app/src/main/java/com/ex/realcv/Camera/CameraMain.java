package com.ex.realcv.Camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ex.realcv.Func.NetworkChecker;
import com.ex.realcv.MainActivity;
import com.ex.realcv.MemoMain.MemoBase;
import com.ex.realcv.MyApplication;
import com.ex.realcv.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

import com.ex.realcv.Camera.CameraController;
import com.google.mlkit.vision.common.InputImage;

public class CameraMain extends MainActivity {

    private PreviewView previewView;
    private ImageView ivCaptured;
    private TextView tvOcrText;
    //private TextView tvTranslatedText;
    private CardView cardOcrText;
   // private CardView cardTranslatedText;

    private Button btnCapture;
    private Button btnReset;
    private Button backToMainBtn;

    private CameraController cameraController;

    //OCR
    private OcrEngine ocrJapanEngine;
    private JapaneseOcr japaneseOcr;
    private ProgressBar progressOcr;
    private FrameLayout loadingOverlay;

    private MyApplication.AppContainer container;
    private TranslatorService translator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_translate);

        // XML 뷰들 연결
        previewView       = findViewById(R.id.previewView);
        ivCaptured        = findViewById(R.id.ivCaptured);
        tvOcrText         = findViewById(R.id.tvOcrText);
        //tvTranslatedText  = findViewById(R.id.tvTranslatedText);
        cardOcrText       = findViewById(R.id.cardOcrText);
        //cardTranslatedText= findViewById(R.id.cardTranslatedText);

        loadingOverlay = findViewById(R.id.loadingOverlay);

        btnCapture   = findViewById(R.id.btnCapture);
        btnReset = findViewById(R.id.btnReset);
        backToMainBtn= findViewById(R.id.backToMain);


        //MyApplication
        container = ((MyApplication) getApplication()).appContainer;
        translator = pickTranslator();


        cameraController = new CameraController(this, this, previewView);
        ocrJapanEngine = container.ocrJapaneEngine;
        //japaneseOcr = new JapaneseOcr();
        //ui & camera start
        enterCaptureMode();


        if (allPermissionsGranted()) {
            cameraController.startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 100);
        }

        backToMainBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });

        btnCapture.setOnClickListener(v -> {
            cameraController.takePhoto(new CameraController.OnPhotoCapturedListener() {
                @ExperimentalGetImage
                @Override
                public void onCaptured(ImageProxy image) {

                    enterResultMode(image);

                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(CameraMain.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnReset.setOnClickListener(v -> {

            updateUiForResult(false);
            cameraController.startCamera();

        });


    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }


    private void updateUiForResult(boolean showResult) {

        if (showResult) {
            // 결과 모드
            previewView.setVisibility(View.GONE);
            btnCapture.setVisibility(View.GONE);

            loadingOverlay.setVisibility(View.VISIBLE);

            ivCaptured.setVisibility(View.VISIBLE);
            cardOcrText.setVisibility(View.VISIBLE);
            //cardTranslatedText.setVisibility(View.VISIBLE);
            btnReset.setVisibility(View.VISIBLE);

        } else {
            // 촬영 모드
            loadingOverlay.setVisibility(View.GONE);

            previewView.setVisibility(View.VISIBLE);
            btnCapture.setVisibility(View.VISIBLE);

            ivCaptured.setVisibility(View.GONE);
            cardOcrText.setVisibility(View.GONE);
            //cardTranslatedText.setVisibility(View.GONE);
            btnReset.setVisibility(View.GONE);

            // 텍스트도 초기화
            tvOcrText.setText("");
          //  tvTranslatedText.setText("");
        }
    }

    private void enterCaptureMode() {
        updateUiForResult(false);
        cameraController.startCamera();
    }
    @ExperimentalGetImage
    private void enterResultMode(ImageProxy image) {
        try{
            updateUiForResult(true);

            Image mediaImage = image.getImage();
            /*if (mediaImage != null) {
                return;
            }*/
            int rotation = image.getImageInfo().getRotationDegrees();

            // 화면용 Bitmap
            Bitmap bitmap = cameraController.imageProxyToBitmap(image);
            Bitmap rotated = cameraController.rotateBitmap(bitmap, rotation);
            //저장이미지테스트 코드
            //Bitmap bitmap =loadTestImageFromDrawable();

            // 프리뷰 숨기고 결과 영역 보여주기

            ivCaptured.setImageBitmap(rotated);
            // 3) OCR용 InputImage 생성 (회전 0도로 가정)
            InputImage inputImage = InputImage.fromBitmap(bitmap, 90);

            //문자 추출 및 번역
            GetOCRJapaneseWord(inputImage);
        } finally {
            image.close(); // 여기서 반드시 닫기
        }


    }

    private Bitmap loadTestImageFromDrawable() {
        // jp_test 는 drawable 파일 이름 (확장자 빼고)
        return BitmapFactory.decodeResource(
                getResources(),
                R.drawable.japan_horizen
        );
    }

    private TranslatorService pickTranslator() {
        if (NetworkChecker.isOnline(this)) {
            Log.d("OCR", "papago");
            //return container.onlineTranslator;   // 파파고
            return container.offlineTranslator;

        } else {
            Log.d("OCR", "ML Kit");
            return container.offlineTranslator;  // ML Kit
        }
    }

    private void GetOCRJapaneseWord(InputImage inputImage) {
        loadingOverlay.setVisibility(View.VISIBLE);

        ocrJapanEngine.recognize(inputImage, new OcrEngine.Callback() {
            @Override
            public void onSuccess(String text) {

                onOcrSuccess(text);
            }

            @Override
            public void onError(Exception e) {
                onOcrError(e);
            }
        });
    }

    private void onOcrSuccess(String text) {
        // OCR 결과를 먼저 화면에 보여주고

        if(text.isEmpty()){
            tvOcrText.setText("번역할 글자가 없습니다.");
            loadingOverlay.setVisibility(View.GONE);
            return ;
        }
        tvOcrText.setText(text);
        // 그 다음 번역 시작
       translator.translateByLines(text, new TranslatorService.Callback() {
            @Override
            public void onSuccess(String translated) {
                onTranslateSuccess(text, translated);
            }

            @Override
            public void onError(Exception e) {
                onTranslateError(e);
            }
        });
    }

    private void onTranslateSuccess(String original, String translated) {
        // 번역 텍스트 UI 업데이트
        tvOcrText.setText(translated);
        loadingOverlay.setVisibility(View.GONE);
    }

    private void onOcrError(Exception e) {
        loadingOverlay.setVisibility(View.GONE);
        tvOcrText.setText("OCR 실패: " + e.getMessage());
    }

    private void onTranslateError(Exception e) {
        loadingOverlay.setVisibility(View.GONE);
        tvOcrText.setText("번역 실패: " + e.getMessage());
    }


}




