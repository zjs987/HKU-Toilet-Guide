package com.hku.toiletguide.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class DetailActivity extends Activity {
    public static final String EXTRA_TOILET_ID = "toilet_id";

    private final MockToiletRepository repository = MockToiletRepository.getInstance();
    private String toiletId;
    private int selectedTab = 0;

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

    private ScrollView buildContent() {
        Toilet toilet = repository.getToiletById(toiletId);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(UiFactory.dp(this, 22), UiFactory.dp(this, 26), UiFactory.dp(this, 22), UiFactory.dp(this, 28));
        page.setBackgroundColor(Color.WHITE);
        scrollView.addView(page);

        if (toilet == null) {
            page.addView(UiFactory.title(this, "Toilet not found"));
            page.addView(UiFactory.subtitle(this, "Return to the map and choose another point."));
            return scrollView;
        }

        page.addView(buildTopBar());
        page.addView(buildHeader(toilet));
        page.addView(buildTabs());
        if (selectedTab == 0) {
            page.addView(buildDescription(toilet));
        } else {
            page.addView(buildRatingSection(toilet));
        }
        return scrollView;
    }

    private LinearLayout buildTopBar() {
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        ImageView backIcon = new ImageView(this);
        backIcon.setImageResource(R.drawable.ic_back);
        backIcon.setColorFilter(UiFactory.TEXT);
        backIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        backIcon.setPadding(UiFactory.dp(this, 8), UiFactory.dp(this, 8), UiFactory.dp(this, 8), UiFactory.dp(this, 8));
        backIcon.setOnClickListener(v -> finish());
        top.addView(backIcon, new LinearLayout.LayoutParams(UiFactory.dp(this, 44), UiFactory.dp(this, 44)));

        TextView back = UiFactory.label(this, "Back", 17, UiFactory.TEXT, true);
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
        TextView view = UiFactory.label(this, text, 12, UiFactory.TEXT, true);
        view.setGravity(Gravity.CENTER);
        view.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 18, Color.rgb(232, 235, 238)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UiFactory.dp(this, 54), UiFactory.dp(this, 38));
        params.setMargins(UiFactory.dp(this, 6), 0, 0, 0);
        view.setLayoutParams(params);
        return view;
    }

    private LinearLayout buildHeader(Toilet toilet) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, UiFactory.dp(this, 22), 0, UiFactory.dp(this, 22));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView genderBadge = UiFactory.label(this, genderShort(toilet), 17, Color.WHITE, true);
        genderBadge.setGravity(Gravity.CENTER);
        genderBadge.setBackground(UiFactory.rounded(this, genderColor(toilet), 28));
        titleRow.addView(genderBadge, new LinearLayout.LayoutParams(UiFactory.dp(this, 58), UiFactory.dp(this, 58)));

        TextView title = UiFactory.label(this, toilet.building + " " + toilet.floor, 31, UiFactory.TEXT, true);
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
                UiFactory.MUTED,
                false);
        locationRow.addView(address, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button navigate = UiFactory.primaryButton(this, "Start");
        navigate.setTypeface(Typeface.DEFAULT_BOLD);
        navigate.setOnClickListener(v -> navigateTo(toilet));
        locationRow.addView(navigate, new LinearLayout.LayoutParams(UiFactory.dp(this, 104), UiFactory.dp(this, 46)));
        return header;
    }

    private ImageView favoriteButton(Toilet toilet) {
        boolean favorite = repository.isFavorite(toilet.id);
        ImageView favoriteIcon = new ImageView(this);
        favoriteIcon.setImageResource(R.drawable.ic_favorite);
        favoriteIcon.setColorFilter(favorite ? UiFactory.PINK : UiFactory.MUTED);
        favoriteIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        favoriteIcon.setPadding(UiFactory.dp(this, 9), UiFactory.dp(this, 9), UiFactory.dp(this, 9), UiFactory.dp(this, 9));
        favoriteIcon.setBackground(favorite
                ? UiFactory.rounded(this, Color.rgb(253, 232, 241), 23)
                : UiFactory.roundedStroke(this, Color.WHITE, 23, Color.rgb(226, 230, 233)));
        favoriteIcon.setOnClickListener(v -> {
            repository.toggleFavorite(toilet.id);
            Toast.makeText(this, repository.isFavorite(toilet.id) ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
            setContentView(buildContent());
        });
        return favoriteIcon;
    }

    private LinearLayout facilityTags(Toilet toilet) {
        LinearLayout tags = new LinearLayout(this);
        tags.setOrientation(LinearLayout.HORIZONTAL);
        tags.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(UiFactory.dp(this, 72), UiFactory.dp(this, 10), 0, 0);
        tags.setLayoutParams(params);

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
        return tags;
    }

    private TextView tag(String text) {
        TextView tag = UiFactory.label(this, text, 12, UiFactory.DARK_GREEN, true);
        tag.setGravity(Gravity.CENTER);
        tag.setBackground(UiFactory.rounded(this, Color.rgb(232, 248, 245), 14));
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
        tabs.setPadding(0, UiFactory.dp(this, 10), 0, UiFactory.dp(this, 10));

        tabs.addView(tab("Overview", selectedTab == 0, 0));
        tabs.addView(tab("Reviews", selectedTab == 1, 1));
        return tabs;
    }

    private TextView tab(String text, boolean active, int tabIndex) {
        TextView view = UiFactory.label(this, text, 17, active ? UiFactory.DARK_GREEN : Color.rgb(158, 160, 166), true);
        view.setGravity(Gravity.CENTER);
        view.setBackground(active
                ? UiFactory.rounded(this, Color.rgb(232, 248, 245), 20)
                : UiFactory.rounded(this, Color.WHITE, 20));
        view.setOnClickListener(v -> {
            selectedTab = tabIndex;
            setContentView(buildContent());
        });
        view.setLayoutParams(new LinearLayout.LayoutParams(0, UiFactory.dp(this, 46), 1f));
        return view;
    }

    private LinearLayout buildDescription(Toilet toilet) {
        LinearLayout section = section("Overview");

        TextView note = UiFactory.label(this, toilet.note, 17, UiFactory.TEXT, false);
        note.setLineSpacing(UiFactory.dp(this, 5), 1f);
        section.addView(note);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams statsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statsParams.setMargins(0, UiFactory.dp(this, 16), 0, 0);
        section.addView(stats, statsParams);

        stats.addView(crowdStatCard(toilet));
        stats.addView(statCard("Open", toilet.openingHours, Color.rgb(74, 134, 230)));
        stats.addView(statCard("Rating", String.format("%.1f", toilet.avgOverall), Color.rgb(245, 179, 53)));
        section.addView(liveStatusSection(toilet));
        section.addView(reportActions(toilet));
        return section;
    }

    private LinearLayout buildRatingSection(Toilet toilet) {
        LinearLayout section = section("Ratings and Reviews");

        if (toilet.reviews.isEmpty()) {
            TextView empty = UiFactory.label(this, "No reviews yet. Be the first to leave a review.", 16, UiFactory.MUTED, false);
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
        block.setBackground(UiFactory.rounded(this, Color.rgb(247, 249, 250), 12));
        block.setPadding(UiFactory.dp(this, 12), UiFactory.dp(this, 12), UiFactory.dp(this, 12), UiFactory.dp(this, 12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 16), 0, 0);
        block.setLayoutParams(params);

        block.addView(UiFactory.label(this, "Current Live Status", 17, UiFactory.TEXT, true));
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

        TextView caption = UiFactory.label(this, "Latest active reports", 13, UiFactory.MUTED, true);
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
                    UiFactory.TEXT,
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
        row.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 14), 0, 0);
        row.setLayoutParams(params);
        Button status = UiFactory.primaryButton(this, "Report status");
        status.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatusReportActivity.class);
            intent.putExtra(EXTRA_TOILET_ID, toilet.id);
            startActivity(intent);
        });
        row.addView(status, new LinearLayout.LayoutParams(
                UiFactory.dp(this, 148),
                UiFactory.dp(this, 48)
        ));
        return row;
    }

    private LinearLayout section(String title) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(UiFactory.dp(this, 16), UiFactory.dp(this, 16), UiFactory.dp(this, 16), UiFactory.dp(this, 16));
        section.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 12, Color.rgb(226, 230, 233)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 14), 0, 0);
        section.setLayoutParams(params);

        TextView heading = UiFactory.label(this, title, 19, UiFactory.DARK_GREEN, true);
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headingParams.setMargins(0, 0, 0, UiFactory.dp(this, 12));
        section.addView(heading, headingParams);
        return section;
    }

    private LinearLayout statCard(String label, String value, int color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(UiFactory.dp(this, 8), UiFactory.dp(this, 10), UiFactory.dp(this, 8), UiFactory.dp(this, 10));
        card.setBackground(UiFactory.rounded(this, Color.rgb(247, 249, 250), 10));

        TextView valueView = UiFactory.label(this, value, 14, color, true);
        valueView.setGravity(Gravity.CENTER);
        card.addView(valueView);
        TextView labelView = UiFactory.label(this, label, 11, UiFactory.MUTED, false);
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
        card.setBackground(UiFactory.rounded(this, Color.rgb(247, 249, 250), 10));

        LinearLayout people = crowdPeople(toilet.currentCrowdLevel, UiFactory.dp(this, 16));
        people.setGravity(Gravity.CENTER);
        card.addView(people);

        TextView labelView = UiFactory.label(this, toilet.crowdLabel(), 11, UiFactory.MUTED, false);
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
            person.setColorFilter(i <= safeLevel ? crowdColor(safeLevel) : Color.rgb(210, 215, 220));
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
        card.setBackground(UiFactory.rounded(this, Color.rgb(247, 249, 250), 10));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        card.setLayoutParams(params);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(UiFactory.label(this, review.userName + " | " + review.dateLabel(), 14, UiFactory.MUTED, false),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(likeView(review));
        card.addView(header);
        card.addView(UiFactory.label(this,
                "Clean " + review.cleanliness + "/5 | Crowd " + review.crowdedness + "/5 | Overall " + review.overall + "/5",
                15,
                UiFactory.TEXT,
                true));
        TextView comment = UiFactory.label(this, review.comment, 15, UiFactory.TEXT, false);
        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        commentParams.setMargins(0, UiFactory.dp(this, 6), 0, 0);
        card.addView(comment, commentParams);
        return card;
    }

    private LinearLayout likeView(Review review) {
        boolean liked = review.isLikedBy(repository.getCurrentUser().id);
        LinearLayout like = new LinearLayout(this);
        like.setGravity(Gravity.CENTER);
        like.setPadding(UiFactory.dp(this, 8), 0, UiFactory.dp(this, 8), 0);
        like.setBackground(liked
                ? UiFactory.rounded(this, Color.rgb(253, 232, 241), 16)
                : UiFactory.roundedStroke(this, Color.WHITE, 16, Color.rgb(226, 230, 233)));
        like.setOnClickListener(v -> {
            repository.toggleReviewLike(toiletId, review.id);
            setContentView(buildContent());
        });

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_like);
        icon.setColorFilter(liked ? UiFactory.PINK : UiFactory.MUTED);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        like.addView(icon, new LinearLayout.LayoutParams(UiFactory.dp(this, 22), UiFactory.dp(this, 22)));

        TextView count = UiFactory.label(this, String.valueOf(review.likes), 13, liked ? UiFactory.PINK : UiFactory.MUTED, true);
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

    private void showCrowdDialog(Toilet toilet) {
        String[] labels = {"Empty", "Not crowded", "Normal", "Crowded", "Full"};
        new AlertDialog.Builder(this)
                .setTitle("Current crowd level")
                .setItems(labels, (dialog, which) -> {
                    repository.reportCrowdLevel(toilet.id, which + 1);
                    Toast.makeText(this, "Crowd report saved", Toast.LENGTH_SHORT).show();
                    setContentView(buildContent());
                })
                .show();
    }

    private void navigateTo(Toilet toilet) {
        Uri uri = Uri.parse("google.navigation:q=" + toilet.latitude + "," + toilet.longitude + "&mode=w");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException error) {
            Intent fallback = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + toilet.latitude + "," + toilet.longitude));
            startActivity(fallback);
        }
    }
}
