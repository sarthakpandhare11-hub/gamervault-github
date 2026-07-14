package com.example.model;

public class NotificationModel {
    private String notificationId;
    private String id;

    private String title;
    private String message;
    private String type; // "MATCH", "TOURNAMENT", "SYSTEM", "CONTENT", "BATTLE"
    private boolean isRead;
    private long timestamp;

    // NEW: Needed for the live listener routing
    private String targetUserId;
    private String actionRoute; // e.g., "BATTLE:12345" for deep linking

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (this.notificationId == null) {
            this.notificationId = id; // Map old IDs to the new system automatically
        }
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getActionRoute() {
        return actionRoute;
    }

    public void setActionRoute(String actionRoute) {
        this.actionRoute = actionRoute;
    }
}