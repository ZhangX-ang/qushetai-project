package com.qushetai.backend.service;

import com.qushetai.backend.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tag.service.base-url:http://192.168.100.105:5001}")
    private String tagServiceBaseUrl;

    @Value("${spring.datasource.url:jdbc:mysql://192.168.100.105:3306/qushetai}")
    private String databaseUrl;

    private boolean dbConnectionTested = false;
    private boolean isDbConnected = false;

    /**
     * 测试数据库连接
     */
    private boolean testDatabaseConnection() {
        try {
            if (!dbConnectionTested) {
                System.out.println("【DEBUG】测试数据库连接...");
                System.out.println("【DEBUG】数据库URL: " + databaseUrl);

                // 尝试执行简单的查询
                int count = tagMapper.countActiveTags();
                System.out.println("【DEBUG】数据库连接成功！标签总数: " + count);
                isDbConnected = true;
                dbConnectionTested = true;
            }
            return isDbConnected;
        } catch (Exception e) {
            System.err.println("【ERROR】数据库连接失败: " + e.getMessage());
            System.err.println("【ERROR】请检查数据库配置，确认以下信息：");
            System.err.println("【ERROR】1. 数据库地址: " + databaseUrl);
            System.err.println("【ERROR】2. 用户名/密码是否正确");
            System.err.println("【ERROR】3. 数据库同学是否开放了远程连接权限");
            isDbConnected = false;
            dbConnectionTested = true;
            return false;
        }
    }

    /**
     * 获取所有标签 - 优先从C++标签API获取
     */
    public List<Map<String, Object>> getAllTags() {
        // 先尝试从C++标签API获取
        System.out.println("【DEBUG】尝试从C++标签API获取标签: " + tagServiceBaseUrl);

        try {
            String url = tagServiceBaseUrl + "/api/tags";
            System.out.println("【DEBUG】调用标签API URL: " + url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            System.out.println("【DEBUG】标签API响应状态码: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                System.out.println("【DEBUG】标签API返回数据: " + body);

                // 解析返回的数据
                List<Map<String, Object>> tags = parseApiResponse(body);
                if (tags != null && !tags.isEmpty()) {
                    System.out.println("【INFO】从C++标签API成功获取 " + tags.size() + " 个标签");
                    return tags;
                }
            }
        } catch (Exception e) {
            System.err.println("【ERROR】调用C++标签API失败，尝试数据库: " + e.getMessage());
        }

        // C++ API失败，尝试从数据库获取
        if (testDatabaseConnection()) {
            try {
                System.out.println("【DEBUG】从数据库获取标签...");
                List<Map<String, Object>> tags = tagMapper.findAllTags();
                System.out.println("【DEBUG】从数据库成功获取 " + tags.size() + " 个标签");

                // 确保每个标签都有必要的字段
                for (Map<String, Object> tag : tags) {
                    if (!tag.containsKey("type")) {
                        tag.put("type", "tag");
                    }
                    if (!tag.containsKey("category") && tag.containsKey("name")) {
                        // 如果没有分类字段，使用一个默认分类
                        tag.put("category", "general");
                    }
                }
                return tags;
            } catch (Exception e) {
                System.err.println("【ERROR】从数据库查询标签失败: " + e.getMessage());
                System.out.println("【INFO】使用模拟标签数据");
                return getMockTags();
            }
        } else {
            System.out.println("【INFO】数据库连接失败，使用模拟标签数据");
            return getMockTags();
        }
    }

    /**
     * 解析C++标签API返回的数据
     */
    private List<Map<String, Object>> parseApiResponse(Map<String, Object> apiResponse) {
        try {
            if (apiResponse == null || apiResponse.isEmpty()) {
                return Collections.emptyList();
            }

            // 检查API返回的数据格式
            System.out.println("【DEBUG】解析C++标签API响应，keys: " + apiResponse.keySet());

            List<Map<String, Object>> tags = new ArrayList<>();

            // 假设API直接返回标签列表或包装在data字段中
            Object data = apiResponse.get("data");
            if (data instanceof List) {
                // data是列表
                for (Object item : (List<?>) data) {
                    if (item instanceof Map) {
                        tags.add(convertTagFormat((Map<String, Object>) item));
                    }
                }
            } else if (apiResponse.containsKey("tags")) {
                // 有tags字段
                Object tagsObj = apiResponse.get("tags");
                if (tagsObj instanceof List) {
                    for (Object item : (List<?>) tagsObj) {
                        if (item instanceof Map) {
                            tags.add(convertTagFormat((Map<String, Object>) item));
                        }
                    }
                }
            } else {
                // 尝试将整个响应当作标签列表
                for (Map.Entry<String, Object> entry : apiResponse.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        Map<String, Object> tagData = (Map<String, Object>) entry.getValue();
                        tagData.put("name", entry.getKey()); // 使用key作为标签名
                        tags.add(convertTagFormat(tagData));
                    }
                }
            }

            System.out.println("【DEBUG】解析出 " + tags.size() + " 个标签");
            return tags;
        } catch (Exception e) {
            System.err.println("【ERROR】解析标签API响应失败: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 转换标签格式为标准格式
     */
    private Map<String, Object> convertTagFormat(Map<String, Object> rawTag) {
        Map<String, Object> tag = new HashMap<>();

        // 复制所有字段
        tag.putAll(rawTag);

        // 确保有必要的字段
        if (!tag.containsKey("type")) {
            tag.put("type", "tag");
        }

        if (!tag.containsKey("id") && tag.containsKey("tagId")) {
            tag.put("id", tag.get("tagId"));
        }

        if (!tag.containsKey("category") && tag.containsKey("name")) {
            // 如果没有分类字段，尝试从name推断或使用默认
            String name = tag.get("name").toString();
            if (name.contains("篮球") || name.contains("足球") || name.contains("运动")) {
                tag.put("category", "运动");
            } else if (name.contains("编程") || name.contains("学习")) {
                tag.put("category", "学习");
            } else if (name.contains("摄影") || name.contains("艺术")) {
                tag.put("category", "艺术");
            } else {
                tag.put("category", "general");
            }
        }

        return tag;
    }

    /**
     * 根据分类获取标签
     */
    public List<Map<String, Object>> getTagsByCategory(String category) {
        // 先尝试从C++ API获取
        try {
            String url = UriComponentsBuilder.fromHttpUrl(tagServiceBaseUrl + "/api/tags")
                    .queryParam("category", category)
                    .toUriString();
            System.out.println("【DEBUG】按分类获取标签URL: " + url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> tags = parseApiResponse(response.getBody());
                if (!tags.isEmpty()) {
                    System.out.println("【DEBUG】从API获取分类为 '" + category + "' 的标签: " + tags.size() + " 个");
                    return tags;
                }
            }
        } catch (Exception e) {
            System.err.println("【ERROR】从API按分类获取标签失败: " + e.getMessage());
        }

        // API失败，尝试从数据库获取
        if (testDatabaseConnection()) {
            try {
                System.out.println("【DEBUG】从数据库获取分类为 '" + category + "' 的标签");
                List<Map<String, Object>> tags = tagMapper.findByCategory(category);

                if (tags != null && !tags.isEmpty()) {
                    System.out.println("【DEBUG】找到 " + tags.size() + " 个标签");
                    for (Map<String, Object> tag : tags) {
                        tag.put("type", "tag");
                    }
                    return tags;
                } else {
                    System.out.println("【INFO】数据库中没有分类为 '" + category + "' 的标签，尝试获取所有标签再筛选");
                    // 如果数据库中该分类没有标签，尝试从所有标签中筛选
                    List<Map<String, Object>> allTags = getAllTags();
                    List<Map<String, Object>> filteredTags = new ArrayList<>();
                    for (Map<String, Object> tag : allTags) {
                        Object cat = tag.get("category");
                        if (cat != null && cat.toString().equalsIgnoreCase(category)) {
                            filteredTags.add(tag);
                        }
                    }
                    return filteredTags;
                }
            } catch (Exception e) {
                System.err.println("【ERROR】根据分类查询标签失败: " + e.getMessage());
                return filterMockTagsByCategory(category);
            }
        } else {
            return filterMockTagsByCategory(category);
        }
    }

    /**
     * 搜索标签
     */
    public List<Map<String, Object>> searchTags(String keyword) {
        // 先尝试从C++ API搜索
        try {
            String url = UriComponentsBuilder.fromHttpUrl(tagServiceBaseUrl + "/api/tags")
                    .queryParam("search", keyword)
                    .toUriString();
            System.out.println("【DEBUG】搜索标签URL: " + url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> tags = parseApiResponse(response.getBody());
                if (!tags.isEmpty()) {
                    System.out.println("【DEBUG】从API搜索到 '" + keyword + "' 的标签: " + tags.size() + " 个");
                    return tags;
                }
            }
        } catch (Exception e) {
            System.err.println("【ERROR】从API搜索标签失败: " + e.getMessage());
        }

        // API失败，尝试从数据库搜索
        if (testDatabaseConnection()) {
            try {
                System.out.println("【DEBUG】在数据库中搜索标签: " + keyword);
                List<Map<String, Object>> tags = tagMapper.searchByName(keyword);

                if (tags != null && !tags.isEmpty()) {
                    System.out.println("【DEBUG】搜索到 " + tags.size() + " 个相关标签");
                    for (Map<String, Object> tag : tags) {
                        tag.put("type", "tag");
                    }
                    return tags;
                } else {
                    // 如果数据库搜索无结果，尝试从模拟数据中搜索
                    return searchMockTags(keyword);
                }
            } catch (Exception e) {
                System.err.println("【ERROR】搜索标签失败: " + e.getMessage());
                return searchMockTags(keyword);
            }
        } else {
            return searchMockTags(keyword);
        }
    }

    /**
     * 根据标签ID获取标签详情
     */
    public Map<String, Object> getTagById(Long tagId) {
        // 先尝试从C++ API获取
        try {
            String url = tagServiceBaseUrl + "/api/tags/" + tagId;
            System.out.println("【DEBUG】获取标签详情URL: " + url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                System.out.println("【DEBUG】标签详情API返回: " + body);
                return convertTagFormat(body);
            }
        } catch (Exception e) {
            System.err.println("【ERROR】从API获取标签详情失败: " + e.getMessage());
        }

        // API失败，尝试从数据库获取
        if (testDatabaseConnection()) {
            try {
                System.out.println("【DEBUG】从数据库获取标签ID: " + tagId);
                Map<String, Object> tag = tagMapper.findById(tagId);

                if (tag != null && !tag.isEmpty()) {
                    tag.put("type", "tag");
                    return tag;
                } else {
                    System.out.println("【INFO】数据库中未找到标签ID: " + tagId);
                    // 尝试从模拟数据中查找
                    return findMockTagById(tagId);
                }
            } catch (Exception e) {
                System.err.println("【ERROR】获取标签详情失败: " + e.getMessage());
                return findMockTagById(tagId);
            }
        } else {
            return findMockTagById(tagId);
        }
    }

    /**
     * 根据活动ID获取标签
     */
    public List<String> getTagsByActivityId(Long activityId) {
        // 尝试从C++ API获取
        try {
            String url = tagServiceBaseUrl + "/api/activities/" + activityId + "/tags";
            System.out.println("【DEBUG】获取活动标签URL: " + url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Object data = body.get("data");

                if (data instanceof List) {
                    List<String> tags = new ArrayList<>();
                    for (Object tag : (List<?>) data) {
                        if (tag instanceof String) {
                            tags.add((String) tag);
                        } else if (tag instanceof Map) {
                            Object name = ((Map<?, ?>) tag).get("name");
                            if (name instanceof String) {
                                tags.add((String) name);
                            }
                        }
                    }
                    return tags;
                }
            }
        } catch (Exception e) {
            System.err.println("【ERROR】从API获取活动标签失败: " + e.getMessage());
        }

        // 如果数据库中有activity_tags表，可以从数据库查询
        System.out.println("【INFO】获取活动标签功能需要ActivityTagMapper实现");
        return Collections.emptyList();
    }

    /**
     * 健康检查 - 检查C++标签服务和数据库连接
     */
    public boolean healthCheck() {
        // 先检查C++标签服务
        boolean apiHealthy = checkTagApiHealth();
        if (apiHealthy) {
            System.out.println("【INFO】C++标签服务健康");
            return true;
        }

        // C++服务不可用，检查数据库
        boolean dbHealthy = testDatabaseConnection();
        if (dbHealthy) {
            System.out.println("【INFO】数据库连接正常");
            return true;
        }

        System.err.println("【ERROR】C++标签服务和数据库都不可用");
        return false;
    }

    /**
     * 检查C++标签API健康状态
     */
    private boolean checkTagApiHealth() {
        try {
            String url = tagServiceBaseUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("【ERROR】C++标签服务健康检查失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取服务状态信息
     */
    public Map<String, Object> getServiceStatus() {
        boolean isApiHealthy = checkTagApiHealth();
        boolean isDbHealthy = testDatabaseConnection();

        Map<String, Object> status = new HashMap<>();
        status.put("tagApiHealthy", isApiHealthy);
        status.put("databaseConnected", isDbHealthy);
        status.put("tagApiUrl", tagServiceBaseUrl);
        status.put("databaseUrl", databaseUrl);
        status.put("mockDataEnabled", !isApiHealthy && !isDbHealthy);

        try {
            if (isApiHealthy) {
                status.put("message", "C++标签服务连接正常");
                status.put("dataSource", "C++标签API");
            } else if (isDbHealthy) {
                int tagCount = tagMapper.countActiveTags();
                status.put("tagCount", tagCount);
                status.put("message", "数据库连接正常，标签数量: " + tagCount);
                status.put("dataSource", "MySQL数据库");
            } else {
                status.put("message", "C++标签服务和数据库都不可用，使用模拟数据");
                status.put("dataSource", "模拟数据");
            }
        } catch (Exception e) {
            status.put("message", "服务状态检查异常: " + e.getMessage());
        }

        return status;
    }

    /**
     * 测试C++标签API（调试用）
     */
    public String testTagApi() {
        StringBuilder result = new StringBuilder();

        try {
            result.append("=== C++标签API测试 ===\n");
            result.append("API地址: ").append(tagServiceBaseUrl).append("\n");

            // 测试健康检查
            String healthUrl = tagServiceBaseUrl + "/health";
            result.append("\n1. 健康检查: ").append(healthUrl).append("\n");
            try {
                ResponseEntity<String> healthResponse = restTemplate.getForEntity(healthUrl, String.class);
                result.append("   状态码: ").append(healthResponse.getStatusCode()).append("\n");
                result.append("   响应: ").append(healthResponse.getBody()).append("\n");
            } catch (Exception e) {
                result.append("   失败: ").append(e.getMessage()).append("\n");
            }

            // 测试获取所有标签
            String tagsUrl = tagServiceBaseUrl + "/api/tags";
            result.append("\n2. 获取所有标签: ").append(tagsUrl).append("\n");
            try {
                ResponseEntity<String> tagsResponse = restTemplate.getForEntity(tagsUrl, String.class);
                result.append("   状态码: ").append(tagsResponse.getStatusCode()).append("\n");
                result.append("   响应长度: ").append(tagsResponse.getBody().length()).append(" 字符\n");
                result.append("   前200字符: ").append(tagsResponse.getBody().substring(0, Math.min(200, tagsResponse.getBody().length()))).append("\n");
            } catch (Exception e) {
                result.append("   失败: ").append(e.getMessage()).append("\n");
            }

        } catch (Exception e) {
            result.append("测试失败: ").append(e.getMessage());
        }

        return result.toString();
    }

    /**
     * 获取C++标签服务地址
     */
    public String getTagServiceBaseUrl() {
        return tagServiceBaseUrl;
    }

    /**
     * 模拟标签数据（当所有服务都不可用时）
     */
    private List<Map<String, Object>> getMockTags() {
        List<Map<String, Object>> mockTags = new ArrayList<>();

        // 预定义的分类和标签
        String[] categories = {"运动", "艺术", "学习", "社交", "娱乐", "生活"};
        Map<String, String[]> categoryTags = new HashMap<>();
        categoryTags.put("运动", new String[]{"篮球", "足球", "羽毛球", "跑步", "健身", "游泳", "瑜伽", "乒乓球"});
        categoryTags.put("艺术", new String[]{"摄影", "绘画", "舞蹈", "手工艺", "设计", "音乐", "戏剧", "书法"});
        categoryTags.put("学习", new String[]{"编程", "数学", "外语", "写作", "阅读", "科学", "历史", "哲学"});
        categoryTags.put("社交", new String[]{"聚会", "聊天", "桌游", "旅行", "聚餐", "交友", "团建", "合作"});
        categoryTags.put("娱乐", new String[]{"电竞", "手游", "主机游戏", "卡牌", "电影", "追剧", "动漫", "综艺"});
        categoryTags.put("生活", new String[]{"美食", "烹饪", "旅行", "购物", "养生", "宠物", "园艺", "穿搭"});

        int id = 1;
        for (String category : categories) {
            for (String tagName : categoryTags.get(category)) {
                Map<String, Object> tag = new HashMap<>();
                tag.put("id", id++);
                tag.put("name", tagName);
                tag.put("category", category);
                tag.put("description", tagName + "相关活动");
                tag.put("type", "tag");
                tag.put("usageCount", new Random().nextInt(100));
                tag.put("isActive", 1);
                tag.put("createdAt", new Date());
                mockTags.add(tag);
            }
        }

        System.out.println("【INFO】生成 " + mockTags.size() + " 个模拟标签");
        return mockTags;
    }

    /**
     * 从模拟数据中按分类筛选
     */
    private List<Map<String, Object>> filterMockTagsByCategory(String category) {
        List<Map<String, Object>> allMockTags = getMockTags();
        List<Map<String, Object>> filteredTags = new ArrayList<>();

        for (Map<String, Object> tag : allMockTags) {
            Object cat = tag.get("category");
            if (cat != null && cat.toString().equalsIgnoreCase(category)) {
                filteredTags.add(tag);
            }
        }

        System.out.println("【INFO】从模拟数据中找到 " + filteredTags.size() + " 个分类为 '" + category + "' 的标签");
        return filteredTags;
    }

    /**
     * 在模拟数据中搜索标签
     */
    private List<Map<String, Object>> searchMockTags(String keyword) {
        List<Map<String, Object>> allMockTags = getMockTags();
        List<Map<String, Object>> searchResults = new ArrayList<>();

        for (Map<String, Object> tag : allMockTags) {
            String tagName = (String) tag.get("name");
            String tagCategory = (String) tag.get("category");

            if ((tagName != null && tagName.toLowerCase().contains(keyword.toLowerCase())) ||
                    (tagCategory != null && tagCategory.toLowerCase().contains(keyword.toLowerCase()))) {
                searchResults.add(tag);
            }
        }

        System.out.println("【INFO】在模拟数据中搜索到 " + searchResults.size() + " 个包含 '" + keyword + "' 的标签");
        return searchResults;
    }

    /**
     * 在模拟数据中根据ID查找标签
     */
    private Map<String, Object> findMockTagById(Long tagId) {
        List<Map<String, Object>> allMockTags = getMockTags();

        for (Map<String, Object> tag : allMockTags) {
            Object id = tag.get("id");
            if (id != null) {
                Long currentId = null;
                if (id instanceof Long) {
                    currentId = (Long) id;
                } else if (id instanceof Integer) {
                    currentId = ((Integer) id).longValue();
                }

                if (currentId != null && currentId.equals(tagId)) {
                    return tag;
                }
            }
        }

        System.out.println("【INFO】在模拟数据中未找到标签ID: " + tagId);
        // 返回一个默认标签
        Map<String, Object> defaultTag = new HashMap<>();
        defaultTag.put("id", tagId);
        defaultTag.put("name", "标签" + tagId);
        defaultTag.put("category", "general");
        defaultTag.put("description", "默认标签");
        defaultTag.put("type", "tag");
        return defaultTag;
    }

    /**
     * 直接测试数据库连接（调试用）
     */
    public String testDatabaseConnectionDirectly() {
        StringBuilder result = new StringBuilder();

        try {
            result.append("=== 数据库连接测试 ===\n");
            result.append("数据库URL: ").append(databaseUrl).append("\n");

            // 测试连接
            result.append("\n1. 测试简单查询...\n");
            int count = tagMapper.countActiveTags();
            result.append("   成功！标签总数: ").append(count).append("\n");

            // 获取样本数据
            result.append("\n2. 获取样本标签...\n");
            List<Map<String, Object>> sampleTags = tagMapper.findPopularTags(3);
            for (Map<String, Object> tag : sampleTags) {
                result.append("   - ").append(tag.get("name")).append(" (").append(tag.get("category")).append(")\n");
            }

            // 获取分类列表
            result.append("\n3. 获取标签分类...\n");
            List<Map<String, Object>> allTags = tagMapper.findAllTags();
            Set<String> categories = new HashSet<>();
            for (Map<String, Object> tag : allTags) {
                Object category = tag.get("category");
                if (category != null) {
                    categories.add(category.toString());
                }
            }
            result.append("   找到 ").append(categories.size()).append(" 个分类: ").append(String.join(", ", categories)).append("\n");

            result.append("\n=== 数据库连接正常 ===\n");

        } catch (Exception e) {
            result.append("\n=== 数据库连接失败 ===\n");
            result.append("错误信息: ").append(e.getMessage()).append("\n");
            result.append("可能的原因:\n");
            result.append("1. 数据库地址错误\n");
            result.append("2. 数据库服务未运行\n");
            result.append("3. 用户名/密码错误\n");
            result.append("4. 防火墙阻止了连接\n");
            result.append("5. 数据库用户没有远程连接权限\n");
        }

        return result.toString();
    }
}