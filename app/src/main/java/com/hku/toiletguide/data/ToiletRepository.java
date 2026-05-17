package com.hku.toiletguide.data;

import com.hku.toiletguide.model.Review;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.model.User;

import java.util.List;

public interface ToiletRepository {
    List<Toilet> getToilets();

    Toilet getToiletById(String id);

    User getCurrentUser();

    void addReview(String toiletId, Review review);

    void reportCrowdLevel(String toiletId, int level);

    void toggleReviewLike(String toiletId, String reviewId);
}
