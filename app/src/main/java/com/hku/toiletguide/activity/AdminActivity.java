package com.hku.toiletguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.hku.toiletguide.model.ContentSubmission;
import com.hku.toiletguide.model.LiveStatusReport;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.util.UiFactory;

import java.io.IOException;
import java.util.List;

public class AdminActivity extends Activity {
    private final MockToiletRepository repository = MockToiletRepository.getInstance();
    private static final int VIEW_PENDING_CONTENT = 0;
    private static final int VIEW_ACTIVE_STATUSES = 1;

    private int selectedSection = VIEW_PENDING_CONTENT;

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

        page.addView(backBar("Admin Dashboard"));
        page.addView(heroPanel("Use this page for pending tasks. For direct toilet changes, open a toilet from Home, Map, or Ranking."));
        page.addView(summaryRow());

        if (selectedSection == VIEW_PENDING_CONTENT) {
            page.addView(sectionTitle("Pending Content"));
            List<ContentSubmission> submissions = repository.getContentSubmissions();
            boolean hasPendingContent = false;
            for (ContentSubmission submission : submissions) {
                if (submission.isPending()) {
                    page.addView(contentCard(submission));
                    hasPendingContent = true;
                }
            }
            if (!hasPendingContent) {
                page.addView(emptyState("No pending content submissions."));
            }
        } else {
            page.addView(sectionTitle("Active Status Issues"));
            List<LiveStatusReport> reports = repository.getAllLiveStatusReports();
            boolean hasActiveStatuses = false;
            if (reports.isEmpty()) {
                page.addView(emptyState("No status reports yet."));
            } else {
                for (LiveStatusReport report : reports) {
                    if (!report.resolved && LiveStatusReport.isProblemStatus(report.statusCode)) {
                        page.addView(statusCard(report));
                        hasActiveStatuses = true;
                    }
                }
            }
            if (!hasActiveStatuses) {
                page.addView(emptyState("No active status issues."));
            }
        }

