package com.hku.toiletguide.activity;

import android.app.Activity;
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
import com.hku.toiletguide.model.User;
import com.hku.toiletguide.util.UiFactory;

public class ProfileActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private LinearLayout buildContent() {
        LinearLayout page = UiFactory.page(this);
        page.setBackgroundColor(Color.WHITE);
        page.addView(backBar("Profile"));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, UiFactory.dp(this, 16), 0, UiFactory.dp(this, 20));
        User currentUser = MockToiletRepository.getInstance().getCurrentUser();

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.drawable.ic_user);
        avatar.setColorFilter(UiFactory.DARK_GREEN);
        avatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        avatar.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 18), UiFactory.dp(this, 18), UiFactory.dp(this, 18));
        avatar.setBackground(UiFactory.rounded(this, Color.rgb(219, 241, 237), 44));
        header.addView(avatar, new LinearLayout.LayoutParams(UiFactory.dp(this, 88), UiFactory.dp(this, 88)));

        TextView name = UiFactory.label(this, currentUser.displayName, 28, UiFactory.TEXT, true);
        name.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 88), 1f);
        nameParams.setMargins(UiFactory.dp(this, 18), 0, 0, 0);
        header.addView(name, nameParams);
        page.addView(header);
        TextView email = UiFactory.label(this, currentUser.email + " | role: " + currentUser.role, 14, UiFactory.MUTED, false);
        email.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(email, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 32)
        ));

        int reviewed = 0;
        for (Toilet toilet : MockToiletRepository.getInstance().getToilets()) {
            reviewed += toilet.reviews.size();
        }

        page.addView(menuRow(R.drawable.ic_favorite, "My favorites", "Saved toilets", UiFactory.PINK));
        page.addView(menuRow(R.drawable.ic_comment, "My reviews", reviewed + " reviews in demo data", Color.rgb(74, 134, 230)));
        page.addView(menuRow(R.drawable.ic_toilet, "My submissions", "Submitted toilets and issue reports", UiFactory.DARK_GREEN));
        page.addView(menuRow(R.drawable.ic_admin, "Admin review center", "Visible only for admin accounts later", Color.rgb(245, 179, 53)));
        page.addView(menuRow(R.drawable.ic_logout, "Log out", "Return to login page later", UiFactory.MUTED));
        return page;
    }

    private LinearLayout menuRow(int iconRes, String title, String subtitle, int color) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiFactory.dp(this, 12), 0, UiFactory.dp(this, 12));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(color);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setPadding(UiFactory.dp(this, 10), UiFactory.dp(this, 10), UiFactory.dp(this, 10), UiFactory.dp(this, 10));
        row.addView(icon, new LinearLayout.LayoutParams(UiFactory.dp(this, 48), UiFactory.dp(this, 48)));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 58), 1f);
        textParams.setMargins(UiFactory.dp(this, 16), 0, 0, 0);
        row.addView(text, textParams);

        TextView titleView = UiFactory.label(this, title, 18, UiFactory.TEXT, false);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        text.addView(titleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView subtitleView = UiFactory.label(this, subtitle, 13, UiFactory.MUTED, false);
        subtitleView.setGravity(Gravity.CENTER_VERTICAL);
        text.addView(subtitleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView arrow = UiFactory.label(this, ">", 24, UiFactory.MUTED, false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(UiFactory.dp(this, 32), UiFactory.dp(this, 58)));
        return row;
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

        wrapper.addView(bar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 56)
        ));
        View line = new View(this);
        line.setBackgroundColor(Color.rgb(232, 235, 238));
        wrapper.addView(line, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 1)
        ));
        return wrapper;
    }
}
