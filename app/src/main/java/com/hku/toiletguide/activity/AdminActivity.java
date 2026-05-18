package com.hku.toiletguide.activity;

import android.app.Activity;
import android.app.AlertDialog;
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

    private ScrollView buildContent() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout page = UiFactory.page(this);
        page.setBackgroundColor(Color.WHITE);
        scrollView.addView(page);

        page.addView(backBar("Admin Console"));
        page.addView(UiFactory.subtitle(this, "Review content submissions and manage live toilet statuses."));
        page.addView(summaryRow());

        page.addView(sectionTitle("Content Moderation"));
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

        page.addView(sectionTitle("Live Status Record Flow"));
        page.addView(statusManager());
        List<LiveStatusReport> reports = repository.getAllLiveStatusReports();
        if (reports.isEmpty()) {
            page.addView(emptyState("No status reports yet."));
        } else {
            for (LiveStatusReport report : reports) {
                page.addView(statusCard(report));
            }
        }

        return scrollView;
    }

    private LinearLayout statusManager() {
        LinearLayout block = cardShell();
        block.addView(UiFactory.label(this, "Manual Toilet Status Manager", 16, UiFactory.TEXT, true));
        TextView tip = UiFactory.label(this, "Admins can directly restore normal states or set a toilet to maintenance / closed.", 13, UiFactory.MUTED, false);
        tip.setLineSpacing(UiFactory.dp(this, 2), 1f);
        block.addView(tip, blockParams(6));

        for (Toilet toilet : repository.getToilets()) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, UiFactory.dp(this, 10), 0, 0);
            row.setLayoutParams(rowParams);

            TextView name = UiFactory.label(this, toilet.building + " " + toilet.floor, 14, UiFactory.TEXT, false);
            row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(actionButton("Set status", UiFactory.DARK_GREEN, v -> showManualStatusDialog(toilet)));
            block.addView(row);
        }
        return block;
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
            if (!report.resolved) {
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
        row.addView(summaryCard("Pending content", String.valueOf(pendingContent), Color.rgb(245, 179, 53)));
        row.addView(summaryCard("Active statuses", String.valueOf(activeStatuses), UiFactory.DARK_GREEN));
        return row;
    }

    private LinearLayout summaryCard(String label, String value, int color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiFactory.rounded(this, Color.rgb(247, 249, 250), 14));
        card.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(0, 0, UiFactory.dp(this, 10), 0);
        card.setLayoutParams(params);

        card.addView(UiFactory.label(this, value, 26, color, true));
        TextView text = UiFactory.label(this, label, 13, UiFactory.MUTED, false);
        text.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(text);
        return card;
    }

    private TextView sectionTitle(String text) {
        TextView title = UiFactory.label(this, text, 20, UiFactory.TEXT, true);
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
        card.addView(UiFactory.label(this,
                submission.typeLabel() + " · " + (toilet == null ? submission.toiletId : toilet.building + " " + toilet.floor),
                16,
                UiFactory.TEXT,
                true));
        card.addView(UiFactory.label(this, submission.userName + " · " + submission.createdLabel(), 13, UiFactory.MUTED, false));

        String body;
        if (ContentSubmission.TYPE_PHOTO.equals(submission.contentType)) {
            body = (submission.title == null ? "Untitled photo" : submission.title)
                    + "\n" + (TextUtils.isEmpty(submission.body) ? "No description." : submission.body);
        } else {
            body = "Clean " + submission.cleanliness + "/5 · Crowd " + submission.crowdedness + "/5 · Overall " + submission.overall + "/5"
                    + "\n" + submission.body;
        }
        TextView content = UiFactory.label(this, body, 14, UiFactory.TEXT, false);
        content.setLineSpacing(UiFactory.dp(this, 3), 1f);
        card.addView(content, blockParams(8));

        if (!TextUtils.isEmpty(submission.imageUri)) {
            ImageView preview = moderationPreview(submission.imageUri);
            card.addView(preview, blockParams(10));
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.RIGHT);
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
                UiFactory.TEXT,
                true));
        String subtitle = (toilet == null ? report.toiletId : toilet.building + " " + toilet.floor)
                + " · by " + report.userName
                + " · " + report.createdLabel();
        card.addView(UiFactory.label(this, subtitle, 13, UiFactory.MUTED, false));

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
            actions.addView(actionButton("Mark resolved", UiFactory.DARK_GREEN, v -> resolve(report)));
            actions.addView(actionButton(normalizeLabel(report), UiFactory.BLUE, v -> normalize(report)));
            card.addView(actions, blockParams(8));
        }
        return card;
    }

    private LinearLayout emptyState(String text) {
        LinearLayout state = cardShell();
        TextView label = UiFactory.label(this, text, 15, UiFactory.MUTED, false);
        label.setGravity(Gravity.CENTER);
        state.addView(label);
        return state;
    }

    private LinearLayout cardShell() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14), UiFactory.dp(this, 14));
        card.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 14, UiFactory.LINE));
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
        button.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 14, UiFactory.LINE));
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                UiFactory.dp(this, 120),
                UiFactory.dp(this, 120)
        );
        preview.setLayoutParams(params);
        try {
            Uri uri = Uri.parse(imageUri);
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                preview.setImageBitmap(ImageDecoder.decodeBitmap(source));
            } else {
                preview.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
            }
        } catch (IOException | IllegalArgumentException error) {
            preview.setImageResource(R.drawable.ic_comment);
            preview.setColorFilter(UiFactory.MUTED);
        }
        return preview;
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

    private void moderate(ContentSubmission submission, boolean approved) {
        repository.moderateContent(submission.id, approved, approved ? "" : "Rejected in demo moderation");
        Toast.makeText(this, approved ? "Content approved" : "Content rejected", Toast.LENGTH_SHORT).show();
        setContentView(buildContent());
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

    private void showManualStatusDialog(Toilet toilet) {
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
                .setTitle(toilet.building + " " + toilet.floor)
                .setItems(labels, (dialog, which) -> {
                    repository.submitLiveStatuses(toilet.id, java.util.Collections.singletonList(codes[which]));
                    Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                    setContentView(buildContent());
                })
                .show();
    }
}
