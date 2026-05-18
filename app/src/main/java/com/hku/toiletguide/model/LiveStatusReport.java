package com.hku.toiletguide.model;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LiveStatusReport {
    public static final String GROUP_TISSUE = "tissue";
    public static final String GROUP_SOAP = "soap";
    public static final String GROUP_DRYER = "dryer";
    public static final String GROUP_OPERATION = "operation";

    public static final String STATUS_TISSUE_LOW = "tissue_low";
    public static final String STATUS_TISSUE_OK = "tissue_ok";
    public static final String STATUS_SOAP_LOW = "soap_low";
    public static final String STATUS_SOAP_OK = "soap_ok";
    public static final String STATUS_DRYER_BROKEN = "dryer_broken";
    public static final String STATUS_DRYER_OK = "dryer_ok";
    public static final String STATUS_MAINTENANCE = "maintenance";
    public static final String STATUS_CLOSED_TEMPORARILY = "closed_temporarily";
    public static final String STATUS_OPEN = "open";

    public static final class StatusOption {
        public final String code;
        public final String group;
        public final String title;
        public final String shortLabel;
        public final String longLabel;
        public final boolean normal;
        public final int textColor;
        public final int fillColor;

        public StatusOption(String code, String group, String title, String shortLabel, String longLabel, boolean normal, int textColor, int fillColor) {
            this.code = code;
            this.group = group;
            this.title = title;
            this.shortLabel = shortLabel;
            this.longLabel = longLabel;
            this.normal = normal;
            this.textColor = textColor;
            this.fillColor = fillColor;
        }
    }

    private static final Map<String, StatusOption> STATUS_MAP = buildStatusMap();
    private static final List<String> USER_REPORTABLE_CODES = Collections.unmodifiableList(Arrays.asList(
            STATUS_TISSUE_LOW,
            STATUS_SOAP_LOW,
            STATUS_DRYER_BROKEN,
            STATUS_MAINTENANCE,
            STATUS_CLOSED_TEMPORARILY
    ));

    public final String id;
    public final String toiletId;
    public final String userId;
    public final String userName;
    public final String statusCode;
    public final long createdAt;
    public boolean resolved;
    public long resolvedAt;
    public String resolvedByUserName;

    public LiveStatusReport(String id, String toiletId, String userId, String userName, String statusCode, long createdAt) {
        this.id = id;
        this.toiletId = toiletId;
        this.userId = userId;
        this.userName = userName;
        this.statusCode = statusCode;
        this.createdAt = createdAt;
    }

    private static Map<String, StatusOption> buildStatusMap() {
        LinkedHashMap<String, StatusOption> map = new LinkedHashMap<>();
        map.put(STATUS_TISSUE_LOW, new StatusOption(
                STATUS_TISSUE_LOW, GROUP_TISSUE, "Tissue", "LOW", "Tissue low", false,
                Color.WHITE, Color.rgb(220, 72, 72)
        ));
        map.put(STATUS_TISSUE_OK, new StatusOption(
                STATUS_TISSUE_OK, GROUP_TISSUE, "Tissue", "OK", "Tissue sufficient", true,
                Color.WHITE, Color.rgb(43, 170, 94)
        ));
        map.put(STATUS_SOAP_LOW, new StatusOption(
                STATUS_SOAP_LOW, GROUP_SOAP, "Soap", "LOW", "Soap low", false,
                Color.WHITE, Color.rgb(220, 72, 72)
        ));
        map.put(STATUS_SOAP_OK, new StatusOption(
                STATUS_SOAP_OK, GROUP_SOAP, "Soap", "OK", "Soap sufficient", true,
                Color.WHITE, Color.rgb(43, 170, 94)
        ));
        map.put(STATUS_DRYER_BROKEN, new StatusOption(
                STATUS_DRYER_BROKEN, GROUP_DRYER, "Dryer", "BROKEN", "Dryer broken", false,
                Color.WHITE, Color.rgb(220, 72, 72)
        ));
        map.put(STATUS_DRYER_OK, new StatusOption(
                STATUS_DRYER_OK, GROUP_DRYER, "Dryer", "OK", "Dryer working", true,
                Color.WHITE, Color.rgb(43, 170, 94)
        ));
        map.put(STATUS_MAINTENANCE, new StatusOption(
                STATUS_MAINTENANCE, GROUP_OPERATION, "Operation", "MAINT", "Under maintenance", false,
                Color.WHITE, Color.rgb(220, 72, 72)
        ));
        map.put(STATUS_CLOSED_TEMPORARILY, new StatusOption(
                STATUS_CLOSED_TEMPORARILY, GROUP_OPERATION, "Operation", "CLOSED", "Temporarily closed", false,
                Color.WHITE, Color.rgb(220, 72, 72)
        ));
        map.put(STATUS_OPEN, new StatusOption(
                STATUS_OPEN, GROUP_OPERATION, "Operation", "OPEN", "Open", true,
                Color.WHITE, Color.rgb(43, 170, 94)
        ));
        return Collections.unmodifiableMap(map);
    }

    public static StatusOption option(String statusCode) {
        return STATUS_MAP.get(statusCode);
    }

    public static List<String> allStatusCodes() {
        return new ArrayList<>(STATUS_MAP.keySet());
    }

    public static List<String> userReportableCodes() {
        return USER_REPORTABLE_CODES;
    }

    public static List<StatusOption> allOptions() {
        return new ArrayList<>(STATUS_MAP.values());
    }

    public static List<StatusOption> userReportableOptions() {
        List<StatusOption> result = new ArrayList<>();
        for (String code : USER_REPORTABLE_CODES) {
            result.add(option(code));
        }
        return result;
    }

    public static String groupFor(String statusCode) {
        StatusOption option = option(statusCode);
        return option == null ? GROUP_OPERATION : option.group;
    }

    public static String groupLabel(String group) {
        if (GROUP_TISSUE.equals(group)) {
            return "Tissue";
        }
        if (GROUP_SOAP.equals(group)) {
            return "Soap";
        }
        if (GROUP_DRYER.equals(group)) {
            return "Dryer";
        }
        return "Operation";
    }

    public static String labelFor(String statusCode) {
        StatusOption option = option(statusCode);
        return option == null ? statusCode : option.longLabel;
    }

    public static int colorFor(String statusCode) {
        StatusOption option = option(statusCode);
        return option == null ? Color.rgb(158, 160, 166) : option.fillColor;
    }

    public static int textColorFor(String statusCode) {
        StatusOption option = option(statusCode);
        return option == null ? Color.WHITE : option.textColor;
    }

    public static String shortLabelFor(String statusCode) {
        StatusOption option = option(statusCode);
        return option == null ? statusCode : option.shortLabel;
    }

    public static String titleFor(String statusCode) {
        StatusOption option = option(statusCode);
        return option == null ? groupLabel(groupFor(statusCode)) : option.title;
    }

    public String group() {
        return groupFor(statusCode);
    }

    public String label() {
        return labelFor(statusCode);
    }

    public String createdLabel() {
        return new SimpleDateFormat("MMM d, HH:mm", Locale.US).format(new Date(createdAt));
    }

    public String resolvedLabel() {
        if (!resolved || resolvedAt <= 0L) {
            return "";
        }
        return new SimpleDateFormat("MMM d, HH:mm", Locale.US).format(new Date(resolvedAt));
    }

    public void resolve(String resolverName, long resolvedTime) {
        resolved = true;
        resolvedByUserName = resolverName;
        resolvedAt = resolvedTime;
    }
}
