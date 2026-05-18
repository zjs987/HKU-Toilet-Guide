package com.hku.toiletguide;

import android.app.Application;

import com.hku.toiletguide.data.MockToiletRepository;

public class ToiletGuideApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MockToiletRepository.getInstance().init(this);
    }
}
