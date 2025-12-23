package com.ex.realcv.Camera;

import android.graphics.Bitmap;

import com.google.mlkit.vision.common.InputImage;

public interface OcrEngine {
    interface Callback {
        void onSuccess(String text);
        void onError(Exception e);
    }

    void recognize(InputImage image, Callback callback);

    void recognize(Bitmap bitmap, int rotationDegrees, Callback callback);
    void close();
}
