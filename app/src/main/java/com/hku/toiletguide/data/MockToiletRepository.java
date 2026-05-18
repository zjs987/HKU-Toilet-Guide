package com.hku.toiletguide.data;

import android.content.Context;

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
    private final User pickyReviewer = new User("user_picky_002", "Sensitive Nose", "reviewer002@connect.hku.hk", "user");
    private final User libraryReviewer = new User("user_library_003", "Library Regular", "reviewer003@connect.hku.hk", "user");
    private final User campusGuide = new User("user_guide_004", "Campus Guide", "guide004@connect.hku.hk", "user");
    private final User rushingStudent = new User("user_rush_005", "Rushing Student", "rush005@connect.hku.hk", "user");
    private User currentUser = studentUser;
    private ToiletGuideDatabase database;

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
        if (database != null) {
            return;
        }
        database = new ToiletGuideDatabase(context);
        database.ensureSeeded(seedUsers(), toilets, favoriteToiletIds, contentSubmissions, liveStatusReports);
        User savedUser = database.getCurrentUser();
        if (savedUser != null) {
            currentUser = savedUser;
        }
        database.loadInto(toilets, favoriteToiletIds, contentSubmissions, liveStatusReports, currentUser.id);
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
        if (database != null) {
            User user = database.authenticate(email, password);
            if (user != null) {
                currentUser = user;
                persistCurrentUser(user.email);
                database.loadInto(toilets, favoriteToiletIds, contentSubmissions, liveStatusReports, currentUser.id);
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
        if (database == null || !database.register(displayName.trim(), email, password)) {
            return false;
        }
        User localUser = database.authenticate(email, password);
        if (localUser == null) {
            return false;
        }
        currentUser = localUser;
        persistCurrentUser(localUser.email);
        database.loadInto(toilets, favoriteToiletIds, contentSubmissions, liveStatusReports, currentUser.id);
        return true;
    }

    @Override
    public void logout() {
        currentUser = studentUser;
        if (database != null) {
            database.clearCurrentUser();
            database.loadInto(toilets, favoriteToiletIds, contentSubmissions, liveStatusReports, currentUser.id);
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
        if (database != null) {
            database.saveReview(toiletId, review, toilet);
        }
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
        if (database != null) {
            database.saveSubmission(submission);
        }
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
            if (database != null) {
                database.updateSubmission(submission);
            }

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
            if (database != null) {
                database.saveCrowdLevel(toiletId, level);
            }
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
            LiveStatusReport report = new LiveStatusReport(
                    "status_" + statusCode + "_" + System.nanoTime(),
                    toiletId,
                    currentUser.id,
                    currentUser.displayName,
                    statusCode,
                    System.currentTimeMillis()
            );
            liveStatusReports.add(0, report);
            if (database != null) {
                database.saveLiveStatus(report);
            }
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
                if (database != null) {
                    database.updateLiveStatus(report);
                }
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
                if (database != null) {
                    database.setReviewLiked(review.id, currentUser.id, review.isLikedBy(currentUser.id), review.likes);
                }
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
            if (database != null) {
                database.setFavorite(currentUser.id, toiletId, false);
            }
        } else {
            favoriteToiletIds.add(toiletId);
            if (database != null) {
                database.setFavorite(currentUser.id, toiletId, true);
            }
        }
    }

    private void persistCurrentUser(String email) {
        if (database != null) {
            database.setCurrentUser(currentUser);
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
            if (database != null) {
                database.updateLiveStatus(report);
            }
            return;
        }
    }

    private List<User> seedUsers() {
        List<User> users = new ArrayList<>();
        users.add(studentUser);
        users.add(adminUser);
        users.add(pickyReviewer);
        users.add(libraryReviewer);
        users.add(campusGuide);
        users.add(rushingStudent);
        return users;
    }

    private double rollingAverage(double oldAverage, int oldCount, int newValue) {
        if (oldCount <= 0) {
            return newValue;
        }
        return ((oldAverage * oldCount) + newValue) / (oldCount + 1);
    }

    private void seed() {
        add(new Toilet("toilet_lsk_lg", "Lee Shau Kee Lecture Centre", "B1 / LG", "all",
                22.28400, 114.13483, true, true, true, true, true, 6,
                4.8, 1.8, 4.9, 2, "08:00-22:00",
                "Red-list pick from student tips: bright, no smell, touch door button and Dyson taps."));
        add(new Toilet("toilet_main_building_1f", "Main Building", "1/F", "all",
                22.28393, 114.13766, false, true, true, true, true, 4,
                4.4, 2.4, 4.6, 2, "08:00-21:00",
                "Historic building toilet with warm lighting, auto flush and good cleaning; may queue after class."));
        add(new Toilet("toilet_kk_leung_high", "KK Building / K.K. Leung Building", "High floor", "all",
                22.28358, 114.13482, true, true, true, true, true, 8,
                4.9, 1.5, 4.9, 1, "08:00-22:00",
                "Priority pick from student rankings. High floors near staff offices are quiet, new and clean; G/LG are weaker."));
        add(new Toilet("toilet_run_run_shaw_1f", "Run Run Shaw Building", "1/F and above", "all",
                22.28410, 114.13642, false, true, true, true, true, 6,
                4.6, 1.8, 4.7, 1, "08:00-21:00",
                "Top-tier combo with Library Extension. Upper floors need card access, so they stay quiet and tourist-free."));
        add(new Toilet("toilet_med_sassoon_21", "Medical Campus", "Sassoon Road 21", "all",
                22.26772, 114.12820, true, true, true, true, true, 9,
                4.8, 1.8, 4.7, 2, "08:00-21:00",
                "Medicine cluster pick: new, clean, many cubicles and several gender-neutral toilets. Far from main campus."));
        add(new Toilet("toilet_cpd_high", "CPD Teaching Building", "5/F and above", "all",
                22.28328, 114.13554, true, true, true, true, true, 5,
                4.2, 2.0, 4.2, 2, "08:00-22:00",
                "Only recommend higher floors. Cleaner, quieter and more reliable than CPD lower floors."));
        add(new Toilet("toilet_kb223", "Knowles Building", "Near KB223", "all",
                22.28358, 114.13606, false, true, true, true, true, 5,
                4.1, 2.7, 4.2, 3, "08:00-21:00",
                "Convenient near large classrooms, with frequent cleaning and automatic flush."));
        add(new Toilet("toilet_knowles_high", "Knowles Building", "High floor", "all",
                22.28361, 114.13609, false, true, true, true, true, 5,
                4.5, 1.9, 4.5, 2, "08:00-21:00",
                "High-floor Knowles is quieter and cleaner. Student notes mention the 8/F area as especially good."));
        add(new Toilet("toilet_library_extension_le1", "Library Extension", "LE1", "all",
                22.28276, 114.13705, true, true, false, true, true, 4,
                4.2, 2.5, 4.2, 3, "08:00-22:00",
                "Better than Main Library in most student notes. Reliable daily option, though peak time still has traffic."));
        add(new Toilet("toilet_main_library_4f", "Main Library", "4/F", "all",
                22.28309, 114.13744, false, true, false, true, true, 4,
                3.7, 3.0, 3.7, 3, "08:00-23:00",
                "Safer choice inside Main Library: mid-sized and usually acceptable compared with lower floors."));
        add(new Toilet("toilet_chong_yuet_ming_gf", "Chong Yuet Ming Amenities Centre", "G/F", "all",
                22.28237, 114.13802, false, true, false, true, true, 4,
                3.4, 2.6, 3.4, 2, "08:00-21:00",
                "Pink interior and small space. Cleanliness varies, but tissue is usually available."));
        add(new Toilet("toilet_cym_4f", "Chong Yuet Ming Amenities Centre", "4/F", "all",
                22.28239, 114.13804, false, true, false, true, true, 4,
                3.8, 3.0, 3.7, 3, "08:00-21:00",
                "Acceptable class-change option. Renovation is decent, but it can get busy and is not ideal when urgent."));
        add(new Toilet("toilet_meng_wah", "Meng Wah Complex", "Upper floors", "all",
                22.28355, 114.13715, false, true, false, true, true, 4,
                3.6, 2.8, 3.6, 3, "08:00-21:00",
                "No major surprise: acceptable but not special. Good enough when passing by."));
        add(new Toilet("toilet_haking_wong_female", "Haking Wong Building", "Female toilet", "female",
                22.28333, 114.13586, false, true, false, true, true, 4,
                3.7, 2.0, 3.8, 2, "08:00-21:00",
                "Student note: fewer female users means less queueing and cleaner conditions, but phone signal may be weak."));
        add(new Toilet("toilet_rayson_huang_eliot", "Rayson Huang Theatre / Eliot Hall", "Nearby", "all",
                22.28321, 114.13826, false, true, false, true, true, 5,
                4.1, 2.0, 4.1, 2, "08:00-21:00",
                "Often described as quiet and clean. A practical option when moving around the east side of campus."));
        add(new Toilet("toilet_chi_wah_2f", "Chi Wah Learning Commons", "2/F-3/F", "all",
                22.28355, 114.13593, true, true, false, true, true, 7,
                3.1, 4.2, 3.0, 4, "07:00-23:30",
                "Mixed-to-negative reports: convenient near lifts, but often busy and hygiene is unstable."));
        add(new Toilet("toilet_main_library_low", "Main Library", "3/F and below", "all",
                22.28308, 114.13746, false, false, false, true, false, 4,
                1.9, 4.4, 1.8, 5, "08:00-23:00",
                "Avoid-list item. Student notes repeatedly complain about smell and poor condition on lower floors."));
        add(new Toilet("toilet_cpd_low", "CPD Teaching Building", "Lower floors", "all",
                22.28325, 114.13552, true, false, false, true, false, 4,
                2.8, 4.0, 2.7, 4, "08:00-22:00",
                "Lower floors are busier and less stable; choose upper floors if time allows."));
        add(new Toilet("toilet_chow_yei_ching_1f", "Chow Yei Ching Building", "1/F", "all",
                22.28391, 114.13600, false, true, false, true, false, 4,
                3.2, 3.2, 3.2, 3, "08:00-21:00",
                "G/F or 1/F can work as a class-change fallback. Older style, but usually less queueing than Main Library."));
        add(new Toilet("toilet_cob_b1", "Composite Building", "B1", "all",
                22.28366, 114.13671, false, false, false, false, false, 3,
                2.0, 4.0, 2.0, 4, "08:00-20:00",
                "Avoid-list item from student notes: old facilities and poor experience. Keep as last resort."));
        add(new Toilet("toilet_hku_station", "HKU Station", "Concourse", "all",
                22.28373, 114.12888, true, true, false, true, true, 8,
                4.0, 3.1, 4.0, 3, "06:00-00:30",
                "Best before entering campus; useful backup when arriving by MTR."));
        add(new Toilet("toilet_d2_exit_station", "HKU Station D2 Exit", "Outside campus", "all",
                22.28359, 114.12805, true, true, false, true, true, 8,
                4.3, 2.2, 4.3, 2, "06:00-00:30",
                "Off-campus rescue point from student tips: good no-queue option before entering HKU."));

        favoriteToiletIds.add("toilet_lsk_lg");
        favoriteToiletIds.add("toilet_main_building_1f");
        favoriteToiletIds.add("toilet_kk_leung_high");
        favoriteToiletIds.add("toilet_run_run_shaw_1f");
        favoriteToiletIds.add("toilet_library_extension_le1");

        submitSeedLiveStatus("toilet_main_library_low", studentUser, LiveStatusReport.STATUS_TISSUE_LOW, System.currentTimeMillis() - 5_400_000L, false);
        submitSeedLiveStatus("toilet_main_library_low", studentUser, LiveStatusReport.STATUS_SOAP_LOW, System.currentTimeMillis() - 4_800_000L, false);
        submitSeedLiveStatus("toilet_chi_wah_2f", adminUser, LiveStatusReport.STATUS_TISSUE_OK, System.currentTimeMillis() - 1_800_000L, false);
        submitSeedLiveStatus("toilet_run_run_shaw_1f", studentUser, LiveStatusReport.STATUS_OPEN, System.currentTimeMillis() - 3_600_000L, false);

        seedContentModeration();
    }

    private void add(Toilet toilet) {
        seedReviews(toilet);
        recalculateScores(toilet);
        toilets.add(toilet);
    }

    private void seedReviews(Toilet toilet) {
        switch (toilet.id) {
            case "toilet_lsk_lg":
                addSeedReview(toilet, pickyReviewer, 5, 2, 5, 13, true,
                        "King-tier choice. Bright, clean, almost no smell, with a touch door button and Dyson taps. I would gladly detour here during deadline season.");
                addSeedReview(toilet, campusGuide, 5, 1, 5, 9, false,
                        "Usually no queue and the facilities feel premium. One of the safest recommendations for visitors and students.");
                break;
            case "toilet_main_building_1f":
                addSeedReview(toilet, campusGuide, 4, 2, 5, 11, true,
                        "Warm lighting, wood-tone interior and automatic flushing. It feels much newer than expected for Main Building.");
                addSeedReview(toilet, rushingStudent, 4, 3, 4, 5, false,
                        "Clean enough and worth using if you pass by, but short queues can appear right after class.");
                break;
            case "toilet_kk_leung_high":
                addSeedReview(toilet, pickyReviewer, 5, 1, 5, 16, true,
                        "High-floor KKL is the champion: new facilities, clean mirrors, low traffic and a quiet business-school feel.");
                addSeedReview(toilet, campusGuide, 5, 1, 5, 10, true,
                        "Prioritize high floors near staff offices. G and LG are much less impressive, but high floors are excellent.");
                break;
            case "toilet_run_run_shaw_1f":
                addSeedReview(toilet, libraryReviewer, 5, 1, 5, 10, true,
                        "Upper floors are calm because card access keeps tourists away. Automatic flushing and a faint disinfectant smell make it feel reliable.");
                addSeedReview(toilet, campusGuide, 4, 1, 5, 7, false,
                        "A strong quiet option near the Subway side. Good when you want to avoid the red-wall crowd.");
                break;
            case "toilet_med_sassoon_21":
                addSeedReview(toilet, campusGuide, 5, 2, 5, 8, true,
                        "Medical campus toilets are surprisingly strong: clean, many cubicles and several gender-neutral options.");
                addSeedReview(toilet, rushingStudent, 5, 2, 4, 4, false,
                        "Far from main campus, but if you are nearby this is a very dependable option.");
                break;
            case "toilet_cpd_high":
                addSeedReview(toilet, libraryReviewer, 4, 2, 4, 7, true,
                        "Only the higher floors are worth recommending. Cleaner, quieter and sometimes have Dyson-style hand dryers.");
                addSeedReview(toilet, rushingStudent, 4, 2, 4, 3, false,
                        "Skip the lower floors and go straight up if you have time. The experience improves a lot.");
                break;
            case "toilet_kb223":
                addSeedReview(toilet, campusGuide, 4, 3, 4, 6, false,
                        "Convenient near large classrooms, with frequent cleaning. Good for class changes, not the quietest.");
                addSeedReview(toilet, rushingStudent, 4, 3, 4, 4, true,
                        "Reliable when rushing between lectures. The location is better than the atmosphere.");
                break;
            case "toilet_knowles_high":
                addSeedReview(toilet, pickyReviewer, 5, 2, 5, 9, true,
                        "High-floor Knowles feels calmer and cleaner. The 8/F area gets especially good word of mouth.");
                addSeedReview(toilet, campusGuide, 4, 2, 4, 5, false,
                        "Wood-tone interior and low traffic. A good choice if you are already inside Knowles.");
                break;
            case "toilet_library_extension_le1":
                addSeedReview(toilet, libraryReviewer, 4, 3, 4, 7, true,
                        "LE is much better than Main Library lower floors. It can still get busy, but it rarely feels hopeless.");
                addSeedReview(toilet, campusGuide, 4, 2, 4, 5, false,
                        "A practical daily option around the library area. Not fancy, just dependable.");
                break;
            case "toilet_main_library_4f":
                addSeedReview(toilet, libraryReviewer, 4, 3, 4, 8, true,
                        "The 4/F toilet is the safest Main Library option. Medium-sized and the smell is usually manageable.");
                addSeedReview(toilet, pickyReviewer, 3, 3, 3, 3, false,
                        "Acceptable in an emergency, but I would still choose another building if I had time.");
                break;
            case "toilet_chong_yuet_ming_gf":
                addSeedReview(toilet, pickyReviewer, 3, 3, 3, 4, false,
                        "Pink interior and a small full-length mirror. Cute, but only three or four cubicles and cleanliness is random.");
                addSeedReview(toilet, rushingStudent, 4, 2, 3, 2, false,
                        "There is usually tissue, but the space is tight. Fine if you are nearby.");
                break;
            case "toilet_cym_4f":
                addSeedReview(toilet, campusGuide, 4, 3, 4, 4, false,
                        "The 4/F CYM option is acceptable for class changes. Renovation is decent, but it can get crowded.");
                addSeedReview(toilet, rushingStudent, 3, 3, 3, 2, false,
                        "Usable, not memorable. I would not choose it when very urgent because queues can happen.");
                break;
            case "toilet_meng_wah":
                addSeedReview(toilet, libraryReviewer, 4, 3, 4, 4, false,
                        "Meng Wah is neutral: no big surprise, no big disaster. Good enough when passing by.");
                addSeedReview(toilet, campusGuide, 3, 3, 3, 2, false,
                        "A normal campus toilet. The score depends on timing and floor.");
                break;
            case "toilet_haking_wong_female":
                addSeedReview(toilet, pickyReviewer, 4, 2, 4, 5, true,
                        "Fewer female users means fewer queues and a cleaner feeling, though phone signal can be weak.");
                addSeedReview(toilet, campusGuide, 4, 2, 4, 3, false,
                        "A useful quiet choice if you are in Haking Wong.");
                break;
            case "toilet_rayson_huang_eliot":
                addSeedReview(toilet, campusGuide, 4, 2, 4, 5, true,
                        "Quiet and clean around the east side of campus. Eliot Hall is often used quietly by people who know it.");
                addSeedReview(toilet, libraryReviewer, 4, 2, 4, 3, false,
                        "Not the most central option, but a good backup when the main routes are crowded.");
                break;
            case "toilet_chi_wah_2f":
                addSeedReview(toilet, pickyReviewer, 3, 4, 3, 4, false,
                        "Convenient near the lifts and Mac bar, but smell and hygiene are unstable. I often detour to CPD instead.");
                addSeedReview(toilet, rushingStudent, 3, 4, 3, 2, false,
                        "Many cubicles, but it is too busy too often. Use only when timing is good.");
                break;
            case "toilet_main_library_low":
                addSeedReview(toilet, pickyReviewer, 1, 5, 1, 12, true,
                        "Avoid if possible. Lower-floor Main Library toilets are repeatedly reported for strong smell and stressful conditions.");
                addSeedReview(toilet, libraryReviewer, 2, 4, 2, 5, false,
                        "Only for emergencies. Go to 4/F or leave the building if you can.");
                break;
            case "toilet_cpd_low":
                addSeedReview(toilet, campusGuide, 3, 4, 3, 3, false,
                        "Lower CPD is unstable. Sometimes fine, sometimes not flushed clean, so higher floors are strongly preferred.");
                addSeedReview(toilet, rushingStudent, 2, 4, 2, 2, false,
                        "Too much traffic. I would skip it unless I have no time.");
                break;
            case "toilet_chow_yei_ching_1f":
                addSeedReview(toilet, rushingStudent, 3, 3, 3, 3, false,
                        "Useful near the Exit A to red-wall route. Strong disinfectant smell, but acceptable in a real emergency.");
                addSeedReview(toilet, campusGuide, 3, 3, 3, 2, false,
                        "Old style but usually less hopeless than the busiest Main Library spots.");
                break;
            case "toilet_cob_b1":
                addSeedReview(toilet, pickyReviewer, 2, 4, 2, 4, false,
                        "Avoid-list item. Old facilities and a poor overall experience; keep it as a last resort.");
                addSeedReview(toilet, rushingStudent, 2, 4, 2, 1, false,
                        "Not recommended unless every other nearby option is worse.");
                break;
            case "toilet_hku_station":
                addSeedReview(toilet, campusGuide, 4, 3, 4, 5, true,
                        "Best before entering campus when arriving by MTR. Practical, accessible and easy to locate.");
                addSeedReview(toilet, rushingStudent, 4, 3, 4, 3, false,
                        "A useful backup before going uphill. Not a campus hidden gem, but very dependable.");
                break;
            case "toilet_d2_exit_station":
                addSeedReview(toilet, campusGuide, 4, 2, 4, 5, true,
                        "Off-campus rescue point near D2. Good no-queue option before entering HKU.");
                addSeedReview(toilet, rushingStudent, 4, 2, 4, 4, false,
                        "A lifesaver when you just arrived and do not want to gamble inside campus.");
                break;
            default:
                addSeedReview(toilet, campusGuide, Math.max(1, (int) Math.round(toilet.avgCleanliness)), toilet.currentCrowdLevel,
                        Math.max(1, (int) Math.round(toilet.avgOverall)), 2, false, toilet.note);
                break;
        }

        addSeedReview(toilet, studentUser,
                Math.max(1, (int) Math.round(toilet.avgCleanliness)),
                toilet.currentCrowdLevel,
                Math.max(1, (int) Math.round(toilet.avgOverall)),
                toilet.avgOverall >= 4.5 ? 6 : 2,
                toilet.avgOverall >= 4.5,
                "Saved from campus toilet tips. I am using this record as a reference and will update it after visiting in person.");
    }

    private void addSeedReview(Toilet toilet, User user, int clean, int crowd, int overall, int likes, boolean likedByStudent, String comment) {
        Review review = new Review("review_" + toilet.id + "_" + String.format("%03d", toilet.reviews.size() + 1),
                user.id,
                user.displayName,
                clampRating(clean),
                clampRating(crowd),
                clampRating(overall),
                comment,
                System.currentTimeMillis() - (long) (toilet.reviews.size() + 1) * 43_200_000L,
                likes);
        if (likedByStudent && !studentUser.id.equals(user.id)) {
            review.markLikedBy(studentUser.id);
        }
        toilet.reviews.add(review);
    }

    private int clampRating(int rating) {
        return Math.max(1, Math.min(5, rating));
    }

    private void recalculateScores(Toilet toilet) {
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
                "toilet_chi_wah_2f",
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
                "toilet_main_library_4f",
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
                "toilet_lsk_lg",
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
