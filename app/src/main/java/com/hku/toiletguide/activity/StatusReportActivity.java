package com.hku.toiletguide.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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

    private ScrollView buildContent() {
        Toilet toilet = repository.getToiletById(toiletId);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout page = UiFactory.page(this);
        page.setBackgroundColor(Color.WHITE);
        scrollView.addView(page);

        page.addView(backBar("Report Live Status"));
        page.addView(UiFactory.subtitle(this, toilet == null ? "Unknown toilet" : toilet.building + " | " + toilet.floor));

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

        page.addView(UiFactory.subtitle(this,
                "Users can only report problem states. Normal states such as tissue sufficient are restored by admin after the issue is resolved."));
        return scrollView;
    }

    private void addTag(LinearLayout page, LiveStatusReport.StatusOption option) {
        CheckBox box = new CheckBox(this);
        box.setText(option.title + " · " + option.longLabel);
        box.setTextColor(UiFactory.TEXT);
        box.setButtonTintList(android.content.res.ColorStateList.valueOf(UiFactory.DARK_GREEN));
        box.setPadding(UiFactory.dp(this, 6), UiFactory.dp(this, 8), UiFactory.dp(this, 6), UiFactory.dp(this, 8));
        box.setTag(option.code);
        box.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 14, UiFactory.LINE));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 10), 0, 0);
        page.addView(box, params);
        checkBoxes.add(box);
    }

    private TextView sectionTitle(String text) {
        TextView title = UiFactory.label(this, text, 18, UiFactory.TEXT, true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        title.setLayoutParams(params);
        return title;
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
