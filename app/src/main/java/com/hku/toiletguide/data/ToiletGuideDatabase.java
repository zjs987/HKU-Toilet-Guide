package com.hku.toiletguide.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.hku.toiletguide.model.ContentSubmission;
import com.hku.toiletguide.model.LiveStatusReport;
import com.hku.toiletguide.model.Review;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

class ToiletGuideDatabase {
    private static final String PREFS = "toilet_guide_database";
    private static final String KEY_INITIALIZED = "initialized";
    private static final String KEY_CURRENT_EMAIL = "current_email";
    private static final String KEY_USERS = "users";
    private static final String KEY_SUBMISSIONS = "submissions";
    private static final String KEY_LIVE_STATUSES = "live_statuses";
    private static final String KEY_CROWD_LEVELS = "crowd_levels";
    private static final String KEY_REVIEW_PREFIX = "reviews_";
    private static final String KEY_FAVORITES_PREFIX = "favorites_";

    private final SharedPreferences preferences;

    ToiletGuideDatabase(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    void ensureSeeded(List<User> users,
                      List<Toilet> toilets,
                      Set<String> favoriteToiletIds,
                      List<ContentSubmission> submissions,
                      List<LiveStatusReport> liveStatusReports) {
        if (preferences.getBoolean(KEY_INITIALIZED, false)) {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USERS, serializeUsers(users).toString());
        if (!users.isEmpty()) {
            editor.putString(KEY_CURRENT_EMAIL, normalizeEmail(users.get(0).email));
        }
        editor.putString(KEY_SUBMISSIONS, serializeSubmissions(submissions).toString());
        editor.putString(KEY_LIVE_STATUSES, serializeLiveStatuses(liveStatusReports).toString());
        editor.putString(KEY_CROWD_LEVELS, serializeCrowdLevels(toilets).toString());
        for (Toilet toilet : toilets) {
            editor.putString(KEY_REVIEW_PREFIX + toilet.id, serializeReviews(toilet.reviews).toString());
        }
        if (!users.isEmpty()) {
            editor.putString(KEY_FAVORITES_PREFIX + users.get(0).id, serializeStringSet(favoriteToiletIds).toString());
        }
        editor.putBoolean(KEY_INITIALIZED, true);
        editor.apply();
    }

    User getCurrentUser() {
        return getUserByEmail(preferences.getString(KEY_CURRENT_EMAIL, ""));
    }

    void loadInto(List<Toilet> toilets,
                  Set<String> favoriteToiletIds,
                  List<ContentSubmission> contentSubmissions,
                  List<LiveStatusReport> liveStatusReports,
                  String currentUserId) {
        JSONObject crowdLevels = readObject(KEY_CROWD_LEVELS);
        for (Toilet toilet : toilets) {
            JSONArray reviewsJson = readArray(KEY_REVIEW_PREFIX + toilet.id);
            toilet.reviews.clear();
            toilet.reviews.addAll(deserializeReviews(reviewsJson));
            if (crowdLevels.has(toilet.id)) {
                toilet.currentCrowdLevel = crowdLevels.optInt(toilet.id, toilet.currentCrowdLevel);
            }
            recalculateToiletScores(toilet);
        }

        favoriteToiletIds.clear();
        favoriteToiletIds.addAll(deserializeStringSet(readArray(KEY_FAVORITES_PREFIX + currentUserId)));

        contentSubmissions.clear();
        contentSubmissions.addAll(deserializeSubmissions(readArray(KEY_SUBMISSIONS)));
        contentSubmissions.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));

