package com.hku.toiletguide.data;

import android.content.Context;

import com.hku.toiletguide.auth.LocalAuthStore;
import com.hku.toiletguide.model.ContentSubmission;
import com.hku.toiletguide.model.LiveStatusReport;
import com.hku.toiletguide.model.Review;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockToiletRepository implements ToiletRepository {
    private static MockToiletRepository instance;

    private final User studentUser = new User("user_hku_001", "HKU Student", "hku.student@connect.hku.hk", "user");
    private final User adminUser = new User("admin_hku_001", "HKU Admin", "admin@hku.hk", "admin");
    private User currentUser = studentUser;
    private LocalAuthStore authStore;

    private final List<Toilet> toilets = new ArrayList<>();
    private final Set<String> favoriteToiletIds = new HashSet<>();
    private final List<ContentSubmission> contentSubmissions = new ArrayList<>();
    private final List<LiveStatusReport> liveStatusReports = new ArrayList<>();

    public static synchronized MockToiletRepository getInstance() {
        if (instance == null) {
            instance = new MockToiletRepository();
        }
        return instance;
    }

    private MockToiletRepository() {
        seed();
    }

    public void init(Context context) {
        if (authStore != null) {
            return;
        }
        authStore = new LocalAuthStore(context);
        User savedUser = authStore.getCurrentUser();
        if (savedUser != null) {
            currentUser = savedUser;
        }
    }

    @Override
    public List<Toilet> getToilets() {
        return Collections.unmodifiableList(toilets);
    }

    @Override
    public Toilet getToiletById(String id) {
        for (Toilet toilet : toilets) {
            if (toilet.id.equals(id)) {
                return toilet;
            }
        }
        return null;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public boolean login(String email, String password) {
        if ("hku.student@connect.hku.hk".equalsIgnoreCase(email) && "student123".equals(password)) {
            currentUser = studentUser;
            persistCurrentUser(studentUser.email);
            return true;
        }
        if ("admin@hku.hk".equalsIgnoreCase(email) && "admin123".equals(password)) {
            currentUser = adminUser;
            persistCurrentUser(adminUser.email);
            return true;
        }
        if (authStore != null) {
            User localUser = authStore.authenticate(email, password);
            if (localUser != null) {
                currentUser = localUser;
                persistCurrentUser(localUser.email);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean register(String displayName, String email, String password) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return false;
        }
        if ("hku.student@connect.hku.hk".equalsIgnoreCase(email) || "admin@hku.hk".equalsIgnoreCase(email)) {
            return false;
        }
        if (authStore == null || !authStore.register(displayName.trim(), email, password)) {
            return false;
        }
        User localUser = authStore.authenticate(email, password);
        if (localUser == null) {
            return false;
        }
        currentUser = localUser;
        persistCurrentUser(localUser.email);
        return true;
    }

    @Override
    public void logout() {
        currentUser = studentUser;
        if (authStore != null) {
            authStore.clearCurrentUser();
        }
    }

    @Override
    public void addReview(String toiletId, Review review) {
        Toilet toilet = getToiletById(toiletId);
        if (toilet == null) {
            return;
        }

        int oldCount = toilet.totalReviews;
        int newCount = oldCount + 1;
        toilet.avgCleanliness = rollingAverage(toilet.avgCleanliness, oldCount, review.cleanliness);
        toilet.avgCrowdedness = rollingAverage(toilet.avgCrowdedness, oldCount, review.crowdedness);
        toilet.avgOverall = rollingAverage(toilet.avgOverall, oldCount, review.overall);
        toilet.totalReviews = newCount;
        toilet.reviews.add(0, review);
    }

    @Override
    public void submitComment(String toiletId, Review review, String imageUri) {
        ContentSubmission submission = new ContentSubmission(
                "content_comment_" + System.currentTimeMillis(),
                toiletId,
                review.userId,
                review.userName,
                ContentSubmission.TYPE_COMMENT,
                review.createdAt
        );
        submission.body = review.comment;
        submission.imageUri = imageUri;
        submission.cleanliness = review.cleanliness;
        submission.crowdedness = review.crowdedness;
        submission.overall = review.overall;
        contentSubmissions.add(0, submission);
    }

    @Override
    public List<ContentSubmission> getContentSubmissions() {
        return Collections.unmodifiableList(contentSubmissions);
    }

    @Override
    public List<ContentSubmission> getApprovedPhotos(String toiletId) {
        List<ContentSubmission> result = new ArrayList<>();
        for (ContentSubmission submission : contentSubmissions) {
            if (!ContentSubmission.TYPE_PHOTO.equals(submission.contentType)) {
                continue;
            }
            if (!submission.toiletId.equals(toiletId)) {
                continue;
            }
            if (!ContentSubmission.STATUS_APPROVED.equals(submission.reviewStatus)) {
                continue;
            }
            result.add(submission);
        }
        return result;
    }

    @Override
    public void moderateContent(String submissionId, boolean approved, String rejectionReason) {
        for (ContentSubmission submission : contentSubmissions) {
            if (!submission.id.equals(submissionId)) {
                continue;
            }

            submission.reviewStatus = approved ? ContentSubmission.STATUS_APPROVED : ContentSubmission.STATUS_REJECTED;
            submission.reviewerName = currentUser.displayName;
            submission.reviewedAt = System.currentTimeMillis();
            submission.rejectionReason = approved ? "" : rejectionReason;

            if (approved && ContentSubmission.TYPE_COMMENT.equals(submission.contentType)) {
                addReview(submission.toiletId, new Review(
                        "approved_review_" + submission.id,
                        submission.userId,
                        submission.userName,
                        submission.cleanliness,
                        submission.crowdedness,
                        submission.overall,
                        submission.body == null || submission.body.isEmpty() ? "No written comment." : submission.body,
                        submission.createdAt,
                        0
                ));
            }
            return;
        }
    }

    @Override
    public void reportCrowdLevel(String toiletId, int level) {
        Toilet toilet = getToiletById(toiletId);
        if (toilet != null) {
            toilet.currentCrowdLevel = level;
        }
    }

    @Override
    public void submitLiveStatuses(String toiletId, List<String> statusCodes) {
        if (statusCodes == null || statusCodes.isEmpty()) {
            return;
        }
        for (String statusCode : statusCodes) {
            if (!LiveStatusReport.allStatusCodes().contains(statusCode)) {
                continue;
            }
            resolveLatestActiveInGroup(toiletId, LiveStatusReport.groupFor(statusCode));
            liveStatusReports.add(0, new LiveStatusReport(
                    "status_" + statusCode + "_" + System.nanoTime(),
                    toiletId,
                    currentUser.id,
                    currentUser.displayName,
                    statusCode,
                    System.currentTimeMillis()
            ));
        }
    }

    @Override
    public List<LiveStatusReport> getLiveStatusReports(String toiletId) {
        List<LiveStatusReport> result = new ArrayList<>();
        for (LiveStatusReport report : liveStatusReports) {
            if (report.toiletId.equals(toiletId)) {
                result.add(report);
            }
        }
        result.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));
        return result;
    }

    @Override
    public List<LiveStatusReport> getAllLiveStatusReports() {
        List<LiveStatusReport> result = new ArrayList<>(liveStatusReports);
        result.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));
        return result;
    }

    @Override
    public List<LiveStatusReport> getLatestActiveStatuses(String toiletId) {
        Map<String, LiveStatusReport> latestByGroup = new LinkedHashMap<>();
        List<LiveStatusReport> reports = getLiveStatusReports(toiletId);
        for (LiveStatusReport report : reports) {
            if (report.resolved) {
                continue;
            }
            String group = report.group();
            if (!latestByGroup.containsKey(group)) {
                latestByGroup.put(group, report);
            }
        }
        return new ArrayList<>(latestByGroup.values());
    }

    @Override
    public void resolveLiveStatus(String reportId) {
        for (LiveStatusReport report : liveStatusReports) {
            if (report.id.equals(reportId)) {
                report.resolve(currentUser.displayName, System.currentTimeMillis());
                return;
            }
        }
    }

    @Override
    public void toggleReviewLike(String toiletId, String reviewId) {
        Toilet toilet = getToiletById(toiletId);
        if (toilet == null) {
            return;
        }
        for (Review review : toilet.reviews) {
            if (review.id.equals(reviewId)) {
                review.toggleLike(currentUser.id);
                return;
            }
        }
    }

    public boolean isFavorite(String toiletId) {
        return favoriteToiletIds.contains(toiletId);
    }

    public void toggleFavorite(String toiletId) {
        if (favoriteToiletIds.contains(toiletId)) {
            favoriteToiletIds.remove(toiletId);
        } else {
            favoriteToiletIds.add(toiletId);
        }
    }

    private void persistCurrentUser(String email) {
        if (authStore != null) {
            authStore.setCurrentUser(email);
        }
    }

    private void resolveLatestActiveInGroup(String toiletId, String group) {
        for (LiveStatusReport report : liveStatusReports) {
            if (!report.toiletId.equals(toiletId)) {
                continue;
            }
            if (report.resolved) {
                continue;
            }
            if (!group.equals(report.group())) {
                continue;
            }
            report.resolve(currentUser.displayName, System.currentTimeMillis());
            return;
        }
    }

    private double rollingAverage(double oldAverage, int oldCount, int newValue) {
        if (oldCount <= 0) {
            return newValue;
        }
        return ((oldAverage * oldCount) + newValue) / (oldCount + 1);
    }

    private void seed() {
        add(new Toilet("toilet_main_library_2f", "Main Library", "2F", "all",
                22.28308, 114.13745, true, true, true, true, true, 6,
                4.3, 2.2, 4.2, 2, "08:00-23:00",
                "Near the reading area. Good first demo location."));
        add(new Toilet("toilet_chi_wah_gf", "Chi Wah Learning Commons", "G/F", "all",
                22.28355, 114.13592, true, true, true, true, true, 8,
                4.1, 3.1, 4.0, 3, "07:00-23:30",
                "Busy during class breaks but easy to find."));
        add(new Toilet("toilet_centennial_1f", "Centennial Campus", "1/F", "female",
                22.28378, 114.13498, true, true, true, true, true, 5,
                4.4, 2.6, 4.3, 2, "07:00-22:00",
                "Clean and close to lecture rooms."));
        add(new Toilet("toilet_main_building_gf", "Main Building", "G/F", "male",
                22.28392, 114.13777, false, false, true, false, true, 4,
                3.7, 2.9, 3.8, 3, "08:00-20:00",
                "Historic building, slightly hidden corridor entrance."));
        add(new Toilet("toilet_library_ext_lg1", "Library Extension", "LG1", "all",
                22.28276, 114.13706, true, true, false, true, true, 3,
                3.9, 1.9, 4.1, 1, "08:00-22:00",
                "Usually quiet outside lunch time."));
        add(new Toilet("toilet_kadoorie_2f", "Kadoorie Biological Sciences Building", "2/F", "female",
                22.28452, 114.13812, false, true, true, true, true, 4,
                4.0, 2.4, 4.0, 2, "08:00-19:00",
                "Convenient for upper campus routes."));
        add(new Toilet("toilet_run_run_shaw_1f", "Run Run Shaw Building", "1/F", "male",
                22.28409, 114.13641, false, true, true, true, false, 4,
                3.8, 3.4, 3.7, 4, "08:00-21:00",
                "Often used before evening classes."));
        add(new Toilet("toilet_chong_yu_gf", "Chong Yuet Ming Amenities Centre", "G/F", "all",
                22.28236, 114.13802, true, true, true, true, true, 7,
                4.2, 2.8, 4.1, 3, "07:30-22:30",
                "Large and accessible, good backup option."));

        favoriteToiletIds.add("toilet_main_library_2f");
        favoriteToiletIds.add("toilet_library_ext_lg1");

        submitSeedLiveStatus("toilet_main_library_2f", studentUser, LiveStatusReport.STATUS_TISSUE_LOW, System.currentTimeMillis() - 5_400_000L, false);
        submitSeedLiveStatus("toilet_main_library_2f", studentUser, LiveStatusReport.STATUS_SOAP_LOW, System.currentTimeMillis() - 4_800_000L, false);
        submitSeedLiveStatus("toilet_chi_wah_gf", adminUser, LiveStatusReport.STATUS_TISSUE_OK, System.currentTimeMillis() - 1_800_000L, false);
        submitSeedLiveStatus("toilet_run_run_shaw_1f", studentUser, LiveStatusReport.STATUS_MAINTENANCE, System.currentTimeMillis() - 3_600_000L, false);

        seedContentModeration();
    }

    private void add(Toilet toilet) {
        toilet.totalReviews = 2;
        Review currentUserReview = new Review("review_" + toilet.id + "_001", studentUser.id, studentUser.displayName, 4, toilet.currentCrowdLevel, 4,
                "Clean enough and easy to find from the corridor. I would use this one again.", System.currentTimeMillis() - 86_400_000L, 6);
        Review visitorReview = new Review("review_" + toilet.id + "_002", "user_visitor_002", "HKU visitor", 5, Math.max(1, toilet.currentCrowdLevel - 1), 4,
                "Good location and the facilities match the campus guide.", System.currentTimeMillis() - 43_200_000L, 3);
        visitorReview.markLikedBy(studentUser.id);
        toilet.reviews.add(currentUserReview);
        toilet.reviews.add(visitorReview);
        toilets.add(toilet);
    }

    private void submitSeedLiveStatus(String toiletId, User user, String statusCode, long createdAt, boolean resolved) {
        LiveStatusReport report = new LiveStatusReport(
                "seed_status_" + statusCode + "_" + createdAt,
                toiletId,
                user.id,
                user.displayName,
                statusCode,
                createdAt
        );
        if (resolved) {
            report.resolve(adminUser.displayName, createdAt + 1_800_000L);
        }
        liveStatusReports.add(report);
        liveStatusReports.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));
    }

    private void seedContentModeration() {
        ContentSubmission pendingComment = new ContentSubmission(
                "seed_comment_pending",
                "toilet_chi_wah_gf",
                studentUser.id,
                studentUser.displayName,
                ContentSubmission.TYPE_COMMENT,
                System.currentTimeMillis() - 7_200_000L
        );
        pendingComment.body = "Queue moved quickly but one cubicle needed cleaning.";
        pendingComment.imageUri = "gallery://demo/comment_photo_1";
        pendingComment.cleanliness = 3;
        pendingComment.crowdedness = 4;
        pendingComment.overall = 3;
        contentSubmissions.add(pendingComment);

        ContentSubmission pendingPhoto = new ContentSubmission(
                "seed_photo_pending",
                "toilet_main_library_2f",
                studentUser.id,
                studentUser.displayName,
                ContentSubmission.TYPE_PHOTO,
                System.currentTimeMillis() - 10_800_000L
        );
        pendingPhoto.title = "Entrance photo";
        pendingPhoto.body = "Photo placeholder pending approval.";
        pendingPhoto.imageUri = "gallery://demo/photo_only_1";
        contentSubmissions.add(pendingPhoto);

        ContentSubmission approvedPhoto = new ContentSubmission(
                "seed_photo_approved",
                "toilet_chong_yu_gf",
                adminUser.id,
                adminUser.displayName,
                ContentSubmission.TYPE_PHOTO,
                System.currentTimeMillis() - 129_600_000L
        );
        approvedPhoto.title = "Wide corridor entrance";
        approvedPhoto.body = "Approved sample photo placeholder.";
        approvedPhoto.imageUri = "gallery://demo/photo_only_approved";
        approvedPhoto.reviewStatus = ContentSubmission.STATUS_APPROVED;
        approvedPhoto.reviewerName = adminUser.displayName;
        approvedPhoto.reviewedAt = System.currentTimeMillis() - 108_000_000L;
        contentSubmissions.add(approvedPhoto);

        contentSubmissions.sort(Comparator.comparingLong(a -> -a.createdAt));
    }
}
