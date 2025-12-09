package com.ex.realcv;

import android.app.Application;

import com.ex.realcv.Camera.JapaneseOcr;
import com.ex.realcv.Camera.OcrEngine;
import com.ex.realcv.Camera.OfflineMlTranslator;
import com.ex.realcv.Camera.TranslatorService;

public class MyApplication extends Application {

    public AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer();
    }

    public static class AppContainer {
        public final OcrEngine ocrJapaneEngine = new JapaneseOcr();
        public final TranslatorService offlineTranslator = new OfflineMlTranslator();

    }
}
