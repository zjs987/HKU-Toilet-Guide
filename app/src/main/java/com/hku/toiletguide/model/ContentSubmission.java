package com.hku.toiletguide.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ContentSubmission {
    public static final String TYPE_COMMENT = "comment";
    public static final String TYPE_PHOTO = "photo";

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    public final String id;
    public final String toiletId;
    public final String userId;
    public final String userName;
    public final String contentType;
    public final long createdAt;

    public String title;
    public String body;
    public String imageUri;
    public int cleanliness;
    public int crowdedness;
    public int overall;
    public String reviewStatus;
    public String reviewerName;
    public long reviewedAt;
    public String rejectionReason;

    public ContentSubmission(String id, String toiletId, String userId, String userName, String contentType, long createdAt) {
        this.id = id;
        this.toiletId = toiletId;
        this.userId = userId;
        this.userName = userName;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.reviewStatus = STATUS_PENDING;
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(reviewStatus);
    }

    public String typeLabel() {
        return TYPE_PHOTO.equals(contentType) ? "Photo" : "Comment";
    }

    public String createdLabel() {
        return new SimpleDateFormat("MMM d, HH:mm", Locale.US).format(new Date(createdAt));
    }

    public String reviewedLabel() {
        if (reviewedAt <= 0L) {
            return "";
        }
        return new SimpleDateFormat("MMM d, HH:mm", Locale.US).format(new Date(reviewedAt));
    }
}
