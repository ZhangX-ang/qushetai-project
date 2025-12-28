package com.qushetai.backend.entity;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private String username;        // 新增字段
    private String studentId;        // 改为可为空
    private String phone;
    private String email;
    private String passwordHash;
    private String nickname;
    private String interestTags;
    private String freeTimeSlots;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isAdmin;

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }  // 新增getter
    public void setUsername(String username) { this.username = username; }  // 新增setter
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getInterestTags() { return interestTags; }
    public void setInterestTags(String interestTags) { this.interestTags = interestTags; }
    public String getFreeTimeSlots() { return freeTimeSlots; }
    public void setFreeTimeSlots(String freeTimeSlots) { this.freeTimeSlots = freeTimeSlots; }
    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Integer isAdmin) { this.isAdmin = isAdmin; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +  // 新增
                ", studentId='" + studentId + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='" + (passwordHash != null ? "[HASHED]" : "null") + '\'' +
                ", nickname='" + nickname + '\'' +
                ", interestTags='" + interestTags + '\'' +
                ", freeTimeSlots='" + freeTimeSlots + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isAdmin=" + isAdmin +
                '}';
    }
}