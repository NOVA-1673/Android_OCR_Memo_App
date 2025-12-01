package com.ex.realcv.Camera;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
public final class OCR_Utility {

    private static final String TAG = "OcrTest";

    public interface Callback{
        void onSuccess(String text);
        void onError(Exception e);
    }

    public static void runOcr(Bitmap bitmap, Callback cb){

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        com.google.mlkit.vision.text.TextRecognizer recognizer =
                TextRecognition.getClient(new JapaneseTextRecognizerOptions.Builder().build());

        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        String resultText = visionText.getText();
                        Log.d(TAG, "OCR 결과:\n" + resultText);
                        if (cb != null) cb.onSuccess(resultText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "OCR 실패", e);
                        if (cb != null) cb.onError(e);
                    }
                });

    }

}
