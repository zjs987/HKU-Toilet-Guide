package com.hku.toiletguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.util.UiFactory;

public class MapActivity extends Activity {
    private final MockToiletRepository repository = MockToiletRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private LinearLayout buildContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout page = UiFactory.page(this);
        page.setBackgroundColor(Color.WHITE);
        page.addView(backBar("Campus Map"));
        page.addView(mapPlaceholder(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        root.addView(page, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        root.addView(bottomNav());
        return root;
    }

    private LinearLayout mapPlaceholder() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 18), UiFactory.dp(this, 18), UiFactory.dp(this, 18));
        panel.setBackground(UiFactory.roundedStroke(this, Color.rgb(246, 248, 249), 18, Color.rgb(226, 230, 233)));

        ImageView marker = new ImageView(this);
        marker.setImageResource(R.drawable.ic_map_marker);
        marker.setColorFilter(UiFactory.DARK_GREEN);
        marker.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        panel.addView(marker, new LinearLayout.LayoutParams(UiFactory.dp(this, 78), UiFactory.dp(this, 78)));

        TextView title = UiFactory.label(this, "Google Map placeholder", 22, UiFactory.TEXT, true);
        title.setGravity(Gravity.CENTER);
        panel.addView(title);

        TextView hint = UiFactory.label(this,
                "Add your Google Maps API key later, then replace this blank panel with a SupportMapFragment and draw markers from repository.getToilets().",
                14,
                UiFactory.MUTED,
                false);
        hint.setGravity(Gravity.CENTER);
        hint.setLineSpacing(UiFactory.dp(this, 4), 1f);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        hintParams.setMargins(0, UiFactory.dp(this, 10), 0, UiFactory.dp(this, 18));
        panel.addView(hint, hintParams);

        for (Toilet toilet : repository.getToilets()) {
            TextView markerRow = UiFactory.label(this,
                    "• " + toilet.building + " " + toilet.floor + " (" + toilet.genderLabel() + ")",
                    14,
                    UiFactory.TEXT,
                    false);
            markerRow.setGravity(Gravity.CENTER_VERTICAL);
            panel.addView(markerRow, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    UiFactory.dp(this, 28)
            ));
        }
        return panel;
    }

    private LinearLayout bottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(UiFactory.dp(this, 8), UiFactory.dp(this, 6), UiFactory.dp(this, 8), UiFactory.dp(this, 6));
        nav.setBackgroundColor(Color.WHITE);
        nav.setElevation(UiFactory.dp(this, 8));

        nav.addView(navItem(R.drawable.ic_home, "Home", false, v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }));
        nav.addView(navItem(R.drawable.ic_map_marker, "Map", true, v -> {}));
        nav.addView(navItem(R.drawable.ic_medal_gold, "Ranking", false, v -> startActivity(new Intent(this, RankingActivity.class))));
        nav.addView(navItem(R.drawable.ic_user, "Mine", false, v -> startActivity(new Intent(this, ProfileActivity.class))));
        return nav;
    }

    private LinearLayout navItem(int iconRes, String label, boolean active, View.OnClickListener listener) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setOnClickListener(listener);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(active ? UiFactory.DARK_GREEN : UiFactory.MUTED);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setPadding(UiFactory.dp(this, 4), UiFactory.dp(this, 4), UiFactory.dp(this, 4), UiFactory.dp(this, 4));
        item.addView(icon, new LinearLayout.LayoutParams(UiFactory.dp(this, 30), UiFactory.dp(this, 30)));

        TextView text = UiFactory.label(this, label, 12, active ? UiFactory.DARK_GREEN : UiFactory.MUTED, true);
        text.setGravity(Gravity.CENTER);
        item.addView(text, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiFactory.dp(this, 22)));
        item.setLayoutParams(new LinearLayout.LayoutParams(0, UiFactory.dp(this, 58), 1f));
        return item;
    }

    private LinearLayout backBar(String title) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(0, 0, 0, UiFactory.dp(this, 12));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        ImageView back = new ImageView(this);
        back.setImageResource(R.drawable.ic_back);
        back.setColorFilter(UiFactory.TEXT);
        back.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        back.setPadding(UiFactory.dp(this, 6), UiFactory.dp(this, 6), UiFactory.dp(this, 6), UiFactory.dp(this, 6));
        back.setOnClickListener(v -> finish());
        bar.addView(back, new LinearLayout.LayoutParams(UiFactory.dp(this, 48), UiFactory.dp(this, 56)));

        TextView label = UiFactory.label(this, title, 24, UiFactory.TEXT, true);
        label.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 56), 1f);
        labelParams.setMargins(UiFactory.dp(this, 12), 0, 0, 0);
        bar.addView(label, labelParams);

        wrapper.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiFactory.dp(this, 56)));
        View line = new View(this);
        line.setBackgroundColor(Color.rgb(232, 235, 238));
        wrapper.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiFactory.dp(this, 1)));
        return wrapper;
    }
}
