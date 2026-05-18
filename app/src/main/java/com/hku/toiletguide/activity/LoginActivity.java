package com.hku.toiletguide.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private LinearLayout buildContent() {
        LinearLayout page = UiFactory.page(this);
        page.setBackgroundColor(Color.WHITE);
        page.setPadding(UiFactory.dp(this, 24), UiFactory.dp(this, 36), UiFactory.dp(this, 24), UiFactory.dp(this, 24));

        TextView title = UiFactory.label(this, "Sign In", 30, UiFactory.TEXT, true);
        page.addView(title);

        TextView hint = UiFactory.subtitle(this,
                "Student demo: hku.student@connect.hku.hk / student123\nAdmin demo: admin@hku.hk / admin123");
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        hintParams.setMargins(0, UiFactory.dp(this, 10), 0, UiFactory.dp(this, 20));
        page.addView(hint, hintParams);

        emailInput = input("Email");
        emailInput.setText(repository.getCurrentUser().email);
        page.addView(emailInput, inputParams());

        passwordInput = input("Password");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        page.addView(passwordInput, inputParams());

        Button loginButton = UiFactory.primaryButton(this, "Log in");
        loginButton.setOnClickListener(v -> login());
        LinearLayout.LayoutParams loginParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        );
        loginParams.setMargins(0, UiFactory.dp(this, 16), 0, 0);
        page.addView(loginButton, loginParams);

        Button skipButton = new Button(this);
        skipButton.setText("Continue as current user");
        skipButton.setAllCaps(false);
        skipButton.setTextColor(UiFactory.DARK_GREEN);
        skipButton.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 14, UiFactory.LINE));
        skipButton.setOnClickListener(v -> openMain());
        LinearLayout.LayoutParams skipParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        );
        skipParams.setMargins(0, UiFactory.dp(this, 10), 0, 0);
        page.addView(skipButton, skipParams);
        return page;
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setPadding(UiFactory.dp(this, 14), 0, UiFactory.dp(this, 14), 0);
        input.setBackground(UiFactory.roundedStroke(this, Color.WHITE, 14, UiFactory.LINE));
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
}
