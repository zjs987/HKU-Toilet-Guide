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
import com.hku.toiletguide.model.ContentSubmission;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.model.User;
import com.hku.toiletguide.util.UiFactory;

public class ProfileActivity extends Activity {
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
        page.addView(buildProfilePanel());

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

        TextView eyebrow = UiFactory.label(this, "PROFILE", 12, Color.argb(220, 255, 255, 255), false);
        eyebrow.setLetterSpacing(0.24f);
        LinearLayout.LayoutParams eyebrowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        eyebrowParams.setMargins(0, UiFactory.dp(this, 28), 0, 0);
        hero.addView(eyebrow, eyebrowParams);

        TextView title = UiFactory.label(this, "Your saved account and activity", 29, Color.WHITE, true);
        title.setLineSpacing(UiFactory.dp(this, 5), 1f);
        title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, UiFactory.dp(this, 10), 0, 0);
        hero.addView(title, titleParams);

        TextView subtitle = UiFactory.label(this,
                "Check your local account, demo role and quick shortcuts without leaving the same navigation shell.",
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

    private LinearLayout buildProfilePanel() {
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

        User currentUser = repository.getCurrentUser();

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, UiFactory.dp(this, 18), 0, UiFactory.dp(this, 18));

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.drawable.ic_user);
        avatar.setColorFilter(UiFactory.DARK_GREEN);
        avatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        avatar.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 18), UiFactory.dp(this, 18), UiFactory.dp(this, 18));
        avatar.setBackground(UiFactory.rounded(this, Color.argb(185, 255, 255, 255), 44));
        header.addView(avatar, new LinearLayout.LayoutParams(UiFactory.dp(this, 88), UiFactory.dp(this, 88)));

        LinearLayout nameBlock = new LinearLayout(this);
        nameBlock.setOrientation(LinearLayout.VERTICAL);
        nameBlock.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams nameBlockParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        nameBlockParams.setMargins(UiFactory.dp(this, 18), 0, 0, 0);
        header.addView(nameBlock, nameBlockParams);

        TextView name = UiFactory.label(this, currentUser.displayName, 28, Color.WHITE, true);
        nameBlock.addView(name);

        TextView email = UiFactory.label(this, currentUser.email + " | role: " + currentUser.role, 14, Color.argb(220, 255, 255, 255), false);
        LinearLayout.LayoutParams emailParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        emailParams.setMargins(0, UiFactory.dp(this, 6), 0, 0);
        nameBlock.addView(email, emailParams);
        panel.addView(header);

        int reviewed = 0;
        for (Toilet toilet : repository.getToilets()) {
            reviewed += toilet.reviews.size();
        }

        int pendingSubmissions = 0;
        for (ContentSubmission submission : repository.getContentSubmissions()) {
            if (currentUser.id.equals(submission.userId) && submission.isPending()) {
                pendingSubmissions++;
            }
        }

        panel.addView(menuRow(R.drawable.ic_favorite, "My favorites", "Saved toilets", UiFactory.PINK, null));
        panel.addView(menuRow(R.drawable.ic_comment, "Approved reviews", reviewed + " visible reviews in demo data", Color.rgb(74, 134, 230), null));
        panel.addView(menuRow(R.drawable.ic_toilet, "My submissions", pendingSubmissions + " pending content items", UiFactory.DARK_GREEN, null));
        if ("admin".equals(currentUser.role)) {
            panel.addView(menuRow(R.drawable.ic_admin, "Admin console", "Moderate content and resolve live statuses", Color.rgb(245, 179, 53),
                    v -> startActivity(new Intent(this, AdminActivity.class))));
        }
        panel.addView(menuRow(R.drawable.ic_logout, "Log out", "Back to login page", UiFactory.MUTED, v -> {
            repository.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }));
        return panel;
    }

    private LinearLayout menuRow(int iconRes, String title, String subtitle, int color, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiFactory.dp(this, 12), 0, UiFactory.dp(this, 12));
        if (listener != null) {
            row.setOnClickListener(listener);
        }

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(color);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setPadding(UiFactory.dp(this, 10), UiFactory.dp(this, 10), UiFactory.dp(this, 10), UiFactory.dp(this, 10));
        icon.setBackground(UiFactory.rounded(this, Color.argb(120, 255, 255, 255), 24));
        row.addView(icon, new LinearLayout.LayoutParams(UiFactory.dp(this, 48), UiFactory.dp(this, 48)));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 58), 1f);
        textParams.setMargins(UiFactory.dp(this, 16), 0, 0, 0);
        row.addView(text, textParams);

        TextView titleView = UiFactory.label(this, title, 18, Color.WHITE, false);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        text.addView(titleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView subtitleView = UiFactory.label(this, subtitle, 13, Color.argb(220, 255, 255, 255), false);
        subtitleView.setGravity(Gravity.CENTER_VERTICAL);
        text.addView(subtitleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView arrow = UiFactory.label(this, ">", 24, Color.argb(220, 255, 255, 255), false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(UiFactory.dp(this, 32), UiFactory.dp(this, 58)));
        return row;
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
        nav.addView(navItem(R.drawable.ic_medal_gold, "Ranking", false, v -> startActivity(new Intent(this, RankingActivity.class))));
        nav.addView(navItem(R.drawable.ic_user, "Mine", true, v -> {}));
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
