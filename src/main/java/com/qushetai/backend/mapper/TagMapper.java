package com.qushetai.backend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface TagMapper {

    // 查询所有标签
    @Select("SELECT * FROM tag WHERE is_active = 1 ORDER BY name")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "category", property = "category"),
            @Result(column = "description", property = "description"),
            @Result(column = "usage_count", property = "usageCount"),
            @Result(column = "is_active", property = "isActive"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<Map<String, Object>> findAllTags();

    // 根据分类查询标签
    @Select("SELECT * FROM tag WHERE category = #{category} AND is_active = 1 ORDER BY name")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "category", property = "category"),
            @Result(column = "description", property = "description")
    })
    List<Map<String, Object>> findByCategory(String category);

    // 根据ID查询标签
    @Select("SELECT * FROM tag WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "category", property = "category"),
            @Result(column = "description", property = "description"),
            @Result(column = "usage_count", property = "usageCount")
    })
    Map<String, Object> findById(Long id);

    // 搜索标签
    @Select("SELECT * FROM tag WHERE name LIKE CONCAT('%', #{keyword}, '%') AND is_active = 1 ORDER BY name")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "category", property = "category"),
            @Result(column = "description", property = "description")
    })
    List<Map<String, Object>> searchByName(String keyword);

    // 获取热门标签（按使用次数排序）
    @Select("SELECT * FROM tag WHERE is_active = 1 ORDER BY usage_count DESC LIMIT #{limit}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "category", property = "category"),
            @Result(column = "usage_count", property = "usageCount")
    })
    List<Map<String, Object>> findPopularTags(@Param("limit") int limit);

    // 统计标签总数
    @Select("SELECT COUNT(*) FROM tag WHERE is_active = 1")
    int countActiveTags();

    // 更新标签使用次数
    @Update("UPDATE tag SET usage_count = usage_count + 1 WHERE id = #{tagId}")
    int incrementUsageCount(Long tagId);

    // 根据名称查询标签
    @Select("SELECT * FROM tag WHERE name = #{name}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "category", property = "category")
    })
    Map<String, Object> findByName(String name);
}