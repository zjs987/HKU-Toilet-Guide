package com.hku.toiletguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hku.toiletguide.R;
import com.hku.toiletguide.data.MockToiletRepository;
import com.hku.toiletguide.util.UiFactory;

public class LoginActivity extends Activity {
    private final MockToiletRepository repository = MockToiletRepository.getInstance();
    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private FrameLayout buildContent() {
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        ImageView background = new ImageView(this);
        background.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int backgroundRes = getDrawableId("redwall2");
        if (backgroundRes != 0) {
            background.setImageResource(backgroundRes);
        } else {
            background.setBackgroundColor(Color.rgb(103, 51, 53));
        }
        root.addView(background);

        View overlay = new View(this);
        overlay.setBackgroundColor(Color.argb(108, 6, 14, 22));
        root.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.BOTTOM);
        page.setPadding(UiFactory.dp(this, 24), UiFactory.dp(this, 42), UiFactory.dp(this, 24), UiFactory.dp(this, 28));
        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout intro = new LinearLayout(this);
        intro.setOrientation(LinearLayout.VERTICAL);

        TextView eyebrow = UiFactory.label(this, "HKU TOILET GUIDE", 13, Color.argb(210, 255, 255, 255), false);
        eyebrow.setLetterSpacing(0.24f);
        intro.addView(eyebrow);

        TextView title = UiFactory.label(this, "Find a better toilet stop on campus", 30, Color.WHITE, true);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        intro.addView(title, titleParams);

        TextView hint = UiFactory.label(this,
                "Sign in with your saved demo account, or continue with the current user.",
                15,
                Color.argb(220, 255, 255, 255),
                false);
        hint.setLineSpacing(UiFactory.dp(this, 4), 1f);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        hintParams.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        intro.addView(hint, hintParams);
        page.addView(intro);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(UiFactory.frostedPanel(this, 28));
        card.setPadding(UiFactory.dp(this, 18), UiFactory.dp(this, 20), UiFactory.dp(this, 18), UiFactory.dp(this, 18));
        card.setElevation(UiFactory.dp(this, 8));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, UiFactory.dp(this, 28), 0, 0);
        page.addView(card, cardParams);

        TextView cardTitle = UiFactory.label(this, "Account login", 22, Color.WHITE, true);
        card.addView(cardTitle);

        TextView accountTips = UiFactory.label(this,
                "Student demo: hku.student@connect.hku.hk / student123\nAdmin demo: admin@hku.hk / admin123",
                13,
                Color.argb(220, 255, 255, 255),
                false);
        accountTips.setLineSpacing(UiFactory.dp(this, 3), 1f);
        LinearLayout.LayoutParams tipsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tipsParams.setMargins(0, UiFactory.dp(this, 8), 0, UiFactory.dp(this, 12));
        card.addView(accountTips, tipsParams);

        emailInput = input("Email");
        emailInput.setText(repository.getCurrentUser().email);
        card.addView(emailInput, inputParams());

        passwordInput = input("Password");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        card.addView(passwordInput, inputParams());

        Button loginButton = UiFactory.primaryButton(this, "Log in");
        loginButton.setOnClickListener(v -> login());
        LinearLayout.LayoutParams loginParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        );
        loginParams.setMargins(0, UiFactory.dp(this, 18), 0, 0);
        card.addView(loginButton, loginParams);

        Button skipButton = new Button(this);
        skipButton.setText("Continue as current user");
        skipButton.setAllCaps(false);
        skipButton.setTextColor(Color.WHITE);
        skipButton.setBackground(UiFactory.roundedStroke(this, Color.argb(40, 255, 255, 255), 16, Color.argb(120, 255, 255, 255), 1));
        skipButton.setOnClickListener(v -> openMain());
        LinearLayout.LayoutParams skipParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        );
        skipParams.setMargins(0, UiFactory.dp(this, 10), 0, 0);
        card.addView(skipButton, skipParams);
        return root;
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(Color.argb(190, 255, 255, 255));
        input.setTextColor(Color.WHITE);
        input.setSingleLine(true);
        input.setPadding(UiFactory.dp(this, 14), 0, UiFactory.dp(this, 14), 0);
        input.setBackground(UiFactory.roundedStroke(this, Color.argb(45, 255, 255, 255), 16, Color.argb(115, 255, 255, 255), 1));
        input.setGravity(Gravity.CENTER_VERTICAL);
        return input;
    }

    private LinearLayout.LayoutParams inputParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        );
        params.setMargins(0, UiFactory.dp(this, 12), 0, 0);
        return params;
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (!repository.login(email, password)) {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            return;
        }
        openMain();
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
}
