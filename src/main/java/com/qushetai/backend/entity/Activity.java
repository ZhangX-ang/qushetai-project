package com.qushetai.backend.entity;

import java.time.LocalDateTime;

public class Activity {
    private Long id;
    private String title;
    private String description;
    private String activityType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Long organizerId;
    private Integer maxParticipants;
    private Integer currentParticipants = 0;
    private String status = "active"; // 🔥 改为 String 类型，设置默认值
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 无参构造函数
    public Activity() {}

    // 带参构造函数
    public Activity(String title, String description, String activityType,
                    LocalDateTime startTime, String location, Long organizerId) {
        this.title = title;
        this.description = description;
        this.activityType = activityType;
        this.startTime = startTime;
        this.location = location;
        this.organizerId = organizerId;
        // 设置默认值
        this.currentParticipants = 0;
        this.status = "active"; // 🔥 改为字符串类型
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActivityType() {
        // 空值保护
        return activityType != null ? activityType : "社交活动";
    }
    public void setActivityType(String activityType) {
        this.activityType = activityType != null ? activityType : "社交活动";
    }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public Integer getCurrentParticipants() {
        // 空值保护
        return currentParticipants != null ? currentParticipants : 0;
    }
    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants != null ? currentParticipants : 0;
    }

    // 🔥 修改：status 字段的 getter 和 setter 改为 String 类型
    public String getStatus() {
        // 空值保护
        return status != null ? status : "active";
    }
    public void setStatus(String status) {
        this.status = status != null ? status : "active";
    }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", activityType='" + activityType + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", location='" + location + '\'' +
                ", organizerId=" + organizerId +
                ", maxParticipants=" + maxParticipants +
                ", currentParticipants=" + currentParticipants +
                ", status='" + status + '\'' + // 🔥 修改：status 改为字符串输出
                ", tags='" + tags + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}