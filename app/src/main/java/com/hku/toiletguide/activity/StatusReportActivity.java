package com.hku.toiletguide.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.model.LiveStatusReport;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.util.UiFactory;

import java.util.ArrayList;
import java.util.List;

public class StatusReportActivity extends Activity {
    private final MockToiletRepository repository = MockToiletRepository.getInstance();
    private final List<CheckBox> checkBoxes = new ArrayList<>();
    private String toiletId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toiletId = getIntent().getStringExtra(DetailActivity.EXTRA_TOILET_ID);
        setContentView(buildContent());
    }

    private View buildContent() {
        Toilet toilet = repository.getToiletById(toiletId);
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

        page.addView(backBar("Report Live Status"));
        page.addView(heroPanel(toilet == null ? "Unknown toilet" : toilet.building + " | " + toilet.floor));

        page.addView(sectionTitle("Report one or more problem tags"));
        for (LiveStatusReport.StatusOption option : LiveStatusReport.userReportableOptions()) {
            addTag(page, option);
        }

        Button submit = UiFactory.primaryButton(this, "Submit status");
        submit.setOnClickListener(v -> submit());
        LinearLayout.LayoutParams submitParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        );
        submitParams.setMargins(0, UiFactory.dp(this, 20), 0, UiFactory.dp(this, 12));
        page.addView(submit, submitParams);

        page.addView(UiFactory.label(this,
                "Users can only report problem states. Normal states are restored by admin after the issue is resolved.",
                13,
                Color.argb(220, 255, 255, 255),
                false));

        root.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return root;
    }

    private void addTag(LinearLayout page, LiveStatusReport.StatusOption option) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 10), UiFactory.dp(this, 14), UiFactory.dp(this, 10));
        card.setBackground(UiFactory.frostedPanel(this, 18));

        CheckBox box = new CheckBox(this);
        box.setText(option.title + " · " + option.longLabel);
        box.setTextColor(Color.WHITE);
        box.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.rgb(230, 176, 58)));
        box.setPadding(UiFactory.dp(this, 6), UiFactory.dp(this, 8), UiFactory.dp(this, 6), UiFactory.dp(this, 8));
        box.setTag(option.code);
        box.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        card.addView(box, boxParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 10), 0, 0);
        page.addView(card, params);
        checkBoxes.add(box);
    }

    private TextView sectionTitle(String text) {
        TextView title = UiFactory.label(this, text, 18, Color.WHITE, true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        title.setLayoutParams(params);
        return title;
    }

    private LinearLayout heroPanel(String text) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(UiFactory.dp(this, 16), UiFactory.dp(this, 16), UiFactory.dp(this, 16), UiFactory.dp(this, 16));
        panel.setBackground(UiFactory.frostedPanel(this, 18));

        TextView subtitle = UiFactory.label(this, text, 14, Color.argb(220, 255, 255, 255), false);
        subtitle.setLineSpacing(UiFactory.dp(this, 3), 1f);
        panel.addView(subtitle);
        return panel;
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

    private void submit() {
        List<String> selectedCodes = new ArrayList<>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                selectedCodes.add((String) checkBox.getTag());
            }
        }
        if (selectedCodes.isEmpty()) {
            Toast.makeText(this, "Select at least one status tag", Toast.LENGTH_SHORT).show();
            return;
        }
        repository.submitLiveStatuses(toiletId, selectedCodes);
        Toast.makeText(this, "Status submitted", Toast.LENGTH_SHORT).show();
        finish();
    }
}
