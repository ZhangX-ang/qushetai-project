package com.qushetai.backend.entity;

import java.time.LocalDateTime;

public class VerificationCode {
    private Long id;
    private String contact;  // 手机号或邮箱
    private String code;     // 验证码
    private Integer type;    // 1-注册，2-登录
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private Integer isUsed;  // 0-未使用，1-已使用

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public Integer getIsUsed() { return isUsed; }
    public void setIsUsed(Integer isUsed) { this.isUsed = isUsed; }
}