package com.qushetai.backend.entity;

import java.time.LocalDateTime;

public class UserBehaviorLog {
    private Long id;
    private Long userId;
    private String sessionId;
    private String event;
    private String page;
    private String referrer;
    private Long itemId;
    private String itemTags;
    private Long organizerId;
    private Integer capacity;
    private Integer dwellMs;
    private Integer position;
    private String query;
    private String filters;
    private String context;
    private String userState;
    private Long timestamp;
    private LocalDateTime createdAt;

    // Getter和Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public String getPage() { return page; }
    public void setPage(String page) { this.page = page; }
    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemTags() { return itemTags; }
    public void setItemTags(String itemTags) { this.itemTags = itemTags; }
    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getDwellMs() { return dwellMs; }
    public void setDwellMs(Integer dwellMs) { this.dwellMs = dwellMs; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getFilters() { return filters; }
    public void setFilters(String filters) { this.filters = filters; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public String getUserState() { return userState; }
    public void setUserState(String userState) { this.userState = userState; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}