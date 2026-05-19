package com.hku.toiletguide.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.model.ContentSubmission;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.model.User;
import com.hku.toiletguide.util.DistanceUtil;
import com.hku.toiletguide.util.UiFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    public static final String EXTRA_TAB = "extra_tab";
    public static final String EXTRA_FOCUS_TOILET_ID = "extra_focus_toilet_id";
    public static final int TAB_MAP_INDEX = 1;

    private static final int TAB_HOME = 0;
    private static final int TAB_MAP = TAB_MAP_INDEX;
    private static final int TAB_RANKING = 2;
    private static final int TAB_MINE = 3;

    private final MockToiletRepository repository = MockToiletRepository.getInstance();
    private boolean showMale;
    private boolean showFemale;
    private boolean showAccessible;
    private boolean requireTissue;
    private boolean requireDryer;
    private int sortMode;
    private String searchQuery = "";
    private int currentTab = TAB_HOME;
    private MapView mapView;
    private Bundle mapViewState;
    private String focusedMapToiletId;
    private LinearLayout homePage;
    private View homeListPanelView;
    private boolean activityResumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapViewState = savedInstanceState;
        currentTab = normalizeTab(getIntent().getIntExtra(EXTRA_TAB, TAB_HOME));
        focusedMapToiletId = getIntent().getStringExtra(EXTRA_FOCUS_TOILET_ID);
        setContentView(buildContent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        destroyMapView();
        currentTab = normalizeTab(intent.getIntExtra(EXTRA_TAB, TAB_HOME));
        focusedMapToiletId = intent.getStringExtra(EXTRA_FOCUS_TOILET_ID);
        setContentView(buildContent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityResumed = true;
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        activityResumed = false;
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        destroyMapView();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
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

        View tabContent = buildTabContent();
        contentFrame.addView(tabContent, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        root.addView(contentFrame);
        root.addView(buildBottomNav());
        return root;
    }

    private View buildTabContent() {
        switch (currentTab) {
            case TAB_MAP:
                return buildMapTab();
            case TAB_RANKING:
                return buildRankingTab();
            case TAB_MINE:
                return buildMineTab();
            case TAB_HOME:
            default:
                return buildHomeTab();
        }
    }

    private View buildHomeTab() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        homePage = page;
        scrollView.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        page.addView(buildHomeHero());
        homeListPanelView = buildHomeListPanel();
        page.addView(homeListPanelView);
        return scrollView;
    }

    private LinearLayout buildHomeHero() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(UiFactory.dp(this, 20), UiFactory.dp(this, 34), UiFactory.dp(this, 20), UiFactory.dp(this, 26));

        hero.addView(buildBrandRow());

        LinearLayout search = new LinearLayout(this);
        search.setOrientation(LinearLayout.HORIZONTAL);
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setPadding(UiFactory.dp(this, 18), 0, UiFactory.dp(this, 8), 0);
        search.setBackground(UiFactory.roundedStroke(this,
                Color.argb(118, 4, 14, 22),
                30,
                Color.argb(105, 255, 255, 255),
                1));
        search.setElevation(UiFactory.dp(this, 4));

        ImageView searchIcon = new ImageView(this);
        searchIcon.setImageResource(R.drawable.ic_filter);
        searchIcon.setColorFilter(Color.WHITE);
        search.addView(searchIcon, new LinearLayout.LayoutParams(UiFactory.dp(this, 30), UiFactory.dp(this, 30)));

        EditText searchText = new EditText(this);
        searchText.setText(searchQuery);
        searchText.setHint("Search building, floor, facility");
        searchText.setHintTextColor(Color.argb(170, 255, 255, 255));
        searchText.setTextColor(Color.WHITE);
        searchText.setTextSize(16);
        searchText.setSingleLine(true);
        searchText.setGravity(Gravity.CENTER_VERTICAL);
        searchText.setPadding(0, 0, 0, 0);
        searchText.setBackgroundColor(Color.TRANSPARENT);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s == null ? "" : s.toString();
                refreshHomeList();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        LinearLayout.LayoutParams searchTextParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 58), 1f);
        searchTextParams.setMargins(UiFactory.dp(this, 12), 0, 0, 0);
        search.addView(searchText, searchTextParams);

        Button advanced = new Button(this);
        advanced.setText("Filter");
        advanced.setTextColor(Color.WHITE);
        advanced.setAllCaps(false);
        advanced.setTypeface(Typeface.DEFAULT_BOLD);
        advanced.setGravity(Gravity.CENTER);
        advanced.setBackground(UiFactory.roundedStroke(this, Color.argb(135, 5, 17, 25), 24, Color.argb(95, 255, 255, 255), 1));
        advanced.setOnClickListener(v -> showAdvancedFilterDialog());
        LinearLayout.LayoutParams advancedParams = new LinearLayout.LayoutParams(
                UiFactory.dp(this, 108),
                UiFactory.dp(this, 46)
        );
        advancedParams.setMargins(UiFactory.dp(this, 8), 0, 0, 0);
        search.addView(advanced, advancedParams);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        searchParams.setMargins(0, UiFactory.dp(this, 28), 0, 0);
        hero.addView(search, searchParams);
        return hero;
    }

    private LinearLayout buildHomeListPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 14), UiFactory.dp(this, 18), UiFactory.dp(this, 18));
        panel.setBackground(UiFactory.roundedStroke(this,
                Color.argb(122, 4, 14, 22),
                28,
                Color.argb(90, 255, 255, 255),
                1));

        panel.addView(buildPanelHandle());
        panel.addView(buildFilterBar());
        panel.addView(buildTitleRow());

        List<Toilet> toilets = filteredToilets();
        if (toilets.isEmpty()) {
            TextView empty = UiFactory.label(this, "No toilets match the current filters.", 16, Color.argb(220, 255, 255, 255), false);
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

    private void refreshHomeList() {
        if (homePage == null || homeListPanelView == null) {
            return;
        }
        int index = homePage.indexOfChild(homeListPanelView);
        if (index < 0) {
            return;
        }
        homePage.removeView(homeListPanelView);
        homeListPanelView = buildHomeListPanel();
        homePage.addView(homeListPanelView, index);
    }

    private View buildMapTab() {
        FrameLayout root = new FrameLayout(this);
        root.addView(mapPanel(), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 16), UiFactory.dp(this, 18), UiFactory.dp(this, 15));
        header.setBackground(UiFactory.roundedStroke(this,
                Color.argb(238, 5, 17, 25),
                22,
                Color.argb(120, 77, 105, 113),
                1));
        header.setElevation(UiFactory.dp(this, 8));

        TextView eyebrow = UiFactory.label(this, "MAP VIEW", 11, Color.rgb(230, 176, 58), true);
        eyebrow.setLetterSpacing(0.24f);
        eyebrow.setShadowLayer(UiFactory.dp(this, 2), 0, UiFactory.dp(this, 1), Color.argb(180, 0, 0, 0));
        header.addView(eyebrow);

        TextView title = UiFactory.label(this, "Campus map overview", 23, Color.WHITE, true);
        title.setIncludeFontPadding(false);
        title.setShadowLayer(UiFactory.dp(this, 3), 0, UiFactory.dp(this, 1), Color.argb(210, 0, 0, 0));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, UiFactory.dp(this, 8), 0, 0);
        header.addView(title, titleParams);

        TextView subtitle = UiFactory.label(this,
                "Tap a marker, then tap the info window for details.",
                13,
                Color.rgb(230, 242, 240),
                false);
        subtitle.setShadowLayer(UiFactory.dp(this, 2), 0, UiFactory.dp(this, 1), Color.argb(190, 0, 0, 0));
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, UiFactory.dp(this, 6), 0, 0);
        header.addView(subtitle, subtitleParams);

        FrameLayout.LayoutParams headerParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headerParams.setMargins(UiFactory.dp(this, 16), UiFactory.dp(this, 20), UiFactory.dp(this, 16), 0);
        root.addView(header, headerParams);
        return root;
    }

    private View buildRankingTab() {
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

        page.addView(buildBrandRow());

        TextView eyebrow = UiFactory.label(this, "RANKING BOARD", 12, Color.argb(220, 255, 255, 255), false);
        eyebrow.setLetterSpacing(0.24f);
        LinearLayout.LayoutParams eyebrowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        eyebrowParams.setMargins(0, UiFactory.dp(this, 28), 0, 0);
        page.addView(eyebrow, eyebrowParams);

        TextView title = UiFactory.label(this, "Top rated washrooms across campus", 29, Color.WHITE, true);
        title.setLineSpacing(UiFactory.dp(this, 5), 1f);
        title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, UiFactory.dp(this, 10), 0, 0);
        page.addView(title, titleParams);

        page.addView(buildRankingPanel());
        return scrollView;
    }

    private LinearLayout buildRankingPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 14), UiFactory.dp(this, 18), UiFactory.dp(this, 18));
        panel.setBackground(UiFactory.frostedPanel(this, 28));

        panel.addView(buildPanelHandle());

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

    private View buildMineTab() {
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

        page.addView(buildBrandRow());

        TextView eyebrow = UiFactory.label(this, "PROFILE", 12, Color.argb(220, 255, 255, 255), false);
        eyebrow.setLetterSpacing(0.24f);
        LinearLayout.LayoutParams eyebrowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        eyebrowParams.setMargins(0, UiFactory.dp(this, 28), 0, 0);
        page.addView(eyebrow, eyebrowParams);

        page.addView(buildMinePanel());
        return scrollView;
    }

    private LinearLayout buildMinePanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 14), UiFactory.dp(this, 18), UiFactory.dp(this, 18));
        panel.setBackground(UiFactory.frostedPanel(this, 28));

        panel.addView(buildPanelHandle());

        User currentUser = repository.getCurrentUser();

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, UiFactory.dp(this, 18), 0, UiFactory.dp(this, 18));

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.drawable.ic_user);
        avatar.setColorFilter(UiFactory.DARK_GREEN);
        avatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        avatar.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 18), UiFactory.dp(this, 18), UiFactory.dp(this, 18));
        avatar.setBackground(UiFactory.roundedStroke(this, Color.argb(145, 5, 17, 25), 44, Color.argb(95, 255, 255, 255), 1));
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

        int favoriteCount = repository.getCurrentUserFavoriteToilets().size();
        int pendingSubmissions = 0;
        int totalSubmissions = 0;
        for (ContentSubmission submission : repository.getCurrentUserSubmissions()) {
            totalSubmissions++;
            if (submission.isPending()) {
                pendingSubmissions++;
            }
        }

        panel.addView(menuRow(R.drawable.ic_favorite, "My favorites", favoriteCount + " saved toilets", UiFactory.PINK,
                v -> startActivity(new Intent(this, FavoritesActivity.class))));
        panel.addView(menuRow(R.drawable.ic_toilet, "My submissions",
                pendingSubmissions + " pending · " + totalSubmissions + " total",
                UiFactory.DARK_GREEN,
                v -> startActivity(new Intent(this, MySubmissionsActivity.class))));
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

    private LinearLayout buildBrandRow() {
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
        return brandRow;
    }

    private LinearLayout buildPanelHandle() {
        LinearLayout handle = new LinearLayout(this);
        handle.setBackground(UiFactory.rounded(this, Color.argb(170, 255, 255, 255), 4));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(
                UiFactory.dp(this, 54),
                UiFactory.dp(this, 6)
        );
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handle.setLayoutParams(handleParams);
        return handle;
    }

    private FrameLayout mapPanel() {
        FrameLayout panel = new FrameLayout(this);
        panel.setBackgroundColor(Color.rgb(7, 17, 28));

        destroyMapView();
        mapView = new MapView(this);
        mapView.onCreate(mapViewState);
        if (activityResumed) {
            mapView.onResume();
        }
        panel.addView(mapView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        mapView.getMapAsync(this::setupGoogleMap);
        return panel;
    }

    private void setupGoogleMap(GoogleMap map) {
        if (map == null) {
            return;
        }

        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.clear();

        Toilet focusedToilet = focusedMapToiletId == null ? null : repository.getToiletById(focusedMapToiletId);
        LatLng cameraTarget = focusedToilet == null
                ? new LatLng(22.2834, 114.1367)
                : new LatLng(focusedToilet.latitude, focusedToilet.longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraTarget, focusedToilet == null ? 16.5f : 18f));

        for (Toilet toilet : repository.getToilets()) {
            MarkerOptions options = new MarkerOptions()
                    .position(new LatLng(toilet.latitude, toilet.longitude))
                    .title(toilet.building + " " + toilet.floor)
                    .snippet(String.format(Locale.US, "Rating %.1f · %s · tap for details", toilet.avgOverall, toilet.crowdLabel()))
                    .icon(BitmapDescriptorFactory.defaultMarker(markerHue(toilet)));
            com.google.android.gms.maps.model.Marker marker = map.addMarker(options);
            if (marker != null) {
                marker.setTag(toilet.id);
                if (toilet.id.equals(focusedMapToiletId)) {
                    marker.showInfoWindow();
                }
            }
        }

        map.setOnInfoWindowClickListener(marker -> {
            Object object = marker.getTag();
            if (object instanceof String) {
                openDetail((String) object);
            }
        });
    }

    private float markerHue(Toilet toilet) {
        if (toilet.id.equals(focusedMapToiletId)) {
            return BitmapDescriptorFactory.HUE_YELLOW;
        }
        if ("male".equals(toilet.gender)) {
            return BitmapDescriptorFactory.HUE_AZURE;
        }
        if ("female".equals(toilet.gender)) {
            return BitmapDescriptorFactory.HUE_ROSE;
        }
        return BitmapDescriptorFactory.HUE_GREEN;
    }

    private void destroyMapView() {
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
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
        row.addView(actionIcon(R.drawable.ic_filter, Color.WHITE, UiFactory.TEXT, this::showAdvancedFilterDialog));
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
                : UiFactory.roundedStroke(this, Color.argb(118, 5, 17, 25), 24, Color.argb(65, 255, 255, 255), 1));
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
        icon.setBackground(UiFactory.roundedStroke(this, Color.argb(118, 5, 17, 25), 24, Color.argb(75, 255, 255, 255), 1));
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
        titleRow.addView(UiFactory.label(this, "Nearby Toilets", 20, Color.WHITE, true), new LinearLayout.LayoutParams(0, UiFactory.dp(this, 46), 1f));

        TextView sort = UiFactory.label(this, sortMode == 0 ? "Distance" : "Rating", 14, Color.WHITE, true);
        sort.setGravity(Gravity.CENTER);
        sort.setBackground(UiFactory.rounded(this, Color.argb(135, 5, 17, 25), 20));
        sort.setOnClickListener(v -> showSortDialog());
        titleRow.addView(sort, new LinearLayout.LayoutParams(UiFactory.dp(this, 104), UiFactory.dp(this, 40)));
        return titleRow;
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

        nav.addView(navItem(R.drawable.ic_home, "Home", currentTab == TAB_HOME, v -> switchTab(TAB_HOME)));
        nav.addView(navItem(R.drawable.ic_map_marker, "Map", currentTab == TAB_MAP, v -> switchTab(TAB_MAP)));
        nav.addView(navItem(R.drawable.ic_medal_gold, "Ranking", currentTab == TAB_RANKING, v -> switchTab(TAB_RANKING)));
        nav.addView(navItem(R.drawable.ic_user, "Mine", currentTab == TAB_MINE, v -> switchTab(TAB_MINE)));
        return nav;
    }

    private void switchTab(int tab) {
        int normalized = normalizeTab(tab);
        if (currentTab == normalized) {
            return;
        }
        if (currentTab == TAB_MAP && normalized != TAB_MAP) {
            destroyMapView();
        }
        currentTab = normalized;
        setContentView(buildContent());
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

        TextView name = UiFactory.label(this, toilet.building, 18, Color.WHITE, false);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        info.addView(name, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView meta = UiFactory.label(this,
                toilet.floor + " | " + toilet.genderLabel(),
                13,
                Color.argb(220, 255, 255, 255),
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
                Color.WHITE,
                true);
        distance.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        side.addView(distance, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        return row;
    }

    private LinearLayout rankingRow(int rank, Toilet toilet) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiFactory.dp(this, 14), 0, UiFactory.dp(this, 14));
        row.setOnClickListener(v -> openDetail(toilet.id));

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
        number.setBackground(UiFactory.rounded(this, Color.rgb(166, 174, 183), 22));
        return withSize(number, UiFactory.dp(this, 44), UiFactory.dp(this, 44));
    }

    private View withSize(View view, int width, int height) {
        view.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        return view;
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
        icon.setBackground(UiFactory.roundedStroke(this, Color.argb(110, 5, 17, 25), 24, Color.argb(70, 255, 255, 255), 1));
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
        String query = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.US);
        for (Toilet toilet : repository.getToilets()) {
            boolean hasGenderFilter = showMale || showFemale;
            boolean genderMatches = (showMale && ("male".equals(toilet.gender) || "all".equals(toilet.gender)))
                    || (showFemale && ("female".equals(toilet.gender) || "all".equals(toilet.gender)));
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
            if (!query.isEmpty() && !matchesSearch(toilet, query)) {
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

    private boolean matchesSearch(Toilet toilet, String query) {
        String searchable = (toilet.building + " "
                + toilet.floor + " "
                + toilet.genderLabel() + " "
                + toilet.facilitiesLabel() + " "
                + toilet.openingHours + " "
                + toilet.note).toLowerCase(Locale.US);
        return searchable.contains(query);
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

    private void showAdvancedFilterDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = UiFactory.dp(this, 18);
        content.setPadding(padding, UiFactory.dp(this, 12), padding, 0);
        content.setBackground(UiFactory.rounded(this, Color.rgb(16, 25, 36), 24));

        CheckBox male = filterCheckBox("Male toilets", showMale);
        CheckBox female = filterCheckBox("Female toilets", showFemale);
        CheckBox accessible = filterCheckBox("Accessible toilets", showAccessible);
        CheckBox tissue = filterCheckBox("Tissue available", requireTissue);
        CheckBox dryer = filterCheckBox("Hand dryer available", requireDryer);
        content.addView(male);
        content.addView(female);
        content.addView(accessible);
        content.addView(tissue);
        content.addView(dryer);

        TextView sortTitle = UiFactory.label(this, "Sort by", 15, Color.WHITE, true);
        LinearLayout.LayoutParams sortTitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sortTitleParams.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        content.addView(sortTitle, sortTitleParams);

        RadioGroup sortGroup = new RadioGroup(this);
        sortGroup.setOrientation(RadioGroup.VERTICAL);
        sortGroup.setPadding(0, UiFactory.dp(this, 6), 0, UiFactory.dp(this, 8));
        RadioButton distance = new RadioButton(this);
        distance.setText("Distance");
        distance.setTextSize(15);
        distance.setTextColor(Color.WHITE);
        distance.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.rgb(230, 176, 58)));
        distance.setId(1001);
        RadioButton rating = new RadioButton(this);
        rating.setText("Overall rating");
        rating.setTextSize(15);
        rating.setTextColor(Color.WHITE);
        rating.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.rgb(230, 176, 58)));
        rating.setId(1002);
        sortGroup.addView(distance);
        sortGroup.addView(rating);
        sortGroup.check(sortMode == 1 ? 1002 : 1001);
        content.addView(sortGroup);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Advanced filter")
                .setView(content)
                .setNegativeButton("Reset", (dialogInterface, which) -> {
                    showMale = false;
                    showFemale = false;
                    showAccessible = false;
                    requireTissue = false;
                    requireDryer = false;
                    sortMode = 0;
                    searchQuery = "";
                    setContentView(buildContent());
                })
                .setPositiveButton("Apply", (dialogInterface, which) -> {
                    showMale = male.isChecked();
                    showFemale = female.isChecked();
                    showAccessible = accessible.isChecked();
                    requireTissue = tissue.isChecked();
                    requireDryer = dryer.isChecked();
                    sortMode = sortGroup.getCheckedRadioButtonId() == 1002 ? 1 : 0;
                    setContentView(buildContent());
                })
                .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(UiFactory.rounded(this, Color.argb(245, 10, 18, 28), 26));
        }
        TextView titleView = dialog.findViewById(android.R.id.title);
        if (titleView != null) {
            titleView.setTextColor(Color.WHITE);
        }
        Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (negative != null) {
            negative.setTextColor(Color.rgb(230, 176, 58));
        }
        if (positive != null) {
            positive.setTextColor(Color.WHITE);
        }
    }

    private CheckBox filterCheckBox(String text, boolean checked) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(text);
        checkBox.setTextSize(15);
        checkBox.setTextColor(Color.WHITE);
        checkBox.setChecked(checked);
        checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.rgb(230, 176, 58)));
        checkBox.setGravity(Gravity.CENTER_VERTICAL);
        checkBox.setPadding(0, UiFactory.dp(this, 4), 0, UiFactory.dp(this, 4));
        return checkBox;
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
            builder.append(i < filled ? "★" : "☆");
        }
        return builder.toString();
    }

    private void openDetail(String toiletId) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_TOILET_ID, toiletId);
        startActivity(intent);
    }

    private int normalizeTab(int tab) {
        if (tab < TAB_HOME || tab > TAB_MINE) {
            return TAB_HOME;
        }
        return tab;
    }

    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
}
