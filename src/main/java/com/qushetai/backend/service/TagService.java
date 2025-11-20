package com.qushetai.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class TagService {

    @Value("${tag.service.base-url:http://192.168.100.104:5001}")
    private String tagServiceBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            String url = tagServiceBaseUrl + "/health";
            System.out.println("【DEBUG】健康检查URL: " + url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            System.out.println("【DEBUG】健康检查响应: " + response.getStatusCode());
            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            System.out.println("【INFO】标签服务健康状态: " + (isHealthy ? "正常" : "异常"));
            return isHealthy;
        } catch (Exception e) {
            System.err.println("标签服务健康检查失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取所有标签分类
     */
    public List<Map<String, Object>> getAllTags() {
        try {
            String url = tagServiceBaseUrl + "/test/tags/categories";
            System.out.println("【DEBUG】获取标签分类URL: " + url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            System.out.println("【DEBUG】获取标签分类响应: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("【DEBUG】获取标签分类Body: " + response.getBody());

                // 修正数据提取逻辑 - 根据实际返回结构
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    List<String> categories = (List<String>) data.get("categories");
                    if (categories != null) {
                        List<Map<String, Object>> result = new ArrayList<>();
                        for (int i = 0; i < categories.size(); i++) {
                            result.add(Map.of(
                                    "id", i + 1,
                                    "name", categories.get(i),
                                    "category", categories.get(i),
                                    "type", "category"
                            ));
                        }
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取标签分类失败: " + e.getMessage());
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 根据分类获取标签
     */
    public List<Map<String, Object>> getTagsByCategory(String category) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(tagServiceBaseUrl + "/test/tags")
                    .queryParam("category", category)
                    .toUriString();
            System.out.println("【DEBUG】根据分类获取标签URL: " + url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            System.out.println("【DEBUG】根据分类获取标签响应: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("【DEBUG】根据分类获取标签Body: " + response.getBody());

                // 修正数据提取逻辑 - 从data.tags中获取
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    List<Map<String, Object>> tags = (List<Map<String, Object>>) data.get("tags");
                    if (tags != null) {
                        // 为每个标签添加类型标识
                        for (Map<String, Object> tag : tags) {
                            tag.put("type", "tag");
                        }
                        return tags;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("根据分类获取标签失败: " + e.getMessage());
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 搜索标签 - 修正搜索逻辑
     */
    public List<Map<String, Object>> searchTags(String keyword) {
        try {
            List<Map<String, Object>> searchResults = new ArrayList<>();

            // 先在分类中搜索
            List<Map<String, Object>> categories = getAllTags();
            for (Map<String, Object> category : categories) {
                String categoryName = (String) category.get("name");
                if (categoryName != null && categoryName.toLowerCase().contains(keyword.toLowerCase())) {
                    searchResults.add(category);
                }
            }

            // 在所有分类的标签中搜索
            for (Map<String, Object> category : categories) {
                String categoryName = (String) category.get("name");
                if (categoryName != null) {
                    List<Map<String, Object>> tags = getTagsByCategory(categoryName);
                    for (Map<String, Object> tag : tags) {
                        String tagName = (String) tag.get("name");
                        if (tagName != null && tagName.toLowerCase().contains(keyword.toLowerCase())) {
                            // 检查是否已存在相同的标签
                            boolean exists = searchResults.stream()
                                    .anyMatch(result ->
                                            result.get("tag_id") != null &&
                                                    result.get("tag_id").equals(tag.get("tag_id"))
                                    );
                            if (!exists) {
                                searchResults.add(tag);
                            }
                        }
                    }
                }
            }

            return searchResults;
        } catch (Exception e) {
            System.err.println("搜索标签失败: " + e.getMessage());
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 根据标签ID获取标签详情 - 修正逻辑
     */
    public Map<String, Object> getTagById(Long tagId) {
        try {
            // 遍历所有分类查找标签
            List<Map<String, Object>> categories = getAllTags();
            for (Map<String, Object> category : categories) {
                String categoryName = (String) category.get("name");
                if (categoryName != null) {
                    List<Map<String, Object>> tags = getTagsByCategory(categoryName);
                    for (Map<String, Object> tag : tags) {
                        Object tagIdObj = tag.get("tag_id");
                        if (tagIdObj != null) {
                            Long currentTagId = null;
                            if (tagIdObj instanceof Long) {
                                currentTagId = (Long) tagIdObj;
                            } else if (tagIdObj instanceof Integer) {
                                currentTagId = ((Integer) tagIdObj).longValue();
                            }

                            if (currentTagId != null && currentTagId.equals(tagId)) {
                                return tag;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取标签详情失败: " + e.getMessage());
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    // 其他方法保持不变...
    /**
     * 根据活动ID获取标签 - 需要C同学提供对应接口
     */
    public List<String> getTagsByActivityId(Long activityId) {
        System.out.println("【INFO】获取活动标签功能待C同学实现对应接口");
        // 这个功能需要C同学提供 /test/activities/{activityId}/tags 类似的接口
        return Collections.emptyList();
    }

    /**
     * 获取标签服务的基本URL
     */
    public String getTagServiceBaseUrl() {
        return tagServiceBaseUrl;
    }

    /**
     * 获取服务状态信息
     */
    public Map<String, Object> getServiceStatus() {
        boolean isHealthy = healthCheck();

        // 测试新接口
        boolean categoriesApiWorking = false;
        boolean tagsByCategoryApiWorking = false;

        try {
            ResponseEntity<Map> categoriesResponse = restTemplate.getForEntity(
                    tagServiceBaseUrl + "/test/tags/categories", Map.class);
            categoriesApiWorking = categoriesResponse.getStatusCode().is2xxSuccessful();

            ResponseEntity<Map> tagsResponse = restTemplate.getForEntity(
                    tagServiceBaseUrl + "/test/tags?category=sports", Map.class);
            tagsByCategoryApiWorking = tagsResponse.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("测试新接口失败: " + e.getMessage());
        }

        // 返回可变的HashMap而不是不可变的Map.of()
        Map<String, Object> status = new HashMap<>();
        status.put("tagServiceHealthy", isHealthy);
        status.put("tagServiceUrl", tagServiceBaseUrl);
        status.put("categoriesApiWorking", categoriesApiWorking);
        status.put("tagsByCategoryApiWorking", tagsByCategoryApiWorking);
        status.put("message", isHealthy ?
                "标签服务运行正常，使用新接口路径" :
                "标签服务连接异常");

        return status;
    }

    /**
     * 直接测试C同学的新接口（调试用）
     */
    public String testNewApis() {
        StringBuilder result = new StringBuilder();

        try {
            // 测试分类接口
            String categoriesUrl = tagServiceBaseUrl + "/test/tags/categories";
            result.append("测试分类接口: ").append(categoriesUrl).append("\n");
            ResponseEntity<String> categoriesResponse = restTemplate.getForEntity(categoriesUrl, String.class);
            result.append("响应: ").append(categoriesResponse.getStatusCode()).append("\n");
            result.append("数据: ").append(categoriesResponse.getBody()).append("\n\n");

            // 测试按分类获取标签接口
            String tagsUrl = tagServiceBaseUrl + "/test/tags?category=sports";
            result.append("测试标签接口: ").append(tagsUrl).append("\n");
            ResponseEntity<String> tagsResponse = restTemplate.getForEntity(tagsUrl, String.class);
            result.append("响应: ").append(tagsResponse.getStatusCode()).append("\n");
            result.append("数据: ").append(tagsResponse.getBody()).append("\n");

        } catch (Exception e) {
            result.append("测试失败: ").append(e.getMessage());
        }

        return result.toString();
    }
}