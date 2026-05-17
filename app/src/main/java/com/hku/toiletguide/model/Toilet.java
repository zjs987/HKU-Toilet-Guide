package com.hku.toiletguide.model;

import java.util.ArrayList;
import java.util.List;

public class Toilet {
    public final String id;
    public String building;
    public String floor;
    public String gender;
    public double latitude;
    public double longitude;
    public boolean accessible;
    public boolean hasDryer;
    public boolean hasTissue;
    public boolean hasMirror;
    public int stalls;
    public double avgCleanliness;
    public double avgCrowdedness;
    public double avgOverall;
    public int totalReviews;
    public int currentCrowdLevel;
    public String openingHours;
    public String note;
    public final List<Review> reviews = new ArrayList<>();

    public Toilet(
            String id,
            String building,
            String floor,
            String gender,
            double latitude,
            double longitude,
            boolean accessible,
            boolean hasDryer,
            boolean hasTissue,
            boolean hasMirror,
            int stalls,
            double avgCleanliness,
            double avgCrowdedness,
            double avgOverall,
            int currentCrowdLevel,
            String openingHours,
            String note
    ) {
        this.id = id;
        this.building = building;
        this.floor = floor;
        this.gender = gender;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accessible = accessible;
        this.hasDryer = hasDryer;
        this.hasTissue = hasTissue;
        this.hasMirror = hasMirror;
        this.stalls = stalls;
        this.avgCleanliness = avgCleanliness;
        this.avgCrowdedness = avgCrowdedness;
        this.avgOverall = avgOverall;
        this.currentCrowdLevel = currentCrowdLevel;
        this.openingHours = openingHours;
        this.note = note;
        this.totalReviews = 0;
    }

    public String genderLabel() {
        if ("male".equals(gender)) {
            return "Male";
        }
        if ("female".equals(gender)) {
            return "Female";
        }
        return "All gender";
    }

    public String crowdLabel() {
        switch (currentCrowdLevel) {
            case 1:
                return "Empty";
            case 2:
                return "Not crowded";
            case 3:
                return "Normal";
            case 4:
                return "Crowded";
            case 5:
                return "Full";
            default:
                return "Unknown";
        }
    }

    public String facilitiesLabel() {
        List<String> labels = new ArrayList<>();
        if (accessible) {
            labels.add("Accessible");
        }
        if (hasDryer) {
            labels.add("Dryer");
        }
        if (hasTissue) {
            labels.add("Tissue");
        }
        if (hasMirror) {
            labels.add("Mirror");
        }
        return labels.isEmpty() ? "No facilities marked" : join(labels);
    }

    private String join(List<String> labels) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) {
                builder.append(" · ");
            }
            builder.append(labels.get(i));
        }
        return builder.toString();
    }
}