        root.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return root;
    }

    private LinearLayout summaryRow() {
        int pendingContent = 0;
        for (ContentSubmission submission : repository.getContentSubmissions()) {
            if (submission.isPending()) {
                pendingContent++;
            }
        }

        int activeStatuses = 0;
        for (LiveStatusReport report : repository.getAllLiveStatusReports()) {
            if (!report.resolved && LiveStatusReport.isProblemStatus(report.statusCode)) {
                activeStatuses++;
            }
        }

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 16), 0, UiFactory.dp(this, 8));
        row.setLayoutParams(params);
        row.addView(summaryCard("Pending content", String.valueOf(pendingContent), Color.rgb(245, 179, 53),
                selectedSection == VIEW_PENDING_CONTENT, () -> switchSection(VIEW_PENDING_CONTENT)));
        row.addView(summaryCard("Active statuses", String.valueOf(activeStatuses), Color.rgb(245, 179, 53),
                selectedSection == VIEW_ACTIVE_STATUSES, () -> switchSection(VIEW_ACTIVE_STATUSES)));
        return row;
    }

    private LinearLayout summaryCard(String label, String value, int color, boolean selected, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(selected
                ? UiFactory.roundedStroke(this, Color.argb(156, 0, 126, 111), 14, Color.argb(120, 255, 255, 255), 1)
                : UiFactory.frostedPanel(this, 14));
        card.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(0, 0, UiFactory.dp(this, 10), 0);
        card.setLayoutParams(params);
        card.setClickable(true);
        card.setOnClickListener(v -> action.run());

        card.addView(UiFactory.label(this, value, 26, color, true));
        TextView text = UiFactory.label(this, label, 13, Color.WHITE, selected);
        text.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(text);
        return card;
    }

    private TextView sectionTitle(String text) {
        TextView title = UiFactory.label(this, text, 20, Color.WHITE, true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 18), 0, UiFactory.dp(this, 10));
        title.setLayoutParams(params);
        return title;
    }

    private LinearLayout contentCard(ContentSubmission submission) {
        LinearLayout card = cardShell();
        Toilet toilet = repository.getToiletById(submission.toiletId);
        String toiletLabel = toilet == null ? submission.toiletId : toilet.building + " " + toilet.floor;
        card.addView(UiFactory.label(this,
                submission.typeLabel() + " · " + toiletLabel,
                16,
                Color.WHITE,
                true));
        card.addView(UiFactory.label(this, submission.userName + " · " + submission.createdLabel(), 13, Color.argb(220, 255, 255, 255), false));

        String body;
        if (ContentSubmission.TYPE_PHOTO.equals(submission.contentType)) {
            body = (submission.title == null ? "Untitled photo" : submission.title)
                    + "\n" + (TextUtils.isEmpty(submission.body) ? "No description." : submission.body);
        } else {
            body = "Clean " + submission.cleanliness + "/5 · Crowd " + submission.crowdedness + "/5 · Overall " + submission.overall + "/5"
                    + "\n" + submission.body;
        }
        TextView content = UiFactory.label(this, body, 14, Color.WHITE, false);
        content.setLineSpacing(UiFactory.dp(this, 3), 1f);
        card.addView(content, blockParams(8));

        if (!TextUtils.isEmpty(submission.imageUri)) {
            ImageView preview = moderationPreview(submission.imageUri);
            if (preview != null) {
                LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                        UiFactory.dp(this, 120),
                        UiFactory.dp(this, 120)
                );
                previewParams.setMargins(0, UiFactory.dp(this, 10), 0, 0);
                card.addView(preview, previewParams);
            }
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.RIGHT);
        actions.addView(actionButton("Open toilet", UiFactory.BLUE, v -> openToilet(submission.toiletId)));
        actions.addView(actionButton("Approve", UiFactory.DARK_GREEN, v -> moderate(submission, true)));
        actions.addView(actionButton("Reject", Color.rgb(220, 72, 72), v -> moderate(submission, false)));
        card.addView(actions, blockParams(10));
        return card;
    }

    private LinearLayout statusCard(LiveStatusReport report) {
        LinearLayout card = cardShell();
        Toilet toilet = repository.getToiletById(report.toiletId);
        card.addView(UiFactory.label(this,
                LiveStatusReport.groupLabel(report.group()) + " · " + LiveStatusReport.labelFor(report.statusCode),
                16,
                Color.WHITE,
                true));
        String subtitle = (toilet == null ? report.toiletId : toilet.building + " " + toilet.floor)
                + " · by " + report.userName
                + " · " + report.createdLabel();
        card.addView(UiFactory.label(this, subtitle, 13, Color.argb(220, 255, 255, 255), false));

        TextView status = UiFactory.label(this,
                report.resolved
                        ? "Resolved by " + report.resolvedByUserName + " · " + report.resolvedLabel()
                        : "Active",
                14,
                report.resolved ? UiFactory.MUTED : UiFactory.DARK_GREEN,
                true);
        card.addView(status, blockParams(8));

        if (!report.resolved) {
            LinearLayout actions = new LinearLayout(this);
            actions.setGravity(Gravity.RIGHT);
            actions.addView(actionButton("Open toilet", UiFactory.BLUE, v -> openToilet(report.toiletId)));
            actions.addView(actionButton("Mark resolved", UiFactory.DARK_GREEN, v -> resolve(report)));
            actions.addView(actionButton(normalizeLabel(report), UiFactory.BLUE, v -> normalize(report)));
            card.addView(actions, blockParams(8));
        }
        return card;
    }

    private LinearLayout emptyState(String text) {
        LinearLayout state = cardShell();
        TextView label = UiFactory.label(this, text, 15, Color.argb(220, 255, 255, 255), false);
        label.setGravity(Gravity.CENTER);
        state.addView(label);
        return state;
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

    private LinearLayout cardShell() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14));
        card.setBackground(UiFactory.frostedPanel(this, 16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 10), 0, 0);
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

    private ImageView moderationPreview(String imageUri) {
        ImageView preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable bg = UiFactory.rounded(this, Color.rgb(236, 238, 241), 14);
        preview.setBackground(bg);
        preview.setClipToOutline(true);
        preview.setOnClickListener(v -> openImagePreview(imageUri));
        try {
            Uri uri = Uri.parse(imageUri);
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                preview.setImageBitmap(ImageDecoder.decodeBitmap(source));
            } else {
                preview.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
            }
            return preview;
        } catch (IOException | IllegalArgumentException error) {
            return null;
        }
    }

    private void openImagePreview(String imageUri) {
        if (imageUri == null || imageUri.trim().isEmpty()) {
            return;
        }
        android.content.Intent intent = new android.content.Intent(this, ImagePreviewActivity.class);
        intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_URI, imageUri);
        startActivity(intent);
    }

    private LinearLayout backBar(String title) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(0, 0, 0, UiFactory.dp(this, 12));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackground(UiFactory.darkOverlayPanel(this, 24));
        bar.setPadding(UiFactory.dp(this, 12), UiFactory.dp(this, 8), UiFactory.dp(this, 12), UiFactory.dp(this, 8));

        TextView label = UiFactory.label(this, title, 24, Color.WHITE, true);
        label.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, UiFactory.dp(this, 56), 1f);
        bar.addView(label, labelParams);

        Button logout = new Button(this);
        logout.setText("Log out");
        logout.setAllCaps(false);
        logout.setTextColor(Color.WHITE);
        logout.setBackground(UiFactory.roundedStroke(this, Color.argb(86, 5, 17, 25), 16, Color.argb(100, 255, 255, 255), 1));
        logout.setOnClickListener(v -> logout());
        bar.addView(logout, new LinearLayout.LayoutParams(UiFactory.dp(this, 92), UiFactory.dp(this, 44)));

        wrapper.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return wrapper;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void logout() {
        repository.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void moderate(ContentSubmission submission, boolean approved) {
        repository.moderateContent(submission.id, approved, approved ? "" : "Rejected in demo moderation");
        Toast.makeText(this, approved ? "Content approved" : "Content rejected", Toast.LENGTH_SHORT).show();
        setContentView(buildContent());
    }

    private void openToilet(String toiletId) {
        android.content.Intent intent = new android.content.Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_TOILET_ID, toiletId);
        startActivity(intent);
    }

    private void resolve(LiveStatusReport report) {
        repository.resolveLiveStatus(report.id);
        Toast.makeText(this, "Status marked resolved", Toast.LENGTH_SHORT).show();
        setContentView(buildContent());
    }

    private void normalize(LiveStatusReport report) {
        repository.resolveLiveStatus(report.id);
        String restoreStatus = restoreStatusFor(report.statusCode);
        repository.submitLiveStatuses(report.toiletId, java.util.Collections.singletonList(restoreStatus));
        Toast.makeText(this, "Status restored to normal", Toast.LENGTH_SHORT).show();
        setContentView(buildContent());
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

    private void switchSection(int section) {
        if (selectedSection == section) {
            return;
        }
        selectedSection = section;
        setContentView(buildContent());
    }

}
