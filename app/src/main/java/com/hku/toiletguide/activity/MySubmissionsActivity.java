package com.hku.toiletguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.model.ContentSubmission;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.util.UiFactory;

import java.io.IOException;
import java.util.List;

public class MySubmissionsActivity extends Activity {
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

        page.addView(backBar("My Submissions"));
        List<ContentSubmission> submissions = repository.getCurrentUserSubmissions();
        page.addView(heroPanel(submissions.size() + " submissions in total",
                "Track every comment or photo and see whether it is pending, approved, or rejected."));

        if (submissions.isEmpty()) {
            page.addView(emptyState("You have not submitted any comments or photos yet."));
            root.addView(scrollView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            return root;
        }

        for (ContentSubmission submission : submissions) {
            page.addView(submissionCard(submission));
        }
        root.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return root;
    }

    private LinearLayout submissionCard(ContentSubmission submission) {
        LinearLayout card = cardShell();
        Toilet toilet = repository.getToiletById(submission.toiletId);
        String toiletLabel = toilet == null ? submission.toiletId : toilet.building + " " + toilet.floor;

        card.addView(UiFactory.label(this, submission.typeLabel() + " · " + toiletLabel, 16, Color.WHITE, true));
        card.addView(UiFactory.label(this, "Submitted " + submission.createdLabel(), 13, Color.argb(220, 255, 255, 255), false), blockParams(5));

        TextView status = UiFactory.label(this, statusLabel(submission), 13, statusColor(submission), true);
        status.setBackground(UiFactory.rounded(this, Color.argb(92, 16, 31, 44), 12));
        status.setPadding(UiFactory.dp(this, 10), UiFactory.dp(this, 6), UiFactory.dp(this, 10), UiFactory.dp(this, 6));
        card.addView(status, blockParams(10));

        if (ContentSubmission.TYPE_COMMENT.equals(submission.contentType)) {
            String ratings = "Clean " + submission.cleanliness + "/5 · Crowd " + submission.crowdedness + "/5 · Overall " + submission.overall + "/5";
            card.addView(UiFactory.label(this, ratings, 14, UiFactory.BLUE, true), blockParams(10));
            String body = TextUtils.isEmpty(submission.body) ? "No written comment." : submission.body;
            TextView comment = UiFactory.label(this, body, 14, Color.WHITE, false);
            comment.setLineSpacing(UiFactory.dp(this, 2), 1f);
            card.addView(comment, blockParams(8));
        } else {
            String title = TextUtils.isEmpty(submission.title) ? "Photo submission" : submission.title;
            card.addView(UiFactory.label(this, title, 14, Color.WHITE, true), blockParams(10));
            if (!TextUtils.isEmpty(submission.body)) {
                TextView body = UiFactory.label(this, submission.body, 14, Color.WHITE, false);
                body.setLineSpacing(UiFactory.dp(this, 2), 1f);
                card.addView(body, blockParams(8));
            }
        }

        if (!TextUtils.isEmpty(submission.imageUri)) {
            ImageView preview = previewImage(submission.imageUri);
            if (preview != null) {
                LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                        UiFactory.dp(this, 120),
                        UiFactory.dp(this, 120)
                );
                previewParams.setMargins(0, UiFactory.dp(this, 10), 0, 0);
                card.addView(preview, previewParams);
            }
        }

        if (ContentSubmission.STATUS_REJECTED.equals(submission.reviewStatus) && !TextUtils.isEmpty(submission.rejectionReason)) {
            TextView rejection = UiFactory.label(this, "Reason: " + submission.rejectionReason, 13, Color.rgb(220, 72, 72), false);
            rejection.setLineSpacing(UiFactory.dp(this, 2), 1f);
            card.addView(rejection, blockParams(10));
        }

        if (ContentSubmission.STATUS_APPROVED.equals(submission.reviewStatus) && !TextUtils.isEmpty(submission.reviewerName)) {
            card.addView(UiFactory.label(this,
                    "Approved by " + submission.reviewerName + " · " + submission.reviewedLabel(),
                    13,
                    UiFactory.DARK_GREEN,
                    false), blockParams(10));
        }

        if (ContentSubmission.STATUS_REJECTED.equals(submission.reviewStatus) && !TextUtils.isEmpty(submission.reviewerName)) {
            card.addView(UiFactory.label(this,
                    "Reviewed by " + submission.reviewerName + " · " + submission.reviewedLabel(),
                    13,
                    UiFactory.MUTED,
                    false), blockParams(10));
        }

        return card;
    }

    private String statusLabel(ContentSubmission submission) {
        if (ContentSubmission.STATUS_APPROVED.equals(submission.reviewStatus)) {
            return "Approved";
        }
        if (ContentSubmission.STATUS_REJECTED.equals(submission.reviewStatus)) {
            return "Rejected";
        }
        return "Pending review";
    }

    private int statusColor(ContentSubmission submission) {
        if (ContentSubmission.STATUS_APPROVED.equals(submission.reviewStatus)) {
            return UiFactory.DARK_GREEN;
        }
        if (ContentSubmission.STATUS_REJECTED.equals(submission.reviewStatus)) {
            return Color.rgb(220, 72, 72);
        }
        return Color.rgb(245, 179, 53);
    }

    private ImageView previewImage(String imageUri) {
        ImageView preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackground(UiFactory.rounded(this, Color.rgb(236, 238, 241), 14));
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
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_URI, imageUri);
        startActivity(intent);
    }

    private LinearLayout emptyState(String text) {
        LinearLayout state = cardShell();
        TextView label = UiFactory.label(this, text, 15, Color.argb(220, 255, 255, 255), false);
        label.setGravity(Gravity.CENTER);
        state.addView(label);
        return state;
    }

    private LinearLayout heroPanel(String title, String subtitle) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(UiFactory.dp(this, 16), UiFactory.dp(this, 16), UiFactory.dp(this, 16), UiFactory.dp(this, 16));
        panel.setBackground(UiFactory.frostedPanel(this, 18));

        TextView titleView = UiFactory.label(this, title, 22, Color.WHITE, true);
        panel.addView(titleView);

        TextView subtitleView = UiFactory.label(this, subtitle, 14, Color.argb(220, 255, 255, 255), false);
        subtitleView.setLineSpacing(UiFactory.dp(this, 3), 1f);
        panel.addView(subtitleView, blockParams(8));
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
        params.setMargins(0, UiFactory.dp(this, 12), 0, 0);
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
}
