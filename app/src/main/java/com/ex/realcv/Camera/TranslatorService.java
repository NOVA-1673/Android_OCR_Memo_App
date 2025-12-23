package com.ex.realcv.Camera;

public interface TranslatorService {

    interface Callback {
        void onSuccess(String translated);
        void onError(Exception e);
    }

    void translate(String text, Callback callback);

    void translateByLines(String ocrText, Callback callback);
    void close();   // ML Kit 같은 경우 model 해제용

}
