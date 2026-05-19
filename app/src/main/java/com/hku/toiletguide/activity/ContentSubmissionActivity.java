package com.hku.toiletguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.model.Review;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.util.UiFactory;

import java.io.IOException;

public class ContentSubmissionActivity extends Activity {
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_READ_MEDIA_IMAGES = 1002;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1003;
    private static final int RATING_CLEANLINESS = 0;
    private static final int RATING_CROWDEDNESS = 1;
    private static final int RATING_OVERALL = 2;

    private final MockToiletRepository repository = MockToiletRepository.getInstance();
    private String toiletId;

    private int cleanlinessRating;
    private int crowdednessRating;
    private int overallRating;
    private EditText commentInput;
    private TextView selectedImageView;
    private ImageView previewImageView;
    private TextView removeImageView;
    private Button addImageButton;
    private LinearLayout previewRow;
    private String selectedImageUri;

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

        page.addView(backBar("Submit Review"));
        page.addView(UiFactory.subtitle(this, toilet == null ? "Unknown toilet" : toilet.building + " | " + toilet.floor));
        buildCommentForm(page);
        return scrollView;
    }

    private void buildCommentForm(LinearLayout page) {
        page.addView(sectionTitle("Rate this toilet"));
        addRating(page, "Cleanliness", RATING_CLEANLINESS);
        addRating(page, "Crowdedness", RATING_CROWDEDNESS);
        addRating(page, "Overall experience", RATING_OVERALL);

        page.addView(sectionTitle("Attach a photo"));
        previewRow = new LinearLayout(this);
        previewRow.setOrientation(LinearLayout.HORIZONTAL);
        previewRow.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(previewRow, blockParams(14));

        FrameLayout previewCard = new FrameLayout(this);
        previewCard.setBackground(UiFactory.rounded(this, Color.rgb(236, 238, 241), 18));
        previewCard.setVisibility(View.GONE);
        LinearLayout.LayoutParams previewCardParams = new LinearLayout.LayoutParams(
                UiFactory.dp(this, 72),
                UiFactory.dp(this, 72)
        );
        previewCardParams.setMargins(0, 0, UiFactory.dp(this, 12), 0);
        previewRow.addView(previewCard, previewCardParams);

        previewImageView = new ImageView(this);
        previewImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        previewImageView.setVisibility(View.GONE);
        previewImageView.setOnClickListener(v -> openImagePreview());
        previewCard.addView(previewImageView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        removeImageView = UiFactory.label(this, "x", 16, Color.WHITE, true);
        removeImageView.setGravity(Gravity.CENTER);
        removeImageView.setBackground(UiFactory.rounded(this, Color.argb(210, 30, 30, 35), 12));
        removeImageView.setVisibility(View.GONE);
        FrameLayout.LayoutParams removeParams = new FrameLayout.LayoutParams(
                UiFactory.dp(this, 24),
                UiFactory.dp(this, 24),
                Gravity.TOP | Gravity.RIGHT
        );
        removeParams.setMargins(0, UiFactory.dp(this, 6), UiFactory.dp(this, 6), 0);
        previewCard.addView(removeImageView, removeParams);
        removeImageView.setOnClickListener(v -> clearSelectedImage());

        addImageButton = new Button(this);
        addImageButton.setText("+");
        addImageButton.setTextSize(26);
        addImageButton.setAllCaps(false);
        addImageButton.setTextColor(UiFactory.MUTED);
        addImageButton.setBackground(UiFactory.rounded(this, Color.rgb(236, 238, 241), 18));
        addImageButton.setOnClickListener(v -> openGalleryWithPermission());
        LinearLayout.LayoutParams pickParams = new LinearLayout.LayoutParams(
                UiFactory.dp(this, 72),
                UiFactory.dp(this, 72)
        );
        previewRow.addView(addImageButton, pickParams);

        selectedImageView = UiFactory.label(this, "No image selected", 14, UiFactory.MUTED, false);
        page.addView(selectedImageView, blockParams(6));

        page.addView(sectionTitle("Write your comment"));
        commentInput = multilineInput("Leave a short comment");
        page.addView(commentInput, blockParams(14));

        Button submit = UiFactory.primaryButton(this, "Submit for review");
        submit.setOnClickListener(v -> submitComment());
        page.addView(submit, buttonParams());

        page.addView(UiFactory.subtitle(this, "Reviews with optional photos are reviewed by admin before they appear publicly."));
    }

    private TextView sectionTitle(String text) {
        TextView title = UiFactory.label(this, text, 18, UiFactory.TEXT, true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, 16), 0, 0);
        title.setLayoutParams(params);
        return title;
    }

    private void addRating(LinearLayout page, String label, int ratingType) {
        TextView textView = UiFactory.subtitle(this, label);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 36)
        );
        textParams.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        page.addView(textView, textParams);

        LinearLayout stars = new LinearLayout(this);
        stars.setGravity(Gravity.CENTER_VERTICAL);
        stars.setOrientation(LinearLayout.HORIZONTAL);
        updateEditableStars(stars, ratingType, 0);
        page.addView(stars, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 58)
        ));
    }

    private void updateEditableStars(LinearLayout stars, int ratingType, int selectedRating) {
        stars.removeAllViews();
        for (int i = 1; i <= 5; i++) {
            final int score = i;
            TextView star = UiFactory.label(this, "★", 42,
                    i <= selectedRating ? Color.rgb(245, 179, 53) : Color.rgb(211, 214, 218),
                    true);
            star.setGravity(Gravity.CENTER);
            star.setOnClickListener(v -> {
                setRatingValue(ratingType, score);
                updateEditableStars(stars, ratingType, score);
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    UiFactory.dp(this, 56),
                    UiFactory.dp(this, 56)
            );
            params.setMargins(0, 0, UiFactory.dp(this, 6), 0);
            stars.addView(star, params);
        }
    }

    private void setRatingValue(int ratingType, int score) {
        if (ratingType == RATING_CLEANLINESS) {
            cleanlinessRating = score;
        } else if (ratingType == RATING_CROWDEDNESS) {
            crowdednessRating = score;
        } else {
            overallRating = score;
        }
    }

    private EditText multilineInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setMinLines(4);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setGravity(Gravity.TOP);
        input.setPadding(UiFactory.dp(this, 14), UiFactory.dp(this, 12), UiFactory.dp(this, 14), UiFactory.dp(this, 12));
        input.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 14, UiFactory.LINE));
        return input;
    }

    private LinearLayout.LayoutParams blockParams(int topDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, UiFactory.dp(this, topDp), 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        );
        params.setMargins(0, UiFactory.dp(this, 16), 0, UiFactory.dp(this, 12));
        return params;
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

    private void submitComment() {
        String comment = commentInput.getText().toString().trim();
        if (comment.isEmpty()) {
            comment = "No written comment.";
        }
        Review review = new Review(
                "review_submission_" + System.currentTimeMillis(),
                repository.getCurrentUser().id,
                repository.getCurrentUser().displayName,
                cleanlinessRating,
                crowdednessRating,
                overallRating,
                comment,
                System.currentTimeMillis(),
                0
        );
        repository.submitComment(toiletId, review, selectedImageUri);
        Toast.makeText(this, "Comment submitted for moderation", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void openGalleryWithPermission() {
        String permission = android.os.Build.VERSION.SDK_INT >= 33
                ? android.Manifest.permission.READ_MEDIA_IMAGES
                : android.Manifest.permission.READ_EXTERNAL_STORAGE;
        int requestCode = android.os.Build.VERSION.SDK_INT >= 33
                ? REQUEST_READ_MEDIA_IMAGES
                : REQUEST_READ_EXTERNAL_STORAGE;
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
            return;
        }
        requestPermissions(new String[]{permission}, requestCode);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_PICK_IMAGE || resultCode != RESULT_OK || data == null) {
            return;
        }
        Uri uri = data.getData();
        if (uri == null) {
            return;
        }
        selectedImageUri = uri.toString();
        selectedImageView.setText("Image selected");
        selectedImageView.setTextColor(UiFactory.DARK_GREEN);
        showPreview(uri);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == REQUEST_READ_MEDIA_IMAGES || requestCode == REQUEST_READ_EXTERNAL_STORAGE)
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
            return;
        }
        if (requestCode == REQUEST_READ_MEDIA_IMAGES || requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            Toast.makeText(this, "Gallery permission is required to attach a photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPreview(Uri uri) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                previewImageView.setImageBitmap(ImageDecoder.decodeBitmap(source));
            } else {
                previewImageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
            }
            ((View) previewImageView.getParent()).setVisibility(View.VISIBLE);
            previewImageView.setVisibility(View.VISIBLE);
            removeImageView.setVisibility(View.VISIBLE);
        } catch (IOException error) {
            ((View) previewImageView.getParent()).setVisibility(View.GONE);
            previewImageView.setVisibility(View.GONE);
            removeImageView.setVisibility(View.GONE);
            selectedImageView.setText("Image selected, but preview failed");
            selectedImageView.setTextColor(Color.rgb(220, 72, 72));
        }
    }

    private void clearSelectedImage() {
        selectedImageUri = null;
        previewImageView.setImageDrawable(null);
        ((View) previewImageView.getParent()).setVisibility(View.GONE);
        previewImageView.setVisibility(View.GONE);
        removeImageView.setVisibility(View.GONE);
        selectedImageView.setText("No image selected");
        selectedImageView.setTextColor(UiFactory.MUTED);
    }

    private void openImagePreview() {
        if (selectedImageUri == null || selectedImageUri.trim().isEmpty()) {
            return;
        }
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_URI, selectedImageUri);
        startActivity(intent);
    }
}
