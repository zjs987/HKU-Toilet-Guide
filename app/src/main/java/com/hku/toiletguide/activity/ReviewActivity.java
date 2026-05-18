package com.hku.toiletguide.activity;

import android.app.Activity;
import android.os.Bundle;

public class ReviewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new android.widget.FrameLayout(this));
        startActivity(new android.content.Intent(this, ContentSubmissionActivity.class).putExtras(getIntent()));
        finish();
    }
}
