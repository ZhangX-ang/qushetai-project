package com.qushetai.backend.dto;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    private Boolean success;
    private String message;
    private Map<String, Object> data;
    private Integer code;

    // 成功响应
    public static ApiResponse success(Map<String, Object> data, String message) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data != null ? data : new HashMap<>());
        response.setCode(200);
        return response;
    }

    public static ApiResponse success(String message) {
        return success(new HashMap<>(), message);
    }

    // 错误响应
    public static ApiResponse error(String message, Integer code) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setCode(code);
        response.setData(new HashMap<>());
        return response;
    }

    // Getter和Setter
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", code=" + code +
                '}';
    }
}