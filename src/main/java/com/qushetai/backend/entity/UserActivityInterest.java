package com.qushetai.backend.entity;

import java.time.LocalDateTime;

public class UserActivityInterest {
    private Long id;
    private Long userId;
    private Long activityId;
    private Integer interestType; // 1:感兴趣
    private LocalDateTime createdAt;

    // 无参构造函数
    public UserActivityInterest() {}

    // 带参构造函数 - 修复：添加createdAt设置
    public UserActivityInterest(Long userId, Long activityId) {
        this.userId = userId;
        this.activityId = activityId;
        this.interestType = 1;
        this.createdAt = LocalDateTime.now(); // 新增：设置创建时间
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

    @Override
    public String toString() {
        return "UserActivityInterest{" +
                "id=" + id +
                ", userId=" + userId +
                ", activityId=" + activityId +
                ", interestType=" + interestType +
                ", createdAt=" + createdAt +
                '}';
    }
}