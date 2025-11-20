package com.qushetai.backend.entity;

import java.time.LocalDateTime;

public class UserActivityInterest {
    private Long id;
    private Long userId;
    private Long activityId;
    private Integer interestType; // 1:感兴趣
    private LocalDateTime createdAt;

    // 构造函数
    public UserActivityInterest() {}

    public UserActivityInterest(Long userId, Long activityId) {
        this.userId = userId;
        this.activityId = activityId;
        this.interestType = 1;
    }

    // Getter和Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Integer getInterestType() { return interestType; }
    public void setInterestType(Integer interestType) { this.interestType = interestType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}