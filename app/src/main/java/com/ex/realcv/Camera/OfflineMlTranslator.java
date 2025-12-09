package com.ex.realcv.Camera;

import android.util.Log;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class OfflineMlTranslator  implements TranslatorService {

    private com.google.mlkit.nl.translate.Translator translator;
    private final DownloadConditions conditions;
    private boolean modelReady = false;

    public OfflineMlTranslator() {
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.JAPANESE)
                        .setTargetLanguage(TranslateLanguage.KOREAN)
                        .build();

        translator = Translation.getClient(options);

        conditions = new DownloadConditions.Builder()
                // .requireWifi()   // 에뮬레이터면 이거 때문에 안 될 수도 있음. 필요 없으면 빼자
                .build();

        // 앱 시작 시점에 미리 다운로드 시작
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    modelReady = true;
                    Log.d("OfflineMlTranslator", "번역 모델 다운로드 완료 또는 이미 존재");
                })
                .addOnFailureListener(e -> {
                    Log.e("OfflineMlTranslator", "모델 다운로드 실패", e);
                });
    }
    @Override
    public void translate(String text, Callback callback) {

        translator.translate(text)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void close() {
        translator.close();
    }

    @Override
    public void translateByLines(String ocrText, Callback callback) {
        String[] lines = ocrText.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();

        // 비동기 체인 시작 – 콜백을 여기로 넘겨준다
        translateLineRecursive(lines, 0, sb, callback);
    }

    private void translateLineRecursive(
            String[] lines,
            int index,
            StringBuilder sb,
            Callback finalCallback
    ) {
        // 모든 줄을 다 처리했으면 여기서 최종 콜백 호출
        if (index >= lines.length) {
            finalCallback.onSuccess(sb.toString());
            return;
        }

        String jp = lines[index].trim();

        // 빈 줄이면 그냥 줄 바꿈만 추가하고 다음 줄로
        if (jp.isEmpty()) {
            sb.append("\n");
            translateLineRecursive(lines, index + 1, sb, finalCallback);
            return;
        }

        // 한 줄 번역
        translate(jp, new Callback() {
            @Override
            public void onSuccess(String ko) {
                sb.append(jp).append("\n");
                sb.append(ko).append("\n\n");
                translateLineRecursive(lines, index + 1, sb, finalCallback);
            }

            @Override
            public void onError(Exception e) {
                // 실패해도 다음 줄 진행
                sb.append(jp).append("\n");
                sb.append("[번역 실패]").append("\n\n");
                translateLineRecursive(lines, index + 1, sb, finalCallback);
            }
        });
    }


}
