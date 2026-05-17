package com.hku.toiletguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

public class RankingActivity extends Activity {
    private final MockToiletRepository repository = MockToiletRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private ScrollView buildContent() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout page = UiFactory.page(this);
        page.setBackgroundColor(Color.WHITE);
        scrollView.addView(page);

        page.addView(backBar("Ranking"));
        page.addView(UiFactory.subtitle(this, "Top toilets by overall rating."));

        List<Toilet> toilets = new ArrayList<>(repository.getToilets());
        Collections.sort(toilets, (a, b) -> Double.compare(b.avgOverall, a.avgOverall));
        for (int i = 0; i < toilets.size(); i++) {
            page.addView(rankingRow(i + 1, toilets.get(i)));
        }
        return scrollView;
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

        TextView name = UiFactory.label(this, toilet.building, 18, UiFactory.TEXT, false);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        info.addView(name, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView meta = UiFactory.label(this,
                toilet.floor + " | " + toilet.genderLabel() + " | " + DistanceUtil.metersLabel(DistanceUtil.distanceFromHkuCenter(toilet.latitude, toilet.longitude)),
                13,
                UiFactory.MUTED,
                false);
        meta.setGravity(Gravity.CENTER_VERTICAL);
        info.addView(meta, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView rating = UiFactory.label(this, String.format("%.1f", toilet.avgOverall), 18, Color.rgb(245, 179, 53), true);
        rating.setGravity(Gravity.CENTER);
        row.addView(rating, new LinearLayout.LayoutParams(UiFactory.dp(this, 48), UiFactory.dp(this, 68)));
        return row;
    }

    private View rankBadge(int rank) {
        if (rank <= 3) {
            ImageView medal = new ImageView(this);
            medal.setImageResource(rank == 1 ? R.drawable.ic_medal_gold : rank == 2 ? R.drawable.ic_medal_silver : R.drawable.ic_medal_bronze);
            medal.setScaleType(ImageView.ScaleType.FIT_CENTER);
            medal.setPadding(0, 0, 0, 0);
            return withSize(medal, UiFactory.dp(this, 46), UiFactory.dp(this, 46));
        }

        TextView number = UiFactory.label(this, String.valueOf(rank), 18, Color.WHITE, true);
        number.setGravity(Gravity.CENTER);
        number.setBackground(UiFactory.rounded(this, Color.rgb(172, 178, 184), 22));
        return withSize(number, UiFactory.dp(this, 44), UiFactory.dp(this, 44));
    }

    private View withSize(View view, int width, int height) {
        view.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        return view;
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
