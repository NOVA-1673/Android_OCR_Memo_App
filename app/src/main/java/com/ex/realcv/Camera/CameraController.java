package com.ex.realcv.Camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.YuvImage;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import android.graphics.Rect;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;


public class CameraController {

    private final Context context;
    private final LifecycleOwner owner;
    private final PreviewView previewView;
    private ImageCapture imageCapture;

    public CameraController(Context context, LifecycleOwner owner, PreviewView view) {
        this.context = context;
        this.owner = owner;
        this.previewView = view;
    }

    public interface OnPhotoCapturedListener {
        void onCaptured(ImageProxy image);
        void onError(Exception e);
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        owner, cameraSelector, preview, imageCapture
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void takePhoto(OnPhotoCapturedListener listener) {
        if (imageCapture == null) return;

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(context),
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        try {
                            listener.onCaptured(image);
                        } catch (Exception e) {
                            listener.onError(e);
                        } finally {
                            image.close();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        listener.onError(exc);
                    }
                }
        );
    }
    public Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try{
            ByteBuffer buffer = planes[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch(Exception e){
            Log.e("Camera", "imageProxyToBitmap() 실패: " + e.getMessage(), e);
            return null;
        }
    }

    public Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}
