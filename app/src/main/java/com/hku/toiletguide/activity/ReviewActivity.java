package com.hku.toiletguide.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.model.Review;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.util.UiFactory;

public class ReviewActivity extends Activity {
    private final MockToiletRepository repository = MockToiletRepository.getInstance();
    private String toiletId;
    private RatingBar cleanlinessRating;
    private RatingBar crowdednessRating;
    private RatingBar overallRating;
    private EditText commentInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toiletId = getIntent().getStringExtra(DetailActivity.EXTRA_TOILET_ID);
        setContentView(buildContent());
    }

    private LinearLayout buildContent() {
        Toilet toilet = repository.getToiletById(toiletId);
        LinearLayout page = UiFactory.page(this);

        page.addView(backBar("Write Review"));
        page.addView(UiFactory.subtitle(this, toilet == null ? "Unknown toilet" : toilet.building + " | " + toilet.floor));

        cleanlinessRating = addRating(page, "Cleanliness");
        crowdednessRating = addRating(page, "Crowdedness");
        overallRating = addRating(page, "Overall experience");

        commentInput = new EditText(this);
        commentInput.setHint("Leave a short comment");
        commentInput.setMinLines(4);
        commentInput.setGravity(Gravity.TOP);
        page.addView(commentInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        Button submit = UiFactory.primaryButton(this, "Submit review");
        submit.setGravity(Gravity.CENTER);
        submit.setOnClickListener(v -> submitReview());
        LinearLayout.LayoutParams submitParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        );
        submitParams.setMargins(0, UiFactory.dp(this, 16), 0, 0);
        page.addView(submit, submitParams);

        return page;
    }

    private RatingBar addRating(LinearLayout page, String label) {
        TextView textView = UiFactory.subtitle(this, label);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 36)
        );
        textParams.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        page.addView(textView, textParams);

        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1f);
        ratingBar.setRating(4f);
        page.addView(ratingBar);
        return ratingBar;
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

    private void submitReview() {
        int clean = Math.max(1, Math.round(cleanlinessRating.getRating()));
        int crowd = Math.max(1, Math.round(crowdednessRating.getRating()));
        int overall = Math.max(1, Math.round(overallRating.getRating()));
        String comment = commentInput.getText().toString().trim();
        if (comment.isEmpty()) {
            comment = "No written comment.";
        }

        Review review = new Review(
                "review_local_" + System.currentTimeMillis(),
                repository.getCurrentUser().id,
                repository.getCurrentUser().displayName,
                clean,
                crowd,
                overall,
                comment,
                System.currentTimeMillis(),
                0
        );
        repository.addReview(toiletId, review);
        Toast.makeText(this, "Review submitted", Toast.LENGTH_SHORT).show();
        finish();
    }
}
