package com.hku.toiletguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.util.DistanceUtil;
import com.hku.toiletguide.util.UiFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListActivity extends Activity {
    private final MockToiletRepository repository = MockToiletRepository.getInstance();
    private final List<Toilet> visibleToilets = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private EditText searchInput;
    private Spinner sortSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
        applyFilterAndSort();
    }

    private LinearLayout buildContent() {
        LinearLayout page = UiFactory.page(this);
        page.addView(backBar("Toilet List"));
        page.addView(UiFactory.subtitle(this, "Search, sort, then tap a toilet to open details."));

        searchInput = new EditText(this);
        searchInput.setHint("Search building name");
        searchInput.setSingleLine(true);
        searchInput.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(searchInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        ));
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilterAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        sortSpinner = new Spinner(this);
        sortSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Distance", "Overall rating", "Least crowded"}));
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                applyFilterAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        page.addView(sortSpinner, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        ));

        ListView listView = new ListView(this);
        listView.setDividerHeight(0);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_TOILET_ID, visibleToilets.get(position).id);
            startActivity(intent);
        });
        page.addView(listView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        return page;
    }

    private void applyFilterAndSort() {
        if (adapter == null) {
            return;
        }

        String query = searchInput == null ? "" : searchInput.getText().toString().trim().toLowerCase();
        visibleToilets.clear();
        for (Toilet toilet : repository.getToilets()) {
            if (query.isEmpty() || toilet.building.toLowerCase().contains(query)) {
                visibleToilets.add(toilet);
            }
        }

        int sortMode = sortSpinner == null ? 0 : sortSpinner.getSelectedItemPosition();
        if (sortMode == 1) {
            Collections.sort(visibleToilets, (a, b) -> Double.compare(b.avgOverall, a.avgOverall));
        } else if (sortMode == 2) {
            Collections.sort(visibleToilets, Comparator.comparingInt(a -> a.currentCrowdLevel));
        } else {
            Collections.sort(visibleToilets, (a, b) -> Float.compare(
                    DistanceUtil.distanceFromHkuCenter(a.latitude, a.longitude),
                    DistanceUtil.distanceFromHkuCenter(b.latitude, b.longitude)
            ));
        }

        adapter.clear();
        for (Toilet toilet : visibleToilets) {
            adapter.add(toilet.building + " - " + toilet.floor
                    + "\nRating " + String.format("%.1f", toilet.avgOverall)
                    + " | " + toilet.crowdLabel()
                    + " | " + toilet.facilitiesLabel());
        }
        adapter.notifyDataSetChanged();
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
        line.setBackgroundColor(Color.rgb(232, 235, 238));
        wrapper.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UiFactory.dp(this, 1)));
        return wrapper;
    }
}
