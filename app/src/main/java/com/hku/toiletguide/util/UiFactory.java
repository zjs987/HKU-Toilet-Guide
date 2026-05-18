package com.hku.toiletguide.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hku.toiletguide.R;

public class UiFactory {
    public static final int GREEN = Color.rgb(15, 166, 145);
    public static final int DARK_GREEN = Color.rgb(0, 126, 111);
    public static final int PINK = Color.rgb(232, 84, 145);
    public static final int BLUE = Color.rgb(55, 113, 218);
    public static final int TEXT = Color.rgb(43, 48, 53);
    public static final int MUTED = Color.rgb(122, 128, 136);
    public static final int LINE = Color.rgb(229, 233, 236);

    private UiFactory() {
    }

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable rounded(Context context, int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static GradientDrawable roundedStroke(Context context, int color, int radiusDp, int strokeColor) {
        GradientDrawable drawable = rounded(context, color, radiusDp);
        drawable.setStroke(dp(context, 1), strokeColor);
        return drawable;
    }

    public static GradientDrawable roundedStroke(Context context, int color, int radiusDp, int strokeColor, int strokeWidthDp) {
        GradientDrawable drawable = rounded(context, color, radiusDp);
        drawable.setStroke(dp(context, strokeWidthDp), strokeColor);
        return drawable;
    }

    public static Drawable frostedPanel(Context context, int radiusDp) {
        GradientDrawable fill = rounded(context, Color.argb(150, 255, 255, 255), radiusDp);
        GradientDrawable stroke = roundedStroke(context, Color.TRANSPARENT, radiusDp, Color.argb(140, 255, 255, 255), 1);
        return new LayerDrawable(new Drawable[]{fill, stroke});
    }

    public static Drawable darkOverlayPanel(Context context, int radiusDp) {
        GradientDrawable fill = rounded(context, Color.argb(130, 14, 24, 34), radiusDp);
        GradientDrawable stroke = roundedStroke(context, Color.TRANSPARENT, radiusDp, Color.argb(90, 255, 255, 255), 1);
        return new LayerDrawable(new Drawable[]{fill, stroke});
    }

    public static TextView title(Context context, String text) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextColor(TEXT);
        view.setTextSize(24);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        return view;
    }

    public static TextView subtitle(Context context, String text) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextColor(MUTED);
        view.setTextSize(14);
        view.setLineSpacing(dp(context, 2), 1f);
        return view;
    }

    public static Button primaryButton(Context context, String text) {
        Button button = new Button(context);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.primary_button);
        return button;
    }

    public static TextView label(Context context, String text, int sizeSp, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return view;
    }

    public static LinearLayout page(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(context, 16), dp(context, 16), dp(context, 16), dp(context, 16));
        layout.setBackgroundColor(Color.rgb(247, 248, 250));
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return layout;
    }
}
