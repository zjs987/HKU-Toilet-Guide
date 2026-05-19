package com.hku.toiletguide.activity;

import android.app.Activity;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Color;
import android.os.Build;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ViewStructure;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
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
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private boolean registerMode;

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

        TextView cardTitle = UiFactory.label(this, registerMode ? "Create local account" : "Account login", 22, Color.WHITE, true);
        card.addView(cardTitle);

        TextView accountTips = UiFactory.label(this,
                registerMode
                        ? "Register a local account stored on this device. Demo accounts still work for quick testing."
                        : "Student demo: hku.student@connect.hku.hk / student123\nAdmin demo: admin@hku.hk / admin123",
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

        if (registerMode) {
            nameInput = input("Display name");
            nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            card.addView(nameInput, inputParams());
        }

        emailInput = input("Email");
        emailInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        emailInput.setText(repository.getCurrentUser().email);
        card.addView(emailInput, inputParams());

        passwordInput = new SmartPasswordInput(this);
        passwordInput.setHint("Password");
        passwordInput.setHintTextColor(Color.argb(190, 255, 255, 255));
        passwordInput.setTextColor(Color.WHITE);
        passwordInput.setSingleLine(true);
        passwordInput.setPadding(UiFactory.dp(this, 14), 0, UiFactory.dp(this, 14), 0);
        passwordInput.setBackground(UiFactory.roundedStroke(this, Color.argb(96, 5, 17, 25), 16, Color.argb(105, 255, 255, 255), 1));
        passwordInput.setGravity(Gravity.CENTER_VERTICAL);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        passwordInput.setPrivateImeOptions("nm");
        bindAutofillOnlyWhenEmpty(passwordInput);
        card.addView(passwordInput, inputParams());

        Button primaryButton = UiFactory.primaryButton(this, registerMode ? "Create account" : "Log in");
        primaryButton.setOnClickListener(v -> {
            if (registerMode) {
                register();
            } else {
                login();
            }
        });
        LinearLayout.LayoutParams loginParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        );
        loginParams.setMargins(0, UiFactory.dp(this, 18), 0, 0);
        card.addView(primaryButton, loginParams);

        Button switchModeButton = new Button(this);
        switchModeButton.setText(registerMode ? "Back to sign in" : "Create a local account");
        switchModeButton.setAllCaps(false);
        switchModeButton.setTextColor(Color.WHITE);
        switchModeButton.setBackground(UiFactory.roundedStroke(this, Color.argb(82, 5, 17, 25), 16, Color.argb(100, 255, 255, 255), 1));
        switchModeButton.setOnClickListener(v -> {
            registerMode = !registerMode;
            setContentView(buildContent());
        });
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                UiFactory.dp(this, 52)
        );
        switchParams.setMargins(0, UiFactory.dp(this, 10), 0, 0);
        card.addView(switchModeButton, switchParams);

        return root;
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(Color.argb(190, 255, 255, 255));
        input.setTextColor(Color.WHITE);
        input.setSingleLine(true);
        input.setPadding(UiFactory.dp(this, 14), 0, UiFactory.dp(this, 14), 0);
        input.setBackground(UiFactory.roundedStroke(this, Color.argb(96, 5, 17, 25), 16, Color.argb(105, 255, 255, 255), 1));
        input.setGravity(Gravity.CENTER_VERTICAL);
        return input;
    }

    private void bindAutofillOnlyWhenEmpty(EditText input) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        AutofillManager autofillManager = getSystemService(AutofillManager.class);
        input.setImportantForAutofill(input.getText().length() == 0
                ? View.IMPORTANT_FOR_AUTOFILL_YES
                : View.IMPORTANT_FOR_AUTOFILL_NO);
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                return;
            }
            input.setImportantForAutofill(input.getText().length() == 0
                    ? View.IMPORTANT_FOR_AUTOFILL_YES
                    : View.IMPORTANT_FOR_AUTOFILL_NO);
            if (input.getText().length() > 0 && autofillManager != null) {
                autofillManager.cancel();
            }
        });
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean empty = s == null || s.length() == 0;
                input.setImportantForAutofill(empty
                        ? View.IMPORTANT_FOR_AUTOFILL_YES
                        : View.IMPORTANT_FOR_AUTOFILL_NO);
                if (!empty && autofillManager != null) {
                    autofillManager.cancel();
                }
            }
        });
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

    private void register() {
        String displayName = nameInput == null ? "" : nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (displayName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in name, email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!repository.register(displayName, email, password)) {
            Toast.makeText(this, "Registration failed: email already exists or input is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        openMain();
    }

    private void openMain() {
        Class<?> targetActivity = "admin".equals(repository.getCurrentUser().role)
                ? AdminActivity.class
                : MainActivity.class;
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private static class SmartPasswordInput extends EditText {
        SmartPasswordInput(Context context) {
            super(context);
        }

        @Override
        public void onProvideAutofillStructure(ViewStructure structure, int flags) {
            super.onProvideAutofillStructure(structure, flags);
            if (getText() != null && getText().length() > 0) {
                structure.setAutofillType(AUTOFILL_TYPE_NONE);
                return;
            }
            structure.setAutofillHints(new String[]{View.AUTOFILL_HINT_PASSWORD});
        }

        @Override
        public void autofill(AutofillValue value) {
            if (getText() != null && getText().length() > 0) {
                return;
            }
            super.autofill(value);
        }

        @Override
        public void onProvideAutofillVirtualStructure(ViewStructure structure, int flags) {
            super.onProvideAutofillVirtualStructure(structure, flags);
        }

        @Override
        public void getFocusedRect(Rect r) {
            super.getFocusedRect(r);
        }
    }
}
