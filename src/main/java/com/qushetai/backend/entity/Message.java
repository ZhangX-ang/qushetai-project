package com.qushetai.backend.entity;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private Long senderId;       // 发送者ID（系统消息为0）
    private Long receiverId;     // 接收者ID
    private String title;        // 消息标题
    private String content;      // 消息内容
    private Integer messageType; // 消息类型：1=系统通知，2=活动通知，3=社交通知
    private Integer relatedType; // 关联类型：1=活动，2=用户
    private Long relatedId;      // 关联ID（如活动ID）
    private Integer isRead;      // 是否已读：0=未读，1=已读
    private Integer isDeleted;   // 是否删除：0=正常，1=删除
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // 构造函数
    public Message() {}

    public Message(Long receiverId, String title, String content, Integer messageType) {
        this.receiverId = receiverId;
        this.title = title;
        this.content = content;
        this.messageType = messageType;
        this.senderId = 0L; // 默认系统发送
        this.isRead = 0;
        this.isDeleted = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Message(Long senderId, Long receiverId, String title, String content, Integer messageType, Integer relatedType, Long relatedId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.title = title;
        this.content = content;
        this.messageType = messageType;
        this.relatedType = relatedType;
        this.relatedId = relatedId;
        this.isRead = 0;
        this.isDeleted = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getMessageType() { return messageType; }
    public void setMessageType(Integer messageType) { this.messageType = messageType; }

    public Integer getRelatedType() { return relatedType; }
    public void setRelatedType(Integer relatedType) { this.relatedType = relatedType; }

    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }

    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", messageType=" + messageType +
                ", relatedType=" + relatedType +
                ", relatedId=" + relatedId +
                ", isRead=" + isRead +
                ", isDeleted=" + isDeleted +
                ", createdAt=" + createdAt +
                ", readAt=" + readAt +
                '}';
    }
}