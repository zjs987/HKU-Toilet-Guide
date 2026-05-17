package com.hku.toiletguide.data;

import com.hku.toiletguide.model.Review;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockToiletRepository implements ToiletRepository {
    private static MockToiletRepository instance;
    private final User currentUser = new User("user_hku_001", "HKU Student", "hku.student@connect.hku.hk", "user");
    private final List<Toilet> toilets = new ArrayList<>();
    private final Set<String> favoriteToiletIds = new HashSet<>();

    public static synchronized MockToiletRepository getInstance() {
        if (instance == null) {
            instance = new MockToiletRepository();
        }
        return instance;
    }

    private MockToiletRepository() {
        seed();
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
    public void reportCrowdLevel(String toiletId, int level) {
        Toilet toilet = getToiletById(toiletId);
        if (toilet != null) {
            toilet.currentCrowdLevel = level;
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

    private double rollingAverage(double oldAverage, int oldCount, int newValue) {
        if (oldCount <= 0) {
            return newValue;
        }
        return ((oldAverage * oldCount) + newValue) / (oldCount + 1);
    }

    private void seed() {
        add(new Toilet("toilet_main_library_2f", "Main Library", "2F", "all",
                22.28308, 114.13745, true, true, true, true, 6,
                4.3, 2.2, 4.2, 2, "08:00-23:00",
                "Near the reading area. Good first demo location."));
        add(new Toilet("toilet_chi_wah_gf", "Chi Wah Learning Commons", "G/F", "all",
                22.28355, 114.13592, true, true, true, true, 8,
                4.1, 3.1, 4.0, 3, "07:00-23:30",
                "Busy during class breaks but easy to find."));
        add(new Toilet("toilet_centennial_1f", "Centennial Campus", "1/F", "female",
                22.28378, 114.13498, true, true, true, true, 5,
                4.4, 2.6, 4.3, 2, "07:00-22:00",
                "Clean and close to lecture rooms."));
        add(new Toilet("toilet_main_building_gf", "Main Building", "G/F", "male",
                22.28392, 114.13777, false, true, false, true, 4,
                3.7, 2.9, 3.8, 3, "08:00-20:00",
                "Historic building, slightly hidden corridor entrance."));
        add(new Toilet("toilet_library_ext_lg1", "Library Extension", "LG1", "all",
                22.28276, 114.13706, true, false, true, true, 3,
                3.9, 1.9, 4.1, 1, "08:00-22:00",
                "Usually quiet outside lunch time."));
        add(new Toilet("toilet_kadoorie_2f", "Kadoorie Biological Sciences Building", "2/F", "female",
                22.28452, 114.13812, false, true, true, true, 4,
                4.0, 2.4, 4.0, 2, "08:00-19:00",
                "Convenient for upper campus routes."));
        add(new Toilet("toilet_run_run_shaw_1f", "Run Run Shaw Building", "1/F", "male",
                22.28409, 114.13641, false, true, true, false, 4,
                3.8, 3.4, 3.7, 4, "08:00-21:00",
                "Often used before evening classes."));
        add(new Toilet("toilet_chong_yu_gf", "Chong Yuet Ming Amenities Centre", "G/F", "all",
                22.28236, 114.13802, true, true, true, true, 7,
                4.2, 2.8, 4.1, 3, "07:30-22:30",
                "Large and accessible, good backup option."));
        favoriteToiletIds.add("toilet_main_library_2f");
        favoriteToiletIds.add("toilet_library_ext_lg1");
    }

    private void add(Toilet toilet) {
        toilet.totalReviews = 2;
        Review currentUserReview = new Review("review_" + toilet.id + "_001", currentUser.id, currentUser.displayName, 4, toilet.currentCrowdLevel, 4,
                "Clean enough and easy to find from the corridor. I would use this one again.", System.currentTimeMillis() - 86_400_000L, 6);
        Review visitorReview = new Review("review_" + toilet.id + "_002", "user_visitor_002", "HKU visitor", 5, Math.max(1, toilet.currentCrowdLevel - 1), 4,
                "Good location and the facilities match the campus guide.", System.currentTimeMillis() - 43_200_000L, 3);
        visitorReview.markLikedBy(currentUser.id);
        toilet.reviews.add(currentUserReview);
        toilet.reviews.add(visitorReview);
        toilets.add(toilet);
    }
}
