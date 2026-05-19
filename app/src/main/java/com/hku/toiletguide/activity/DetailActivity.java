package com.hku.toiletguide.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.model.ContentSubmission;
import com.hku.toiletguide.model.LiveStatusReport;
import com.hku.toiletguide.model.Review;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.util.DistanceUtil;
import com.hku.toiletguide.util.UiFactory;

import java.util.List;
import java.util.Locale;

public class DetailActivity extends Activity {
    public static final String EXTRA_TOILET_ID = "toilet_id";

    private final MockToiletRepository repository = MockToiletRepository.getInstance();
    private String toiletId;
    private int selectedTab = 0;
    private boolean adminHistoryExpanded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toiletId = getIntent().getStringExtra(EXTRA_TOILET_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(buildContent());
    }

    private View buildContent() {
        Toilet toilet = repository.getToiletById(toiletId);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(7, 17, 28));

        ImageView background = new ImageView(this);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int backgroundRes = getDrawableId("corridor");
        if (backgroundRes != 0) {
            background.setImageResource(backgroundRes);
        } else {
            background.setBackgroundColor(Color.rgb(46, 54, 67));
        }
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

        if (toilet == null) {
            page.addView(buildTopBar());
            LinearLayout missing = section("Toilet not found");
            TextView hint = UiFactory.label(this, "Return to the list and choose another point.", 16, Color.argb(220, 255, 255, 255), false);
            missing.addView(hint);
            page.addView(missing);
        } else {
            page.addView(buildTopBar());
            page.addView(buildHeader(toilet));
            page.addView(buildTabs());
            if (selectedTab == 0) {
                page.addView(buildDescription(toilet));
            } else {
                page.addView(buildRatingSection(toilet));
            }
        }

