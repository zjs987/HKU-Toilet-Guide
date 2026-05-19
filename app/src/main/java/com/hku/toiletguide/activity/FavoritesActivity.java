package com.hku.toiletguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.util.DistanceUtil;
import com.hku.toiletguide.util.UiFactory;

import java.util.List;
import java.util.Locale;

public class FavoritesActivity extends Activity {
    private final MockToiletRepository repository = MockToiletRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(buildContent());
    }

    private ScrollView buildContent() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout page = UiFactory.page(this);
        page.setBackgroundColor(Color.WHITE);
        scrollView.addView(page);

        page.addView(backBar("My Favorites"));
        List<Toilet> favorites = repository.getCurrentUserFavoriteToilets();
        page.addView(UiFactory.subtitle(this, favorites.size() + " saved toilets"));

        if (favorites.isEmpty()) {
            page.addView(emptyState("You have not saved any toilets yet."));
            return scrollView;
        }

        for (Toilet toilet : favorites) {
            page.addView(favoriteCard(toilet));
        }
        return scrollView;
    }

    private LinearLayout favoriteCard(Toilet toilet) {
        LinearLayout card = cardShell();
        card.setOnClickListener(v -> openDetail(toilet.id));

        TextView title = UiFactory.label(this, toilet.building, 18, UiFactory.TEXT, true);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(title);

        TextView meta = UiFactory.label(this,
                toilet.floor + " · " + toilet.genderLabel() + " · " + DistanceUtil.metersLabel(DistanceUtil.distanceFromHkuCenter(toilet.latitude, toilet.longitude)),
                14,
                UiFactory.MUTED,
                false);
        card.addView(meta, blockParams(6));

        TextView facilities = UiFactory.label(this, toilet.facilitiesLabel(), 14, UiFactory.TEXT, false);
        facilities.setLineSpacing(UiFactory.dp(this, 2), 1f);
        card.addView(facilities, blockParams(8));

        TextView rating = UiFactory.label(this,
                "Overall " + String.format(Locale.US, "%.1f", toilet.avgOverall) + " · " + toilet.totalReviews + " reviews",
                14,
                UiFactory.BLUE,
                true);
        card.addView(rating, blockParams(8));

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.RIGHT);
        actions.addView(actionButton("Open", UiFactory.DARK_GREEN, v -> openDetail(toilet.id)));
        actions.addView(actionButton("Unsave", UiFactory.PINK, v -> {
            repository.toggleFavorite(toilet.id);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            setContentView(buildContent());
        }));
        card.addView(actions, blockParams(12));
        return card;
    }

    private LinearLayout emptyState(String text) {
        LinearLayout state = cardShell();
        TextView label = UiFactory.label(this, text, 15, UiFactory.MUTED, false);
        label.setGravity(Gravity.CENTER);
        state.addView(label);
        return state;
    }

    private LinearLayout cardShell() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14));
        card.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 14, UiFactory.LINE));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        card.setLayoutParams(params);
        return card;
    }

    private LinearLayout.LayoutParams blockParams(int topDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, topDp), 0, 0);
        return params;
    }

    private Button actionButton(String text, int color, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(color);
        button.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 14, UiFactory.LINE));
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                UiFactory.dp(this, 42)
        );
        params.setMargins(UiFactory.dp(this, 8), 0, 0, 0);
        button.setLayoutParams(params);
        return button;
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
        back.setBackground(UiFactory.rounded(this, Color.WHITE, 22));
        back.setOnClickListener(v -> finish());
        bar.addView(back, new LinearLayout.LayoutParams(UiFactory.dp(this, 48), UiFactory.dp(this, 56)));

        TextView label = UiFactory.label(this, title, 24, UiFactory.TEXT, true);
        label.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 56), 1f);
        labelParams.setMargins(UiFactory.dp(this, 12), 0, 0, 0);
        bar.addView(label, labelParams);

        wrapper.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiFactory.dp(this, 56)));
        View line = new View(this);
        line.setBackgroundColor(UiFactory.LINE);
        wrapper.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiFactory.dp(this, 1)));
        return wrapper;
    }

    private void openDetail(String toiletId) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_TOILET_ID, toiletId);
        startActivity(intent);
    }
}
