package com.hku.toiletguide.data;

import com.hku.toiletguide.model.Review;
import com.hku.toiletguide.model.ContentSubmission;
import com.hku.toiletguide.model.LiveStatusReport;
import com.hku.toiletguide.model.Toilet;
import com.hku.toiletguide.model.User;

import java.util.List;

public interface ToiletRepository {
    List<Toilet> getToilets();

    Toilet getToiletById(String id);

    User getCurrentUser();

    boolean login(String email, String password);

    void logout();

    void addReview(String toiletId, Review review);

    void submitComment(String toiletId, Review review, String imageUri);

    List<ContentSubmission> getContentSubmissions();

    List<ContentSubmission> getApprovedPhotos(String toiletId);

    void moderateContent(String submissionId, boolean approved, String rejectionReason);

    void reportCrowdLevel(String toiletId, int level);

    void submitLiveStatuses(String toiletId, List<String> statusCodes);

    List<LiveStatusReport> getLiveStatusReports(String toiletId);

    List<LiveStatusReport> getAllLiveStatusReports();

    List<LiveStatusReport> getLatestActiveStatuses(String toiletId);

    void resolveLiveStatus(String reportId);

    void toggleReviewLike(String toiletId, String reviewId);
}