        root.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return root;
    }

    private LinearLayout buildTopBar() {
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackground(UiFactory.darkOverlayPanel(this, 24));
        top.setPadding(UiFactory.dp(this, 12), UiFactory.dp(this, 8), UiFactory.dp(this, 12), UiFactory.dp(this, 8));

        ImageView backIcon = new ImageView(this);
        backIcon.setImageResource(R.drawable.ic_back);
        backIcon.setColorFilter(Color.WHITE);
        backIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        backIcon.setPadding(UiFactory.dp(this, 8), UiFactory.dp(this, 8), UiFactory.dp(this, 8), UiFactory.dp(this, 8));
        backIcon.setOnClickListener(v -> finish());
        top.addView(backIcon, new LinearLayout.LayoutParams(UiFactory.dp(this, 44), UiFactory.dp(this, 44)));

        TextView back = UiFactory.label(this, "Back", 17, Color.WHITE, true);
        back.setGravity(Gravity.CENTER_VERTICAL);
        back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(0, UiFactory.dp(this, 48), 1f));

        TextView share = iconButton("Share");
        TextView call = iconButton("Call");
        top.addView(share);
        top.addView(call);
        return top;
    }

    private TextView iconButton(String text) {
        TextView view = UiFactory.label(this, text, 12, Color.WHITE, true);
        view.setGravity(Gravity.CENTER);
        view.setBackground(UiFactory.roundedStroke(this, Color.argb(86, 5, 17, 25), 18, Color.argb(100, 255, 255, 255), 1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UiFactory.dp(this, 54), UiFactory.dp(this, 38));
        params.setMargins(UiFactory.dp(this, 6), 0, 0, 0);
        view.setLayoutParams(params);
        return view;
    }

    private LinearLayout buildHeader(Toilet toilet) {
        LinearLayout header = section("");
        header.setPadding(UiFactory.dp(this, 16), UiFactory.dp(this, 20), UiFactory.dp(this, 16), UiFactory.dp(this, 20));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView genderBadge = UiFactory.label(this, genderShort(toilet), 17, Color.WHITE, true);
        genderBadge.setGravity(Gravity.CENTER);
        genderBadge.setBackground(UiFactory.rounded(this, genderColor(toilet), 28));
        titleRow.addView(genderBadge, new LinearLayout.LayoutParams(UiFactory.dp(this, 58), UiFactory.dp(this, 58)));

        TextView title = UiFactory.label(this, toilet.building + " " + toilet.floor, 29, Color.WHITE, true);
        title.setLineSpacing(UiFactory.dp(this, 2), 1f);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.setMargins(UiFactory.dp(this, 14), 0, 0, 0);
        titleRow.addView(title, titleParams);
        titleRow.addView(favoriteButton(toilet), new LinearLayout.LayoutParams(
                UiFactory.dp(this, 46),
                UiFactory.dp(this, 46)
        ));
        header.addView(titleRow);
        header.addView(facilityTags(toilet));

        LinearLayout locationRow = new LinearLayout(this);
        locationRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams locationParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        locationParams.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        header.addView(locationRow, locationParams);

        TextView address = UiFactory.label(this,
                "HKU Campus | " + DistanceUtil.metersLabel(DistanceUtil.distanceFromHkuCenter(toilet.latitude, toilet.longitude)),
                16,
                Color.argb(220, 255, 255, 255),
                false);
        locationRow.addView(address, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button navigate = UiFactory.primaryButton(this, "Go");
        navigate.setTypeface(Typeface.DEFAULT_BOLD);
        navigate.setOnClickListener(v -> navigateTo(toilet));
        locationRow.addView(navigate, new LinearLayout.LayoutParams(UiFactory.dp(this, 104), UiFactory.dp(this, 46)));
        return header;
    }

    private ImageView favoriteButton(Toilet toilet) {
        boolean favorite = repository.isFavorite(toilet.id);
        ImageView favoriteIcon = new ImageView(this);
        favoriteIcon.setImageResource(R.drawable.ic_favorite);
        favoriteIcon.setColorFilter(favorite ? UiFactory.PINK : Color.WHITE);
        favoriteIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        favoriteIcon.setPadding(UiFactory.dp(this, 9), UiFactory.dp(this, 9), UiFactory.dp(this, 9), UiFactory.dp(this, 9));
        favoriteIcon.setBackground(favorite
                ? UiFactory.rounded(this, Color.argb(170, 253, 232, 241), 23)
                : UiFactory.roundedStroke(this, Color.argb(86, 5, 17, 25), 23, Color.argb(100, 255, 255, 255), 1));
        favoriteIcon.setOnClickListener(v -> {
            repository.toggleFavorite(toilet.id);
            Toast.makeText(this, repository.isFavorite(toilet.id) ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
            setContentView(buildContent());
        });
        return favoriteIcon;
    }

    private LinearLayout facilityTags(Toilet toilet) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        scrollParams.setMargins(UiFactory.dp(this, 72), UiFactory.dp(this, 10), 0, 0);
        scroll.setLayoutParams(scrollParams);

        LinearLayout tags = new LinearLayout(this);
        tags.setOrientation(LinearLayout.HORIZONTAL);
        tags.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(tags, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        tags.addView(tag("Stalls " + toilet.stalls));
        if (toilet.accessible) {
            tags.addView(tag("Accessible"));
        }
        if (toilet.hasTissue) {
            tags.addView(tag("Tissue"));
        }
        if (toilet.hasDryer) {
            tags.addView(tag("Dryer"));
        }
        if (toilet.hasMirror) {
            tags.addView(tag("Mirror"));
        }
        return wrapSingleChild(scroll);
    }

    private LinearLayout wrapSingleChild(View child) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(child);
        return wrapper;
    }

    private TextView tag(String text) {
        TextView tag = UiFactory.label(this, text, 12, Color.WHITE, true);
        tag.setGravity(Gravity.CENTER);
        tag.setBackground(UiFactory.roundedStroke(this, Color.argb(102, 5, 17, 25), 14, Color.argb(70, 255, 255, 255), 1));
        tag.setPadding(UiFactory.dp(this, 10), 0, UiFactory.dp(this, 10), 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                UiFactory.dp(this, 30)
        );
        params.setMargins(0, 0, UiFactory.dp(this, 8), 0);
        tag.setLayoutParams(params);
        return tag;
    }

    private LinearLayout buildTabs() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setGravity(Gravity.CENTER);
        tabs.setPadding(0, UiFactory.dp(this, 12), 0, UiFactory.dp(this, 4));

        tabs.addView(tab("Overview", selectedTab == 0, 0));
        tabs.addView(tab("Reviews", selectedTab == 1, 1));
        return tabs;
    }

    private TextView tab(String text, boolean active, int tabIndex) {
        TextView view = UiFactory.label(this, text, 17, Color.WHITE, true);
        view.setGravity(Gravity.CENTER);
        view.setBackground(active
                ? UiFactory.rounded(this, Color.argb(150, 0, 126, 111), 20)
                : UiFactory.roundedStroke(this, Color.argb(70, 5, 17, 25), 20, Color.argb(55, 255, 255, 255), 1));
        view.setOnClickListener(v -> {
            selectedTab = tabIndex;
            setContentView(buildContent());
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 46), 1f);
        params.setMargins(tabIndex == 0 ? 0 : UiFactory.dp(this, 8), 0, 0, 0);
        view.setLayoutParams(params);
        return view;
    }

    private LinearLayout buildDescription(Toilet toilet) {
        LinearLayout section = section("Overview");

        TextView note = UiFactory.label(this, toilet.note, 17, Color.WHITE, false);
        note.setLineSpacing(UiFactory.dp(this, 5), 1f);
        section.addView(note);

        LinearLayout scorePanel = new LinearLayout(this);
        scorePanel.setOrientation(LinearLayout.VERTICAL);
        scorePanel.setPadding(UiFactory.dp(this, 12), UiFactory.dp(this, 12), UiFactory.dp(this, 12), UiFactory.dp(this, 12));
        scorePanel.setBackground(UiFactory.rounded(this, Color.argb(92, 5, 17, 25), 12));
        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        scoreParams.setMargins(0, UiFactory.dp(this, 14), 0, 0);
        section.addView(scorePanel, scoreParams);

        scorePanel.addView(ratingRow("Cleanliness", toilet.avgCleanliness));
        scorePanel.addView(ratingRow("Quietness", quietnessScore(toilet.avgCrowdedness)));
        scorePanel.addView(ratingRow("Overall", toilet.avgOverall));

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams statsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statsParams.setMargins(0, UiFactory.dp(this, 16), 0, 0);
        section.addView(stats, statsParams);

        stats.addView(crowdStatCard(toilet));
        stats.addView(statCard("Open", toilet.openingHours, Color.rgb(140, 196, 255)));
        stats.addView(statCard("Rating", String.format(Locale.US, "%.1f", toilet.avgOverall), Color.rgb(245, 179, 53)));
        section.addView(liveStatusSection(toilet));
        if (isAdmin()) {
            section.addView(adminActionsSection(toilet));
        }
        section.addView(reportActions(toilet));
        return section;
    }

    private LinearLayout buildRatingSection(Toilet toilet) {
        LinearLayout section = section("Ratings and Reviews");

        if (toilet.reviews.isEmpty()) {
            TextView empty = UiFactory.label(this, "No reviews yet. Be the first to leave a review.", 16, Color.argb(220, 255, 255, 255), false);
            empty.setGravity(Gravity.CENTER);
            section.addView(empty, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    UiFactory.dp(this, 110)
            ));
        } else {
            for (Review reviewItem : toilet.reviews) {
                section.addView(reviewCard(reviewItem));
            }
        }

        section.addView(addReviewRow(toilet));
        return section;
    }

    private LinearLayout addReviewRow(Toilet toilet) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, UiFactory.dp(this, 14), 0, 0);
        row.setLayoutParams(rowParams);

        Button review = UiFactory.primaryButton(this, "Add review");
        review.setGravity(Gravity.CENTER);
        review.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReviewActivity.class);
            intent.putExtra(EXTRA_TOILET_ID, toilet.id);
            startActivity(intent);
        });
        row.addView(review, new LinearLayout.LayoutParams(
                UiFactory.dp(this, 132),
                UiFactory.dp(this, 48)
        ));
        return row;
    }

    private LinearLayout liveStatusSection(Toilet toilet) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setBackground(UiFactory.rounded(this, Color.argb(92, 5, 17, 25), 12));
        block.setPadding(UiFactory.dp(this, 12), UiFactory.dp(this, 12), UiFactory.dp(this, 12), UiFactory.dp(this, 12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 16), 0, 0);
        block.setLayoutParams(params);

        block.addView(UiFactory.label(this, "Current Live Status", 17, Color.WHITE, true));
        List<LiveStatusReport> statuses = repository.getLatestActiveStatuses(toilet.id);
        HorizontalScrollView chipsScroll = new HorizontalScrollView(this);
        chipsScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams chipsScrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        chipsScrollParams.setMargins(0, UiFactory.dp(this, 10), 0, 0);
        block.addView(chipsScroll, chipsScrollParams);

        LinearLayout chipsRow = new LinearLayout(this);
        chipsRow.setOrientation(LinearLayout.HORIZONTAL);
        chipsRow.setGravity(Gravity.CENTER_VERTICAL);
        chipsScroll.addView(chipsRow, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        chipsRow.addView(statusChip(latestForGroup(statuses, LiveStatusReport.GROUP_TISSUE, defaultStatusForToilet(LiveStatusReport.GROUP_TISSUE, toilet))));
        chipsRow.addView(statusChip(latestForGroup(statuses, LiveStatusReport.GROUP_SOAP, defaultStatusForToilet(LiveStatusReport.GROUP_SOAP, toilet))));
        chipsRow.addView(statusChip(latestForGroup(statuses, LiveStatusReport.GROUP_DRYER, defaultStatusForToilet(LiveStatusReport.GROUP_DRYER, toilet))));
        chipsRow.addView(statusChip(latestForGroup(statuses, LiveStatusReport.GROUP_OPERATION, defaultStatusForToilet(LiveStatusReport.GROUP_OPERATION, toilet))));

        if (statuses.isEmpty()) {
            return block;
        }

        TextView caption = UiFactory.label(this, "Latest active reports", 13, Color.argb(210, 255, 255, 255), true);
        LinearLayout.LayoutParams captionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        captionParams.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        block.addView(caption, captionParams);
        for (LiveStatusReport report : statuses) {
            TextView item = UiFactory.label(this,
                    LiveStatusReport.groupLabel(report.group()) + ": " + report.label() + " · " + report.userName,
                    14,
                    Color.WHITE,
                    false);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            itemParams.setMargins(0, UiFactory.dp(this, 8), 0, 0);
            block.addView(item, itemParams);
        }
        return block;
    }

    private LiveStatusReport latestForGroup(List<LiveStatusReport> statuses, String group, String fallbackStatusCode) {
        for (LiveStatusReport report : statuses) {
            if (group.equals(report.group())) {
                return report;
            }
        }
        return new LiveStatusReport("fallback_" + group, toiletId, "", "System", fallbackStatusCode, 0L);
    }

    private String defaultStatusForToilet(String group, Toilet toilet) {
        if (LiveStatusReport.GROUP_TISSUE.equals(group)) {
            return toilet.hasTissue ? LiveStatusReport.STATUS_TISSUE_OK : LiveStatusReport.STATUS_TISSUE_LOW;
        }
        if (LiveStatusReport.GROUP_SOAP.equals(group)) {
            return toilet.hasSoap ? LiveStatusReport.STATUS_SOAP_OK : LiveStatusReport.STATUS_SOAP_LOW;
        }
        if (LiveStatusReport.GROUP_DRYER.equals(group)) {
            return toilet.hasDryer ? LiveStatusReport.STATUS_DRYER_OK : LiveStatusReport.STATUS_DRYER_BROKEN;
        }
        return LiveStatusReport.STATUS_OPEN;
    }

    private TextView statusChip(LiveStatusReport report) {
        TextView chip = UiFactory.label(this,
                LiveStatusReport.titleFor(report.statusCode) + "  " + LiveStatusReport.shortLabelFor(report.statusCode),
                12,
                LiveStatusReport.textColorFor(report.statusCode),
                true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(UiFactory.dp(this, 12), UiFactory.dp(this, 8), UiFactory.dp(this, 12), UiFactory.dp(this, 8));
        chip.setBackground(UiFactory.rounded(this, LiveStatusReport.colorFor(report.statusCode), 18));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0f
        );
        params.setMargins(0, 0, UiFactory.dp(this, 8), 0);
        chip.setLayoutParams(params);
        return chip;
    }

    private LinearLayout reportActions(Toilet toilet) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);

        if (isAdmin()) {
            TextView adminHint = UiFactory.label(this,
                    "Users can report here. Admin direct-edit tools are shown above for this toilet.",
                    13,
                    Color.argb(210, 255, 255, 255),
                    false);
            adminHint.setLineSpacing(UiFactory.dp(this, 2), 1f);
            row.addView(adminHint);
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionsParams.setMargins(0, UiFactory.dp(this, 14), 0, 0);
        actions.setLayoutParams(actionsParams);

        Button crowd = UiFactory.primaryButton(this, "Report crowd");
        crowd.setOnClickListener(v -> showCrowdDialog(toilet));
        LinearLayout.LayoutParams crowdParams = new LinearLayout.LayoutParams(
                0,
                UiFactory.dp(this, 48),
                1f
        );
        crowdParams.setMargins(0, 0, UiFactory.dp(this, 8), 0);
        actions.addView(crowd, crowdParams);

        Button status = UiFactory.primaryButton(this, "Report status");
        status.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatusReportActivity.class);
            intent.putExtra(EXTRA_TOILET_ID, toilet.id);
            startActivity(intent);
        });
        actions.addView(status, new LinearLayout.LayoutParams(0, UiFactory.dp(this, 48), 1f));
        row.addView(actions);
        return row;
    }

    private LinearLayout adminActionsSection(Toilet toilet) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setBackground(UiFactory.rounded(this, Color.argb(92, 5, 17, 25), 12));
        block.setPadding(UiFactory.dp(this, 12), UiFactory.dp(this, 12), UiFactory.dp(this, 12), UiFactory.dp(this, 12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 16), 0, 0);
        block.setLayoutParams(params);

        block.addView(UiFactory.label(this, "Admin Controls", 17, Color.WHITE, true));
        TextView hint = UiFactory.label(this,
                "Change this toilet directly here. Use Admin Dashboard only for pending reviews and active issue queue.",
                13,
                Color.argb(210, 255, 255, 255),
                false);
        hint.setLineSpacing(UiFactory.dp(this, 2), 1f);
        block.addView(hint, topMargin(8));

        LinearLayout quickActions = new LinearLayout(this);
        quickActions.setGravity(Gravity.CENTER_VERTICAL);
        quickActions.addView(adminActionButton("Set crowd", v -> showCrowdDialog(toilet), true));
        quickActions.addView(adminActionButton("Set status", v -> showAdminStatusDialog(toilet), false));
        block.addView(quickActions, topMargin(12));

        int pendingCount = pendingSubmissionCount(toilet.id);
        if (pendingCount > 0) {
            TextView pending = UiFactory.label(this,
                    pendingCount + " pending submissions are waiting for moderation for this toilet.",
                    13,
                    Color.argb(210, 255, 255, 255),
                    false);
            pending.setLineSpacing(UiFactory.dp(this, 2), 1f);
            block.addView(pending, topMargin(14));
        }

        block.addView(adminHistorySection(toilet), topMargin(16));
        return block;
    }

    private LinearLayout adminHistorySection(Toilet toilet) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);

        Button toggle = new Button(this);
        toggle.setText(adminHistoryExpanded ? "Collapse edit records" : "Expand edit records");
        toggle.setAllCaps(false);
        toggle.setTextColor(Color.WHITE);
        toggle.setBackground(UiFactory.roundedStroke(this, Color.argb(86, 5, 17, 25), 14, Color.argb(95, 255, 255, 255), 1));
        toggle.setOnClickListener(v -> {
            adminHistoryExpanded = !adminHistoryExpanded;
            setContentView(buildContent());
        });
        section.addView(toggle, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 46)
        ));

        if (!adminHistoryExpanded) {
            return section;
        }

        List<LiveStatusReport> records = repository.getLiveStatusReports(toilet.id);
        if (records.isEmpty()) {
            section.addView(UiFactory.label(this, "No edit records yet.", 13, Color.argb(210, 255, 255, 255), false), topMargin(10));
            return section;
        }

        section.addView(UiFactory.label(this, "Edit records", 14, Color.WHITE, true), topMargin(12));
        for (LiveStatusReport report : records) {
            section.addView(adminStatusRow(report), topMargin(8));
        }
        return section;
    }

    private LinearLayout adminStatusRow(LiveStatusReport report) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(UiFactory.dp(this, 10), UiFactory.dp(this, 10), UiFactory.dp(this, 10), UiFactory.dp(this, 10));
        row.setBackground(UiFactory.rounded(this, Color.argb(92, 16, 31, 44), 10));

        row.addView(UiFactory.label(this,
                LiveStatusReport.groupLabel(report.group()) + " · " + report.label(),
                14,
                Color.WHITE,
                true));
        row.addView(UiFactory.label(this,
                "Reported by " + report.userName + " · " + report.createdLabel(),
                12,
                Color.argb(210, 255, 255, 255),
                false), topMargin(4));

        TextView state = UiFactory.label(this,
                report.resolved
                        ? "Resolved by " + report.resolvedByUserName + " · " + report.resolvedLabel()
                        : "Active issue",
                12,
                report.resolved ? Color.argb(210, 255, 255, 255) : Color.rgb(245, 179, 53),
                true);
        row.addView(state, topMargin(6));

        if (!report.resolved && LiveStatusReport.isProblemStatus(report.statusCode)) {
            LinearLayout actions = new LinearLayout(this);
            actions.setGravity(Gravity.RIGHT);
            actions.addView(adminChipButton("Resolve", v -> {
                repository.resolveLiveStatus(report.id);
                Toast.makeText(this, "Status marked resolved", Toast.LENGTH_SHORT).show();
                setContentView(buildContent());
            }, UiFactory.DARK_GREEN));
            actions.addView(adminChipButton(normalizeLabel(report), v -> {
                repository.resolveLiveStatus(report.id);
                repository.submitLiveStatuses(report.toiletId, java.util.Collections.singletonList(restoreStatusFor(report.statusCode)));
                Toast.makeText(this, "Status restored to normal", Toast.LENGTH_SHORT).show();
                setContentView(buildContent());
            }, UiFactory.BLUE));
            row.addView(actions, topMargin(10));
        }
        return row;
    }

    private Button adminActionButton(String text, View.OnClickListener listener, boolean addRightMargin) {
        Button button = UiFactory.primaryButton(this, text);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                UiFactory.dp(this, 46),
                1f
        );
        if (addRightMargin) {
            params.setMargins(0, 0, UiFactory.dp(this, 8), 0);
        }
        button.setLayoutParams(params);
        return button;
    }

    private Button adminChipButton(String text, View.OnClickListener listener, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(color);
        button.setBackground(UiFactory.roundedStroke(this, Color.argb(86, 5, 17, 25), 14, Color.argb(85, 255, 255, 255), 1));
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                UiFactory.dp(this, 40)
        );
        params.setMargins(UiFactory.dp(this, 8), 0, 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private LinearLayout.LayoutParams topMargin(int topDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, topDp), 0, 0);
        return params;
    }

    private int pendingSubmissionCount(String targetToiletId) {
        int count = 0;
        for (ContentSubmission submission : repository.getContentSubmissions()) {
            if (targetToiletId.equals(submission.toiletId) && submission.isPending()) {
                count++;
            }
        }
        return count;
    }

    private boolean isAdmin() {
        return "admin".equals(repository.getCurrentUser().role);
    }

    private String normalizeLabel(LiveStatusReport report) {
        if (LiveStatusReport.GROUP_TISSUE.equals(report.group())) {
            return "Set tissue OK";
        }
        if (LiveStatusReport.GROUP_SOAP.equals(report.group())) {
            return "Set soap OK";
        }
        if (LiveStatusReport.GROUP_DRYER.equals(report.group())) {
            return "Set dryer OK";
        }
        return "Set open";
    }

    private String restoreStatusFor(String statusCode) {
        if (LiveStatusReport.GROUP_TISSUE.equals(LiveStatusReport.groupFor(statusCode))) {
            return LiveStatusReport.STATUS_TISSUE_OK;
        }
        if (LiveStatusReport.GROUP_SOAP.equals(LiveStatusReport.groupFor(statusCode))) {
            return LiveStatusReport.STATUS_SOAP_OK;
        }
        if (LiveStatusReport.GROUP_DRYER.equals(LiveStatusReport.groupFor(statusCode))) {
            return LiveStatusReport.STATUS_DRYER_OK;
        }
        return LiveStatusReport.STATUS_OPEN;
    }

    private void showAdminStatusDialog(Toilet toilet) {
        String[] labels = {
                "Tissue OK",
                "Tissue low",
                "Soap OK",
                "Soap low",
                "Dryer OK",
                "Dryer broken",
                "Open",
                "Under maintenance",
                "Temporarily closed"
        };
        String[] codes = {
                LiveStatusReport.STATUS_TISSUE_OK,
                LiveStatusReport.STATUS_TISSUE_LOW,
                LiveStatusReport.STATUS_SOAP_OK,
                LiveStatusReport.STATUS_SOAP_LOW,
                LiveStatusReport.STATUS_DRYER_OK,
                LiveStatusReport.STATUS_DRYER_BROKEN,
                LiveStatusReport.STATUS_OPEN,
                LiveStatusReport.STATUS_MAINTENANCE,
                LiveStatusReport.STATUS_CLOSED_TEMPORARILY
        };
        new AlertDialog.Builder(this)
                .setTitle("Admin update · " + toilet.building + " " + toilet.floor)
                .setItems(labels, (dialog, which) -> {
                    repository.submitLiveStatuses(toilet.id, java.util.Collections.singletonList(codes[which]));
                    Toast.makeText(this, "Toilet status updated", Toast.LENGTH_SHORT).show();
                    setContentView(buildContent());
                })
                .show();
    }

    private void showCrowdDialog(Toilet toilet) {
        String[] labels = {
                "Empty - no queue",
                "Not crowded - a few people",
                "Normal - usable",
                "Crowded - short wait",
                "Full - long queue"
        };
        new AlertDialog.Builder(this)
                .setTitle("How crowded is it now?")
                .setSingleChoiceItems(labels, toilet.currentCrowdLevel - 1, (dialog, which) -> {
                    repository.reportCrowdLevel(toilet.id, which + 1);
                    Toast.makeText(this, "Crowd level updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    setContentView(buildContent());
                })
                .show();
    }

    private LinearLayout section(String title) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(UiFactory.dp(this, 16), UiFactory.dp(this, 16), UiFactory.dp(this, 16), UiFactory.dp(this, 16));
        section.setBackground(UiFactory.frostedPanel(this, 18));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 14), 0, 0);
        section.setLayoutParams(params);

        if (!title.isEmpty()) {
            TextView heading = UiFactory.label(this, title, 19, Color.WHITE, true);
            LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            headingParams.setMargins(0, 0, 0, UiFactory.dp(this, 12));
            section.addView(heading, headingParams);
        }
        return section;
    }

    private LinearLayout statCard(String label, String value, int color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(UiFactory.dp(this, 8), UiFactory.dp(this, 10), UiFactory.dp(this, 8), UiFactory.dp(this, 10));
        card.setBackground(UiFactory.rounded(this, Color.argb(92, 5, 17, 25), 10));

        TextView valueView = UiFactory.label(this, value, 14, color, true);
        valueView.setGravity(Gravity.CENTER);
        card.addView(valueView);
        TextView labelView = UiFactory.label(this, label, 11, Color.argb(210, 255, 255, 255), false);
        labelView.setGravity(Gravity.CENTER);
        card.addView(labelView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(0, 0, UiFactory.dp(this, 8), 0);
        card.setLayoutParams(params);
        return card;
    }

    private LinearLayout crowdStatCard(Toilet toilet) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(UiFactory.dp(this, 8), UiFactory.dp(this, 10), UiFactory.dp(this, 8), UiFactory.dp(this, 10));
        card.setBackground(UiFactory.rounded(this, Color.argb(92, 5, 17, 25), 10));

        LinearLayout people = crowdPeople(toilet.currentCrowdLevel, UiFactory.dp(this, 16));
        people.setGravity(Gravity.CENTER);
        card.addView(people);

        TextView labelView = UiFactory.label(this, toilet.crowdLabel(), 11, Color.argb(210, 255, 255, 255), false);
        labelView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, UiFactory.dp(this, 4), 0, 0);
        card.addView(labelView, labelParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(0, 0, UiFactory.dp(this, 8), 0);
        card.setLayoutParams(params);
        return card;
    }

    private LinearLayout crowdPeople(int level, int iconSize) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        int safeLevel = Math.max(0, Math.min(5, level));
        for (int i = 1; i <= 5; i++) {
            ImageView person = new ImageView(this);
            person.setImageResource(R.drawable.ic_male);
            person.setColorFilter(i <= safeLevel ? crowdColor(safeLevel) : Color.argb(120, 255, 255, 255));
            person.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
            params.setMargins(0, 0, UiFactory.dp(this, 2), 0);
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

    private LinearLayout reviewCard(Review review) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14));
        card.setBackground(UiFactory.rounded(this, Color.argb(92, 5, 17, 25), 10));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        card.setLayoutParams(params);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(UiFactory.label(this, review.userName + " | " + review.dateLabel(), 14, Color.argb(220, 255, 255, 255), false),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(likeView(review));
        card.addView(header);

        LinearLayout ratings = new LinearLayout(this);
        ratings.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams ratingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ratingParams.setMargins(0, UiFactory.dp(this, 8), 0, 0);
        card.addView(ratings, ratingParams);
        ratings.addView(ratingRow("Clean", review.cleanliness));
        ratings.addView(ratingRow("Quiet", quietnessScore(review.crowdedness)));
        ratings.addView(ratingRow("Overall", review.overall));

        TextView comment = UiFactory.label(this, review.comment, 15, Color.WHITE, false);
        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        commentParams.setMargins(0, UiFactory.dp(this, 6), 0, 0);
        card.addView(comment, commentParams);
        return card;
    }

    private LinearLayout ratingRow(String label, double rating) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, UiFactory.dp(this, 3), 0, UiFactory.dp(this, 3));

        TextView name = UiFactory.label(this, label, 13, Color.argb(220, 255, 255, 255), true);
        name.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(name, new LinearLayout.LayoutParams(UiFactory.dp(this, 86), UiFactory.dp(this, 30)));

        RatingBar stars = new RatingBar(this, null, android.R.attr.ratingBarStyleSmall);
        stars.setNumStars(5);
        stars.setStepSize(1f);
        stars.setRating((float) roundedWholeRating(rating));
        stars.setIsIndicator(true);
        row.addView(stars, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView score = UiFactory.label(this, String.valueOf(roundedWholeRating(rating)), 13, Color.WHITE, true);
        score.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        row.addView(score, new LinearLayout.LayoutParams(0, UiFactory.dp(this, 30), 1f));
        return row;
    }

    private double quietnessScore(double crowdScore) {
        return Math.max(1, Math.min(5, 6 - crowdScore));
    }

    private double roundHalf(double value) {
        return Math.round(value * 2.0) / 2.0;
    }

    private int roundedWholeRating(double value) {
        return Math.max(1, Math.min(5, (int) Math.round(value)));
    }

    private LinearLayout likeView(Review review) {
        boolean liked = review.isLikedBy(repository.getCurrentUser().id);
        LinearLayout like = new LinearLayout(this);
        like.setGravity(Gravity.CENTER);
        like.setPadding(UiFactory.dp(this, 8), 0, UiFactory.dp(this, 8), 0);
        like.setBackground(liked
                ? UiFactory.rounded(this, Color.argb(160, 253, 232, 241), 16)
                : UiFactory.roundedStroke(this, Color.argb(76, 5, 17, 25), 16, Color.argb(100, 255, 255, 255), 1));
        like.setOnClickListener(v -> {
            repository.toggleReviewLike(toiletId, review.id);
            setContentView(buildContent());
        });

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_like);
        icon.setColorFilter(liked ? UiFactory.PINK : Color.WHITE);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        like.addView(icon, new LinearLayout.LayoutParams(UiFactory.dp(this, 22), UiFactory.dp(this, 22)));

        TextView count = UiFactory.label(this, String.valueOf(review.likes), 13, liked ? UiFactory.PINK : Color.WHITE, true);
        count.setGravity(Gravity.CENTER);
        like.addView(count, new LinearLayout.LayoutParams(UiFactory.dp(this, 26), UiFactory.dp(this, 30)));
        return like;
    }

    private String genderShort(Toilet toilet) {
        if ("female".equals(toilet.gender)) {
            return "F";
        }
        if ("male".equals(toilet.gender)) {
            return "M";
        }
        return "WC";
    }

    private int genderColor(Toilet toilet) {
        if ("female".equals(toilet.gender)) {
            return UiFactory.PINK;
        }
        if ("male".equals(toilet.gender)) {
            return UiFactory.BLUE;
        }
        return UiFactory.DARK_GREEN;
    }

    private void navigateTo(Toilet toilet) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_MAP_INDEX);
        intent.putExtra(MainActivity.EXTRA_FOCUS_TOILET_ID, toilet.id);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
}
