package com.hku.toiletguide.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
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
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private final MockToiletRepository repository = MockToiletRepository.getInstance();
    private boolean showMale;
    private boolean showFemale;
    private boolean showAccessible;
    private boolean requireTissue;
    private boolean requireDryer;
    private int sortMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private LinearLayout buildContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(UiFactory.GREEN);
        scrollView.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        page.addView(buildHero());
        page.addView(buildListPanel());
        root.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        root.addView(buildBottomNav());
        return root;
    }

    private LinearLayout buildHero() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(UiFactory.dp(this, 20), UiFactory.dp(this, 34), UiFactory.dp(this, 20), UiFactory.dp(this, 28));

        LinearLayout brandRow = new LinearLayout(this);
        brandRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView brand = new ImageView(this);
        brand.setImageResource(R.drawable.ic_brand_mark);
        brand.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        brandRow.addView(brand, new LinearLayout.LayoutParams(UiFactory.dp(this, 52), UiFactory.dp(this, 52)));

        TextView appName = UiFactory.label(this, "HKU Toilet Guide", 29, Color.WHITE, true);
        appName.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams appNameParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 52), 1f);
        appNameParams.setMargins(UiFactory.dp(this, 12), 0, 0, 0);
        brandRow.addView(appName, appNameParams);
        hero.addView(brandRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 56)
        ));

        TextView subtitle = UiFactory.label(this, "Find clean, quiet campus toilets faster", 14, Color.argb(220, 255, 255, 255), false);
        subtitle.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 30)
        );
        subtitleParams.setMargins(0, 0, 0, UiFactory.dp(this, 18));
        hero.addView(subtitle, subtitleParams);

        LinearLayout search = new LinearLayout(this);
        search.setOrientation(LinearLayout.HORIZONTAL);
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setPadding(UiFactory.dp(this, 18), 0, UiFactory.dp(this, 8), 0);
        search.setBackground(UiFactory.rounded(this, Color.WHITE, 30));
        search.setElevation(UiFactory.dp(this, 4));

        ImageView searchIcon = new ImageView(this);
        searchIcon.setImageResource(R.drawable.ic_filter);
        searchIcon.setColorFilter(UiFactory.MUTED);
        search.addView(searchIcon, new LinearLayout.LayoutParams(UiFactory.dp(this, 30), UiFactory.dp(this, 30)));

        TextView searchText = UiFactory.label(this, "Search building", 17, UiFactory.MUTED, false);
        searchText.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams searchTextParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 58), 1f);
        searchTextParams.setMargins(UiFactory.dp(this, 12), 0, 0, 0);
        search.addView(searchText, searchTextParams);

        Button advanced = new Button(this);
        advanced.setText("Filter");
        advanced.setTextColor(UiFactory.DARK_GREEN);
        advanced.setAllCaps(false);
        advanced.setTypeface(Typeface.DEFAULT_BOLD);
        advanced.setGravity(Gravity.CENTER);
        advanced.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 24, Color.rgb(224, 234, 232)));
        advanced.setOnClickListener(v -> showSortDialog());
        search.addView(advanced, new LinearLayout.LayoutParams(
                UiFactory.dp(this, 108),
                UiFactory.dp(this, 46)
        ));
        hero.addView(search);
        return hero;
    }

    private LinearLayout buildListPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 14), UiFactory.dp(this, 18), UiFactory.dp(this, 18));
        panel.setBackground(UiFactory.rounded(this, Color.WHITE, 28));

        LinearLayout handle = new LinearLayout(this);
        handle.setBackground(UiFactory.rounded(this, Color.rgb(176, 176, 184), 4));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(
                UiFactory.dp(this, 54),
                UiFactory.dp(this, 6)
        );
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        panel.addView(handle, handleParams);

        panel.addView(buildFilterBar());
        panel.addView(buildTitleRow());

        List<Toilet> toilets = filteredToilets();
        if (toilets.isEmpty()) {
            TextView empty = UiFactory.label(this, "No toilets match the current filters.", 16, UiFactory.MUTED, false);
            empty.setGravity(Gravity.CENTER);
            panel.addView(empty, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    UiFactory.dp(this, 120)
            ));
        } else {
            int count = Math.min(6, toilets.size());
            for (int i = 0; i < count; i++) {
                panel.addView(toiletRow(toilets.get(i)));
            }
        }

        return panel;
    }

    private HorizontalScrollView buildFilterBar() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        scrollParams.setMargins(0, UiFactory.dp(this, 18), 0, UiFactory.dp(this, 14));
        scroll.setLayoutParams(scrollParams);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, UiFactory.dp(this, 8), 0);
        scroll.addView(row);

        row.addView(filterIcon(R.drawable.ic_male, showMale, UiFactory.BLUE, () -> showMale = !showMale));
        row.addView(filterIcon(R.drawable.ic_female, showFemale, UiFactory.PINK, () -> showFemale = !showFemale));
        row.addView(filterIcon(R.drawable.ic_accessible, showAccessible, Color.rgb(79, 177, 104), () -> showAccessible = !showAccessible));
        row.addView(filterIcon(R.drawable.ic_tissue, requireTissue, Color.rgb(245, 179, 53), () -> requireTissue = !requireTissue));
        row.addView(filterIcon(R.drawable.ic_dryer, requireDryer, Color.rgb(58, 174, 190), () -> requireDryer = !requireDryer));
        row.addView(actionIcon(R.drawable.ic_filter, Color.WHITE, UiFactory.TEXT, this::showSortDialog));
        return scroll;
    }

    private View filterIcon(int iconRes, boolean selected, int selectedColor, Runnable toggle) {
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(selected ? Color.WHITE : Color.rgb(74, 78, 84));
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int inset = filterIconInset(iconRes);
        icon.setPadding(inset, inset, inset, inset);
        icon.setBackground(selected
                ? UiFactory.rounded(this, selectedColor, 24)
                : UiFactory.rounded(this, Color.WHITE, 24));
        icon.setElevation(selected ? UiFactory.dp(this, 2) : 0);
        icon.setOnClickListener(v -> {
            toggle.run();
            setContentView(buildContent());
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UiFactory.dp(this, 48), UiFactory.dp(this, 48));
        params.setMargins(0, 0, UiFactory.dp(this, 9), 0);
        icon.setLayoutParams(params);
        return icon;
    }

    private int filterIconInset(int iconRes) {
        if (iconRes == R.drawable.ic_male || iconRes == R.drawable.ic_female) {
            return UiFactory.dp(this, 3);
        }
        if (iconRes == R.drawable.ic_accessible) {
            return UiFactory.dp(this, 7);
        }
        return UiFactory.dp(this, 8);
    }

    private View actionIcon(int iconRes, int backgroundColor, int iconColor, Runnable action) {
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(iconColor);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setPadding(UiFactory.dp(this, 11), UiFactory.dp(this, 11), UiFactory.dp(this, 11), UiFactory.dp(this, 11));
        icon.setBackground(UiFactory.roundedStroke(this, backgroundColor, 24, Color.rgb(226, 230, 233)));
        icon.setElevation(UiFactory.dp(this, 2));
        icon.setOnClickListener(v -> action.run());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UiFactory.dp(this, 48), UiFactory.dp(this, 48));
        params.setMargins(0, 0, UiFactory.dp(this, 9), 0);
        icon.setLayoutParams(params);
        return icon;
    }

    private LinearLayout buildTitleRow() {
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.addView(UiFactory.label(this, "Nearby Toilets", 20, UiFactory.TEXT, true), new LinearLayout.LayoutParams(0, UiFactory.dp(this, 46), 1f));

        TextView sort = UiFactory.label(this, sortMode == 0 ? "Distance" : "Rating", 14, UiFactory.DARK_GREEN, true);
        sort.setGravity(Gravity.CENTER);
        sort.setBackground(UiFactory.rounded(this, Color.rgb(232, 248, 245), 20));
        sort.setOnClickListener(v -> showSortDialog());
        titleRow.addView(sort, new LinearLayout.LayoutParams(UiFactory.dp(this, 104), UiFactory.dp(this, 40)));
        return titleRow;
    }

    private LinearLayout buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(UiFactory.dp(this, 8), UiFactory.dp(this, 6), UiFactory.dp(this, 8), UiFactory.dp(this, 6));
        nav.setBackgroundColor(Color.WHITE);
        nav.setElevation(UiFactory.dp(this, 8));
        nav.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 72)
        ));

        nav.addView(navItem(R.drawable.ic_home, "Home", true, v -> setContentView(buildContent())));
        nav.addView(navItem(R.drawable.ic_map_marker, "Map", false, v -> startActivity(new Intent(this, MapActivity.class))));
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

    private View toiletRow(Toilet toilet) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiFactory.dp(this, 14), 0, UiFactory.dp(this, 14));
        row.setOnClickListener(v -> openDetail(toilet.id));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconForToilet(toilet));
        icon.setColorFilter(Color.WHITE);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setPadding(UiFactory.dp(this, 13), UiFactory.dp(this, 13), UiFactory.dp(this, 13), UiFactory.dp(this, 13));
        icon.setBackground(UiFactory.rounded(this, colorForToilet(toilet), 28));
        row.addView(icon, new LinearLayout.LayoutParams(UiFactory.dp(this, 56), UiFactory.dp(this, 56)));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 70), 1f);
        infoParams.setMargins(UiFactory.dp(this, 14), 0, UiFactory.dp(this, 8), 0);
        row.addView(info, infoParams);

        TextView name = UiFactory.label(this, toilet.building, 18, UiFactory.TEXT, false);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        info.addView(name, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView meta = UiFactory.label(this,
                toilet.floor + " | " + toilet.genderLabel(),
                13,
                UiFactory.MUTED,
                false);
        meta.setGravity(Gravity.CENTER_VERTICAL);
        info.addView(meta, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        info.addView(crowdPeople(toilet.currentCrowdLevel, UiFactory.dp(this, 18)), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        LinearLayout side = new LinearLayout(this);
        side.setOrientation(LinearLayout.VERTICAL);
        side.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        row.addView(side, new LinearLayout.LayoutParams(UiFactory.dp(this, 88), UiFactory.dp(this, 70)));

        TextView stars = UiFactory.label(this, starsFor(toilet.avgOverall), 16, Color.rgb(245, 179, 53), true);
        stars.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        side.addView(stars, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView distance = UiFactory.label(this,
                DistanceUtil.metersLabel(DistanceUtil.distanceFromHkuCenter(toilet.latitude, toilet.longitude)),
                15,
                UiFactory.DARK_GREEN,
                true);
        distance.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        side.addView(distance, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        return row;
    }

    private LinearLayout crowdPeople(int level, int iconSize) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int safeLevel = Math.max(0, Math.min(5, level));
        for (int i = 1; i <= 5; i++) {
            ImageView person = new ImageView(this);
            person.setImageResource(R.drawable.ic_male);
            person.setColorFilter(i <= safeLevel ? crowdColor(safeLevel) : Color.rgb(210, 215, 220));
            person.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
            params.setMargins(0, 0, UiFactory.dp(this, 3), 0);
            row.addView(person, params);
        }
        return row;
    }

    private int crowdColor(int level) {
        if (level <= 2) {
            return Color.rgb(43, 170, 94);
        }
        if (level == 3) {
            return Color.rgb(245, 179, 53);
        }
        return Color.rgb(220, 72, 72);
    }

    private List<Toilet> filteredToilets() {
        List<Toilet> result = new ArrayList<>();
        for (Toilet toilet : repository.getToilets()) {
            boolean hasGenderFilter = showMale || showFemale;
            boolean genderMatches = (showMale && "male".equals(toilet.gender))
                    || (showFemale && "female".equals(toilet.gender));
            if (hasGenderFilter && !genderMatches) {
                continue;
            }
            if (showAccessible && !toilet.accessible) {
                continue;
            }
            if (requireTissue && !toilet.hasTissue) {
                continue;
            }
            if (requireDryer && !toilet.hasDryer) {
                continue;
            }
            result.add(toilet);
        }

        if (sortMode == 1) {
            Collections.sort(result, (a, b) -> Double.compare(b.avgOverall, a.avgOverall));
        } else {
            Collections.sort(result, Comparator.comparingDouble(toilet -> DistanceUtil.distanceFromHkuCenter(toilet.latitude, toilet.longitude)));
        }
        return result;
    }

    private void showSortDialog() {
        String[] options = {"Distance", "Overall rating"};
        new AlertDialog.Builder(this)
                .setTitle("Sort by")
                .setSingleChoiceItems(options, sortMode, (dialog, which) -> {
                    sortMode = which;
                    dialog.dismiss();
                    setContentView(buildContent());
                })
                .show();
    }

    private int iconForToilet(Toilet toilet) {
        if ("male".equals(toilet.gender)) {
            return R.drawable.ic_male;
        }
        if ("female".equals(toilet.gender)) {
            return R.drawable.ic_female;
        }
        return R.drawable.ic_toilet;
    }

    private int colorForToilet(Toilet toilet) {
        if ("male".equals(toilet.gender)) {
            return UiFactory.BLUE;
        }
        if ("female".equals(toilet.gender)) {
            return UiFactory.PINK;
        }
        return UiFactory.DARK_GREEN;
    }

    private String starsFor(double rating) {
        int filled = Math.max(1, Math.min(5, (int) Math.round(rating)));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(i < filled ? "*" : "-");
        }
        return builder.toString();
    }

    private void openDetail(String toiletId) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_TOILET_ID, toiletId);
        startActivity(intent);
    }
}
