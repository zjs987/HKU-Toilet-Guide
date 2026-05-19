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
import android.widget.FrameLayout;
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

    private View buildContent() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(7, 17, 28));

        ImageView background = new ImageView(this);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        background.setImageResource(R.drawable.corridor);
        root.addView(background, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        View overlay = new View(this);
        overlay.setBackgroundColor(Color.argb(126, 6, 16, 26));
        root.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(UiFactory.dp(this, 20), UiFactory.dp(this, 26), UiFactory.dp(this, 20), UiFactory.dp(this, 28));
        scrollView.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        page.addView(backBar("My Favorites"));
        List<Toilet> favorites = repository.getCurrentUserFavoriteToilets();
        page.addView(heroPanel(favorites.size() + " saved toilets",
                "Quick access to the toilets you marked for repeat visits."));

        if (favorites.isEmpty()) {
            page.addView(emptyState("You have not saved any toilets yet."));
            root.addView(scrollView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            return root;
        }

        for (Toilet toilet : favorites) {
            page.addView(favoriteCard(toilet));
        }

        root.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return root;
    }

    private LinearLayout favoriteCard(Toilet toilet) {
        LinearLayout card = cardShell();
        card.setOnClickListener(v -> openDetail(toilet.id));

        TextView title = UiFactory.label(this, toilet.building, 18, Color.WHITE, true);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(title);

        TextView meta = UiFactory.label(this,
                toilet.floor + " · " + toilet.genderLabel() + " · " + DistanceUtil.metersLabel(DistanceUtil.distanceFromHkuCenter(toilet.latitude, toilet.longitude)),
                14,
                Color.argb(220, 255, 255, 255),
                false);
        card.addView(meta, blockParams(6));

        TextView facilities = UiFactory.label(this, toilet.facilitiesLabel(), 14, Color.WHITE, false);
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
        TextView label = UiFactory.label(this, text, 15, Color.argb(220, 255, 255, 255), false);
        label.setGravity(Gravity.CENTER);
        state.addView(label);
        return state;
    }

    private LinearLayout heroPanel(String title, String subtitle) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(UiFactory.dp(this, 16), UiFactory.dp(this, 16), UiFactory.dp(this, 16), UiFactory.dp(this, 16));
        panel.setBackground(UiFactory.frostedPanel(this, 18));

        TextView titleView = UiFactory.label(this, title, 22, Color.WHITE, true);
        panel.addView(titleView);

        TextView subtitleView = UiFactory.label(this, subtitle, 14, Color.argb(220, 255, 255, 255), false);
        subtitleView.setLineSpacing(UiFactory.dp(this, 3), 1f);
        panel.addView(subtitleView, blockParams(8));
        return panel;
    }

    private LinearLayout cardShell() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14));
        card.setBackground(UiFactory.frostedPanel(this, 16));
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
        button.setBackground(UiFactory.roundedStroke(this, Color.argb(86, 5, 17, 25), 14, Color.argb(95, 255, 255, 255), 1));
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
        bar.setBackground(UiFactory.darkOverlayPanel(this, 24));
        bar.setPadding(UiFactory.dp(this, 12), UiFactory.dp(this, 8), UiFactory.dp(this, 12), UiFactory.dp(this, 8));

        ImageView back = new ImageView(this);
        back.setImageResource(R.drawable.ic_back);
        back.setColorFilter(Color.WHITE);
        back.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        back.setPadding(UiFactory.dp(this, 6), UiFactory.dp(this, 6), UiFactory.dp(this, 6), UiFactory.dp(this, 6));
        back.setOnClickListener(v -> finish());
        bar.addView(back, new LinearLayout.LayoutParams(UiFactory.dp(this, 44), UiFactory.dp(this, 44)));

        TextView label = UiFactory.label(this, title, 24, Color.WHITE, true);
        label.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 56), 1f);
        labelParams.setMargins(UiFactory.dp(this, 12), 0, 0, 0);
        bar.addView(label, labelParams);

        wrapper.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return wrapper;
    }

    private void openDetail(String toiletId) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_TOILET_ID, toiletId);
        startActivity(intent);
    }
}
