package com.hku.toiletguide.model;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class Review {
    public final String id;
    public final String userId;
    public final String userName;
    public final int cleanliness;
    public final int crowdedness;
    public final int overall;
    public final String comment;
    public final long createdAt;
    public String imageUri;
    public int likes;
    private final Set<String> likedUserIds = new HashSet<>();

    public Review(String id, String userId, String userName, int cleanliness, int crowdedness, int overall, String comment, long createdAt, int likes) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.cleanliness = cleanliness;
        this.crowdedness = crowdedness;
        this.overall = overall;
        this.comment = comment;
        this.createdAt = createdAt;
        this.likes = likes;
    }

    public void markLikedBy(String userId) {
        if (likedUserIds.add(userId)) {
            likes++;
        }
    }

    public void restoreLikedBy(String userId) {
        likedUserIds.add(userId);
    }

    public boolean isLikedBy(String userId) {
        return likedUserIds.contains(userId);
    }

    public Set<String> likedUserIds() {
        return new HashSet<>(likedUserIds);
    }

    public void toggleLike(String userId) {
        if (likedUserIds.contains(userId)) {
            likedUserIds.remove(userId);
            likes = Math.max(0, likes - 1);
        } else {
            likedUserIds.add(userId);
            likes++;
        }
    }

    public String dateLabel() {
        return new SimpleDateFormat("MMM d, HH:mm", Locale.US).format(new Date(createdAt));
    }

    public String summary() {
        return userName + " · " + dateLabel()
                + "\nClean " + cleanliness + "/5"
                + " · Crowd " + crowdedness + "/5"
                + " · Overall " + overall + "/5"
                + "\n" + comment;
    }
}
