package com.qushetai.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TagConfig {

    @Value("${tag.api.base-url:http://192.168.100.103:5001}")
    private String tagApiBaseUrl;

    @Value("${tag.api.all-tags:/tags/all}")
    private String allTagsEndpoint;

    @Value("${tag.api.user-tags:/users/{userId}/tags}")
    private String userTagsEndpoint;

    @Value("${tag.api.update-user-tags:/users/{userId}/tags}")
    private String updateUserTagsEndpoint;

    @Value("${tag.api.popular-tags:/tags/popular}")
    private String popularTagsEndpoint;

    @Value("${tag.api.categories:/tags/categories}")
    private String categoriesEndpoint;

    @Value("${tag.api.tag-by-id:/tags/{tagId}}")
    private String tagByIdEndpoint;

    @Value("${tag.api.search-tags:/tags/search}")
    private String searchTagsEndpoint;

    @Value("${tag.api.activities-by-tag:/tags/{tagName}/activities}")
    private String activitiesByTagEndpoint;

    @Value("${tag.api.timeout:5000}")
    private int timeout;

    @Value("${tag.api.retry.count:3}")
    private int retryCount;

    @Value("${tag.api.retry.delay:1000}")
    private int retryDelay;

    // Getter 方法
    public String getTagApiBaseUrl() {
        return tagApiBaseUrl;
    }

    public String getAllTagsEndpoint() {
        return tagApiBaseUrl + allTagsEndpoint;
    }

    public String getUserTagsEndpoint(Long userId) {
        return tagApiBaseUrl + userTagsEndpoint.replace("{userId}", userId.toString());
    }

    public String getUpdateUserTagsEndpoint(Long userId) {
        return tagApiBaseUrl + updateUserTagsEndpoint.replace("{userId}", userId.toString());
    }

    public String getPopularTagsEndpoint() {
        return tagApiBaseUrl + popularTagsEndpoint;
    }

    public String getCategoriesEndpoint() {
        return tagApiBaseUrl + categoriesEndpoint;
    }

    public String getTagByIdEndpoint(Long tagId) {
        return tagApiBaseUrl + tagByIdEndpoint.replace("{tagId}", tagId.toString());
    }

    public String getSearchTagsEndpoint(String keyword) {
        return tagApiBaseUrl + searchTagsEndpoint + "?keyword=" + keyword;
    }

    public String getActivitiesByTagEndpoint(String tagName) {
        return tagApiBaseUrl + activitiesByTagEndpoint.replace("{tagName}", tagName);
    }

    public int getTimeout() {
        return timeout;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getRetryDelay() {
        return retryDelay;
    }
}