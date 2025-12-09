package com.ex.realcv.Camera;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions;

/**
 * ML Kit 일본어 OCR 래퍼 클래스
 *  - InputImage / Bitmap 을 받아서 일본어 텍스트를 추출해줌
 *  - 결과는 Callback 으로 전달
 */
public class JapaneseOcr implements OcrEngine {


    private final TextRecognizer recognizer;

    public JapaneseOcr() {
        Log.d("OCR", "init");
        // 일본어 전용 옵션으로 TextRecognizer 생성
        JapaneseTextRecognizerOptions options =
                new JapaneseTextRecognizerOptions.Builder().build();
        recognizer = TextRecognition.getClient(options);
    }

    /**
     * 이미 만들어진 InputImage 로 OCR 수행
     */

    @Override
    public void recognize(InputImage image, Callback callback) {
        recognizer
                .process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text result) {

                        // TextResult 안에서 모든 블록/라인을 하나의 문자열로 합치기
                        StringBuilder sb = new StringBuilder();
                        for (Text.TextBlock block : result.getTextBlocks()) {
                            sb.append(block.getText()).append("\n");
                        }
                        String text = sb.toString().trim();
                        callback.onSuccess(text);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Bitmap + 회전각도로 바로 OCR (편의 함수)
     */
    public void recognize(Bitmap bitmap, int rotationDegrees, Callback callback) {
        InputImage image = InputImage.fromBitmap(bitmap, rotationDegrees);
        recognize(image, callback);
    }

    /**
     * 액티비티/화면 종료 시 호출해주면 좋음
     */
    public void close() {
        recognizer.close();
    }
}