        liveStatusReports.clear();
        liveStatusReports.addAll(deserializeLiveStatuses(readArray(KEY_LIVE_STATUSES)));
        liveStatusReports.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));
    }

    User authenticate(String email, String password) {
        JSONObject users = readObject(KEY_USERS);
        String normalizedEmail = normalizeEmail(email);
        JSONObject user = users.optJSONObject(normalizedEmail);
        if (user == null) {
            return null;
        }
        if (!password.equals(user.optString("password", ""))) {
            return null;
        }
        return userFromJson(user);
    }

    boolean register(String displayName, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty() || password == null || password.isEmpty() || displayName == null || displayName.trim().isEmpty()) {
            return false;
        }

        JSONObject users = readObject(KEY_USERS);
        if (users.has(normalizedEmail)) {
            return false;
        }

        JSONObject user = new JSONObject();
        try {
            user.put("id", "local_" + normalizedEmail.replace("@", "_at_").replace(".", "_"));
            user.put("displayName", displayName.trim());
            user.put("email", normalizedEmail);
            user.put("role", "user");
            user.put("password", password);
            users.put(normalizedEmail, user);
        } catch (JSONException ignored) {
            return false;
        }
        preferences.edit().putString(KEY_USERS, users.toString()).apply();
        return true;
    }

    void clearCurrentUser() {
        preferences.edit().remove(KEY_CURRENT_EMAIL).apply();
    }

    void saveReview(String toiletId, Review review, Toilet toilet) {
        preferences.edit()
                .putString(KEY_REVIEW_PREFIX + toiletId, serializeReviews(toilet.reviews).toString())
                .apply();
    }

    void saveSubmission(ContentSubmission submission) {
        List<ContentSubmission> submissions = deserializeSubmissions(readArray(KEY_SUBMISSIONS));
        submissions.add(0, submission);
        preferences.edit().putString(KEY_SUBMISSIONS, serializeSubmissions(submissions).toString()).apply();
    }

    void updateSubmission(ContentSubmission updatedSubmission) {
        List<ContentSubmission> submissions = deserializeSubmissions(readArray(KEY_SUBMISSIONS));
        for (int i = 0; i < submissions.size(); i++) {
            if (submissions.get(i).id.equals(updatedSubmission.id)) {
                submissions.set(i, updatedSubmission);
                break;
            }
        }
        preferences.edit().putString(KEY_SUBMISSIONS, serializeSubmissions(submissions).toString()).apply();
    }

    void saveCrowdLevel(String toiletId, int level) {
        JSONObject crowdLevels = readObject(KEY_CROWD_LEVELS);
        try {
            crowdLevels.put(toiletId, level);
        } catch (JSONException ignored) {
        }
        preferences.edit().putString(KEY_CROWD_LEVELS, crowdLevels.toString()).apply();
    }

    void saveLiveStatus(LiveStatusReport report) {
        List<LiveStatusReport> reports = deserializeLiveStatuses(readArray(KEY_LIVE_STATUSES));
        reports.add(0, report);
        preferences.edit().putString(KEY_LIVE_STATUSES, serializeLiveStatuses(reports).toString()).apply();
    }

    void updateLiveStatus(LiveStatusReport updatedReport) {
        List<LiveStatusReport> reports = deserializeLiveStatuses(readArray(KEY_LIVE_STATUSES));
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).id.equals(updatedReport.id)) {
                reports.set(i, updatedReport);
                break;
            }
        }
        preferences.edit().putString(KEY_LIVE_STATUSES, serializeLiveStatuses(reports).toString()).apply();
    }

    void setReviewLiked(String reviewId, String userId, boolean liked, int likes) {
        SharedPreferences.Editor editor = preferences.edit();
        for (String key : preferences.getAll().keySet()) {
            if (!key.startsWith(KEY_REVIEW_PREFIX)) {
                continue;
            }
            JSONArray reviewsJson = readArray(key);
            boolean changed = false;
            for (int i = 0; i < reviewsJson.length(); i++) {
                JSONObject review = reviewsJson.optJSONObject(i);
                if (review == null || !reviewId.equals(review.optString("id"))) {
                    continue;
                }
                JSONArray likedUsers = review.optJSONArray("likedUserIds");
                if (likedUsers == null) {
                    likedUsers = new JSONArray();
                }
                Set<String> likedUserIds = deserializeStringSet(likedUsers);
                if (liked) {
                    likedUserIds.add(userId);
                } else {
                    likedUserIds.remove(userId);
                }
                try {
                    review.put("likedUserIds", serializeStringSet(likedUserIds));
                    review.put("likes", likes);
                } catch (JSONException ignored) {
                }
                changed = true;
                break;
            }
            if (changed) {
                editor.putString(key, reviewsJson.toString());
                break;
            }
        }
        editor.apply();
    }

    void setFavorite(String userId, String toiletId, boolean favorite) {
        Set<String> favorites = deserializeStringSet(readArray(KEY_FAVORITES_PREFIX + userId));
        if (favorite) {
            favorites.add(toiletId);
        } else {
            favorites.remove(toiletId);
        }
        preferences.edit()
                .putString(KEY_FAVORITES_PREFIX + userId, serializeStringSet(favorites).toString())
                .apply();
    }

    void setCurrentUser(User user) {
        if (user == null) {
            return;
        }
        JSONObject users = readObject(KEY_USERS);
        String normalizedEmail = normalizeEmail(user.email);
        JSONObject existing = users.optJSONObject(normalizedEmail);
        JSONObject updated = new JSONObject();
        try {
            updated.put("id", user.id);
            updated.put("displayName", user.displayName);
            updated.put("email", normalizedEmail);
            updated.put("role", user.role);
            updated.put("password", existing == null ? defaultPasswordFor(user) : existing.optString("password", defaultPasswordFor(user)));
            users.put(normalizedEmail, updated);
        } catch (JSONException ignored) {
        }
        preferences.edit()
                .putString(KEY_USERS, users.toString())
                .putString(KEY_CURRENT_EMAIL, normalizedEmail)
                .apply();
    }

    private JSONObject serializeUsers(List<User> users) {
        JSONObject object = new JSONObject();
        for (User user : users) {
            JSONObject item = new JSONObject();
            try {
                item.put("id", user.id);
                item.put("displayName", user.displayName);
                item.put("email", normalizeEmail(user.email));
                item.put("role", user.role);
                item.put("password", defaultPasswordFor(user));
                object.put(normalizeEmail(user.email), item);
            } catch (JSONException ignored) {
            }
        }
        return object;
    }

    private JSONArray serializeReviews(List<Review> reviews) {
        JSONArray array = new JSONArray();
        for (Review review : reviews) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", review.id);
                object.put("userId", review.userId);
                object.put("userName", review.userName);
                object.put("cleanliness", review.cleanliness);
                object.put("crowdedness", review.crowdedness);
                object.put("overall", review.overall);
                object.put("comment", review.comment);
                object.put("createdAt", review.createdAt);
                object.put("likes", review.likes);
                object.put("likedUserIds", serializeStringSet(review.likedUserIds()));
                array.put(object);
            } catch (JSONException ignored) {
            }
        }
        return array;
    }

    private JSONArray serializeSubmissions(List<ContentSubmission> submissions) {
        JSONArray array = new JSONArray();
        for (ContentSubmission submission : submissions) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", submission.id);
                object.put("toiletId", submission.toiletId);
                object.put("userId", submission.userId);
                object.put("userName", submission.userName);
                object.put("contentType", submission.contentType);
                object.put("createdAt", submission.createdAt);
                object.put("title", safeString(submission.title));
                object.put("body", safeString(submission.body));
                object.put("imageUri", safeString(submission.imageUri));
                object.put("cleanliness", submission.cleanliness);
                object.put("crowdedness", submission.crowdedness);
                object.put("overall", submission.overall);
                object.put("reviewStatus", safeString(submission.reviewStatus));
                object.put("reviewerName", safeString(submission.reviewerName));
                object.put("reviewedAt", submission.reviewedAt);
                object.put("rejectionReason", safeString(submission.rejectionReason));
                array.put(object);
            } catch (JSONException ignored) {
            }
        }
        return array;
    }

    private JSONArray serializeLiveStatuses(List<LiveStatusReport> reports) {
        JSONArray array = new JSONArray();
        for (LiveStatusReport report : reports) {
            JSONObject object = new JSONObject();
            try {
                object.put("id", report.id);
                object.put("toiletId", report.toiletId);
                object.put("userId", report.userId);
                object.put("userName", report.userName);
                object.put("statusCode", report.statusCode);
                object.put("createdAt", report.createdAt);
                object.put("resolved", report.resolved);
                object.put("resolvedAt", report.resolvedAt);
                object.put("resolvedByUserName", safeString(report.resolvedByUserName));
                array.put(object);
            } catch (JSONException ignored) {
            }
        }
        return array;
    }

    private JSONObject serializeCrowdLevels(List<Toilet> toilets) {
        JSONObject object = new JSONObject();
        for (Toilet toilet : toilets) {
            try {
                object.put(toilet.id, toilet.currentCrowdLevel);
            } catch (JSONException ignored) {
            }
        }
        return object;
    }

    private JSONArray serializeStringSet(Set<String> values) {
        JSONArray array = new JSONArray();
        for (String value : values) {
            array.put(value);
        }
        return array;
    }

    private List<Review> deserializeReviews(JSONArray array) {
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) {
                continue;
            }
            Review review = new Review(
                    object.optString("id"),
                    object.optString("userId"),
                    object.optString("userName"),
                    object.optInt("cleanliness"),
                    object.optInt("crowdedness"),
                    object.optInt("overall"),
                    object.optString("comment"),
                    object.optLong("createdAt"),
                    object.optInt("likes")
            );
            Set<String> likedUsers = deserializeStringSet(object.optJSONArray("likedUserIds"));
            for (String likedUser : likedUsers) {
                review.restoreLikedBy(likedUser);
            }
            review.likes = object.optInt("likes", review.likes);
            reviews.add(review);
        }
        return reviews;
    }

    private List<ContentSubmission> deserializeSubmissions(JSONArray array) {
        List<ContentSubmission> submissions = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) {
                continue;
            }
            ContentSubmission submission = new ContentSubmission(
                    object.optString("id"),
                    object.optString("toiletId"),
                    object.optString("userId"),
                    object.optString("userName"),
                    object.optString("contentType"),
                    object.optLong("createdAt")
            );
            submission.title = object.optString("title");
            submission.body = object.optString("body");
            submission.imageUri = object.optString("imageUri");
            submission.cleanliness = object.optInt("cleanliness");
            submission.crowdedness = object.optInt("crowdedness");
            submission.overall = object.optInt("overall");
            submission.reviewStatus = object.optString("reviewStatus", ContentSubmission.STATUS_PENDING);
            submission.reviewerName = object.optString("reviewerName");
            submission.reviewedAt = object.optLong("reviewedAt");
            submission.rejectionReason = object.optString("rejectionReason");
            submissions.add(submission);
        }
        return submissions;
    }

    private List<LiveStatusReport> deserializeLiveStatuses(JSONArray array) {
        List<LiveStatusReport> reports = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) {
                continue;
            }
            LiveStatusReport report = new LiveStatusReport(
                    object.optString("id"),
                    object.optString("toiletId"),
                    object.optString("userId"),
                    object.optString("userName"),
                    object.optString("statusCode"),
                    object.optLong("createdAt")
            );
            report.resolved = object.optBoolean("resolved");
            report.resolvedAt = object.optLong("resolvedAt");
            report.resolvedByUserName = object.optString("resolvedByUserName");
            reports.add(report);
        }
        return reports;
    }

    private Set<String> deserializeStringSet(JSONArray array) {
        Set<String> values = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            values.add(array.optString(i));
        }
        return values;
    }

    private User getUserByEmail(String email) {
        JSONObject user = readObject(KEY_USERS).optJSONObject(normalizeEmail(email));
        return user == null ? null : userFromJson(user);
    }

    private User userFromJson(JSONObject object) {
        return new User(
                object.optString("id"),
                object.optString("displayName"),
                object.optString("email"),
                object.optString("role", "user")
        );
    }

    private JSONObject readObject(String key) {
        String raw = preferences.getString(key, null);
        if (raw == null || raw.trim().isEmpty()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(raw);
        } catch (JSONException ignored) {
            return new JSONObject();
        }
    }

    private JSONArray readArray(String key) {
        String raw = preferences.getString(key, null);
        if (raw == null || raw.trim().isEmpty()) {
            return new JSONArray();
        }
        try {
            return new JSONArray(raw);
        } catch (JSONException ignored) {
            return new JSONArray();
        }
    }

    private void recalculateToiletScores(Toilet toilet) {
        if (toilet.reviews.isEmpty()) {
            toilet.totalReviews = 0;
            return;
        }
        double clean = 0;
        double crowd = 0;
        double overall = 0;
        for (Review review : toilet.reviews) {
            clean += review.cleanliness;
            crowd += review.crowdedness;
            overall += review.overall;
        }
        toilet.totalReviews = toilet.reviews.size();
        toilet.avgCleanliness = clean / toilet.totalReviews;
        toilet.avgCrowdedness = crowd / toilet.totalReviews;
        toilet.avgOverall = overall / toilet.totalReviews;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }

    private String defaultPasswordFor(User user) {
        if (user == null) {
            return "";
        }
        String email = normalizeEmail(user.email);
        if ("hku.student@connect.hku.hk".equals(email)) {
            return "student123";
        }
        if ("admin@hku.hk".equals(email)) {
            return "admin123";
        }
        return "demo123";
    }
}
