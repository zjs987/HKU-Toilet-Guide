package com.hku.toiletguide.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.hku.toiletguide.model.User;

import java.util.Locale;

public class LocalAuthStore {
    private static final String PREFS = "local_auth_store";
    private static final String KEY_CURRENT_EMAIL = "current_email";
    private static final String KEY_PREFIX_NAME = "user_name_";
    private static final String KEY_PREFIX_PASSWORD = "user_password_";
    private static final String KEY_PREFIX_ROLE = "user_role_";

    private final SharedPreferences preferences;

    public LocalAuthStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean hasUser(String email) {
        return preferences.contains(passwordKey(normalizeEmail(email)));
    }

    public boolean register(String displayName, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty() || password == null || password.isEmpty() || hasUser(normalizedEmail)) {
            return false;
        }
        preferences.edit()
                .putString(nameKey(normalizedEmail), displayName)
                .putString(passwordKey(normalizedEmail), password)
                .putString(roleKey(normalizedEmail), "user")
                .apply();
        return true;
    }

    public User authenticate(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        String savedPassword = preferences.getString(passwordKey(normalizedEmail), null);
        if (savedPassword == null || !savedPassword.equals(password)) {
            return null;
        }
        return buildUser(normalizedEmail);
    }

    public User getCurrentUser() {
        String email = preferences.getString(KEY_CURRENT_EMAIL, "");
        if (email == null || email.isEmpty()) {
            return null;
        }
        return buildUser(email);
    }

    public void setCurrentUser(String email) {
        preferences.edit().putString(KEY_CURRENT_EMAIL, normalizeEmail(email)).apply();
    }

    public void clearCurrentUser() {
        preferences.edit().remove(KEY_CURRENT_EMAIL).apply();
    }

    private User buildUser(String normalizedEmail) {
        String displayName = preferences.getString(nameKey(normalizedEmail), normalizedEmail);
        String role = preferences.getString(roleKey(normalizedEmail), "user");
        return new User(
                "local_" + normalizedEmail.replace("@", "_at_").replace(".", "_"),
                displayName,
                normalizedEmail,
                role
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }

    private String nameKey(String email) {
        return KEY_PREFIX_NAME + email;
    }

    private String passwordKey(String email) {
        return KEY_PREFIX_PASSWORD + email;
    }

    private String roleKey(String email) {
        return KEY_PREFIX_ROLE + email;
    }
}
