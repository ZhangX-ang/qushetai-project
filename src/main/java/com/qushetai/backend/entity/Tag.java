package com.qushetai.backend.entity;

import java.time.LocalDateTime;

public class Tag {
    private Integer id;
    private String name;
    private String category;
    private String description;
    private Integer usageCount;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // 无参构造函数
    public Tag() {}

    // 带参构造函数
    public Tag(String name, String category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.usageCount = 0;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getter和Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", usageCount=" + usageCount +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}