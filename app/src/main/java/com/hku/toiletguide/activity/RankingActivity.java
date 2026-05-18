package com.hku.toiletguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.util.DistanceUtil;
import com.hku.toiletguide.util.UiFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RankingActivity extends Activity {
    private final MockToiletRepository repository = MockToiletRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private LinearLayout buildContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(7, 17, 28));

        FrameLayout contentFrame = new FrameLayout(this);
        contentFrame.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        ImageView background = new ImageView(this);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int backgroundRes = getDrawableId("mainbuilding");
        if (backgroundRes != 0) {
            background.setImageResource(backgroundRes);
        } else {
            background.setBackgroundColor(Color.rgb(28, 51, 67));
        }
        contentFrame.addView(background, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        View overlay = new View(this);
        overlay.setBackgroundColor(Color.argb(118, 6, 16, 26));
        contentFrame.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(UiFactory.dp(this, 20), UiFactory.dp(this, 34), UiFactory.dp(this, 20), UiFactory.dp(this, 26));
        scrollView.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        page.addView(buildHero());
        page.addView(buildRankingPanel());

        contentFrame.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.addView(contentFrame);
        root.addView(buildBottomNav());
        return root;
    }

    private LinearLayout buildHero() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);

        LinearLayout brandRow = new LinearLayout(this);
        brandRow.setGravity(Gravity.CENTER_VERTICAL);
        brandRow.setBackground(UiFactory.darkOverlayPanel(this, 24));
        brandRow.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 10), UiFactory.dp(this, 14), UiFactory.dp(this, 10));

        ImageView brand = new ImageView(this);
        brand.setImageResource(R.drawable.ic_brand_mark);
        brand.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        brandRow.addView(brand, new LinearLayout.LayoutParams(UiFactory.dp(this, 52), UiFactory.dp(this, 52)));

        TextView appName = UiFactory.label(this, "HKU Toilet Guide", 24, Color.WHITE, true);
        appName.setGravity(Gravity.CENTER_VERTICAL);
        appName.setSingleLine(true);
        appName.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams appNameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        appNameParams.setMargins(UiFactory.dp(this, 12), 0, 0, 0);
        brandRow.addView(appName, appNameParams);
        hero.addView(brandRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView eyebrow = UiFactory.label(this, "RANKING BOARD", 12, Color.argb(220, 255, 255, 255), false);
        eyebrow.setLetterSpacing(0.24f);
        LinearLayout.LayoutParams eyebrowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        eyebrowParams.setMargins(0, UiFactory.dp(this, 28), 0, 0);
        hero.addView(eyebrow, eyebrowParams);

        TextView title = UiFactory.label(this, "Top rated washrooms across campus", 29, Color.WHITE, true);
        title.setLineSpacing(UiFactory.dp(this, 5), 1f);
        title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, UiFactory.dp(this, 10), 0, 0);
        hero.addView(title, titleParams);

        TextView subtitle = UiFactory.label(this,
                "Sorted by overall rating so students can quickly spot the best reviewed options before walking over.",
                15,
                Color.argb(220, 255, 255, 255),
                false);
        subtitle.setLineSpacing(UiFactory.dp(this, 4), 1f);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, UiFactory.dp(this, 10), 0, UiFactory.dp(this, 20));
        hero.addView(subtitle, subtitleParams);
        return hero;
    }

    private LinearLayout buildRankingPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 14), UiFactory.dp(this, 18), UiFactory.dp(this, 18));
        panel.setBackground(UiFactory.frostedPanel(this, 28));

        LinearLayout handle = new LinearLayout(this);
        handle.setBackground(UiFactory.rounded(this, Color.argb(170, 255, 255, 255), 4));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(
                UiFactory.dp(this, 54),
                UiFactory.dp(this, 6)
        );
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        panel.addView(handle, handleParams);

        TextView sectionTitle = UiFactory.label(this, "Overall ranking", 21, Color.WHITE, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, UiFactory.dp(this, 18), 0, UiFactory.dp(this, 6));
        panel.addView(sectionTitle, titleParams);

        TextView sectionHint = UiFactory.label(this, "Top toilets by overall rating.", 14, Color.argb(220, 255, 255, 255), false);
        panel.addView(sectionHint);

        List<Toilet> toilets = new ArrayList<>(repository.getToilets());
        Collections.sort(toilets, (a, b) -> Double.compare(b.avgOverall, a.avgOverall));
        for (int i = 0; i < toilets.size(); i++) {
            panel.addView(rankingRow(i + 1, toilets.get(i)));
        }
        return panel;
    }

    private LinearLayout rankingRow(int rank, Toilet toilet) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiFactory.dp(this, 14), 0, UiFactory.dp(this, 14));
        row.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_TOILET_ID, toilet.id);
            startActivity(intent);
        });

        row.addView(rankBadge(rank));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 68), 1f);
        infoParams.setMargins(UiFactory.dp(this, 14), 0, UiFactory.dp(this, 10), 0);
        row.addView(info, infoParams);

        TextView name = UiFactory.label(this, toilet.building, 18, Color.WHITE, false);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        info.addView(name, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView meta = UiFactory.label(this,
                toilet.floor + " | " + toilet.genderLabel() + " | " + DistanceUtil.metersLabel(DistanceUtil.distanceFromHkuCenter(toilet.latitude, toilet.longitude)),
                13,
                Color.argb(220, 255, 255, 255),
                false);
        meta.setGravity(Gravity.CENTER_VERTICAL);
        info.addView(meta, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView rating = UiFactory.label(this, String.format(Locale.US, "%.1f", toilet.avgOverall), 18, Color.rgb(245, 179, 53), true);
        rating.setGravity(Gravity.CENTER);
        row.addView(rating, new LinearLayout.LayoutParams(UiFactory.dp(this, 48), UiFactory.dp(this, 68)));
        return row;
    }

    private View rankBadge(int rank) {
        if (rank <= 3) {
            ImageView medal = new ImageView(this);
            medal.setImageResource(rank == 1 ? R.drawable.ic_medal_gold : rank == 2 ? R.drawable.ic_medal_silver : R.drawable.ic_medal_bronze);
            medal.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return withSize(medal, UiFactory.dp(this, 46), UiFactory.dp(this, 46));
        }

        TextView number = UiFactory.label(this, String.valueOf(rank), 18, Color.WHITE, true);
        number.setGravity(Gravity.CENTER);
        number.setBackground(UiFactory.rounded(this, Color.argb(120, 255, 255, 255), 22));
        return withSize(number, UiFactory.dp(this, 44), UiFactory.dp(this, 44));
    }

    private View withSize(View view, int width, int height) {
        view.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        return view;
    }

    private LinearLayout buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(UiFactory.dp(this, 8), UiFactory.dp(this, 6), UiFactory.dp(this, 8), UiFactory.dp(this, 6));
        nav.setBackgroundColor(Color.rgb(7, 17, 28));
        nav.setElevation(UiFactory.dp(this, 8));
        nav.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 72)
        ));

        nav.addView(navItem(R.drawable.ic_home, "Home", false, v -> startActivity(new Intent(this, MainActivity.class))));
        nav.addView(navItem(R.drawable.ic_map_marker, "Map", false, v -> startActivity(new Intent(this, MapActivity.class))));
        nav.addView(navItem(R.drawable.ic_medal_gold, "Ranking", true, v -> {}));
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

    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
}
