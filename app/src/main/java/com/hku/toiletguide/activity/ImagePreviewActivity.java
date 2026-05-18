package com.hku.toiletguide.activity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hku.toiletguide.util.UiFactory;

import java.io.IOException;

public class ImagePreviewActivity extends Activity {
    public static final String EXTRA_IMAGE_URI = "image_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(imageView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView close = UiFactory.label(this, "Close", 16, Color.WHITE, true);
        close.setGravity(Gravity.CENTER);
        close.setBackground(UiFactory.rounded(this, Color.argb(180, 30, 30, 35), 18));
        close.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 8), UiFactory.dp(this, 14), UiFactory.dp(this, 8));
        close.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.LEFT
        );
        closeParams.setMargins(UiFactory.dp(this, 16), UiFactory.dp(this, 20), 0, 0);
        root.addView(close, closeParams);

        setContentView(root);

        String imageUri = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        if (imageUri == null || imageUri.trim().isEmpty()) {
            finish();
            return;
        }

        try {
            Uri uri = Uri.parse(imageUri);
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                imageView.setImageBitmap(ImageDecoder.decodeBitmap(source));
            } else {
                imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
            }
        } catch (IOException | IllegalArgumentException error) {
            finish();
        }
    }
}
