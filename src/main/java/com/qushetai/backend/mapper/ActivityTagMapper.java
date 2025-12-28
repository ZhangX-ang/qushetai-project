package com.qushetai.backend.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ActivityTagMapper {

    // 为活动添加标签
    @Insert("INSERT INTO activity_tags (activity_id, tag_id) VALUES (#{activityId}, #{tagId})")
    int addTagToActivity(@Param("activityId") Long activityId, @Param("tagId") Long tagId);

    // 删除活动的某个标签
    @Delete("DELETE FROM activity_tags WHERE activity_id = #{activityId} AND tag_id = #{tagId}")
    int removeTagFromActivity(@Param("activityId") Long activityId, @Param("tagId") Long tagId);

    // 获取活动的所有标签ID
    @Select("SELECT tag_id FROM activity_tags WHERE activity_id = #{activityId}")
    List<Long> findTagIdsByActivityId(Long activityId);

    // 获取标签的所有活动ID
    @Select("SELECT activity_id FROM activity_tags WHERE tag_id = #{tagId}")
    List<Long> findActivityIdsByTagId(Long tagId);

    // 删除活动的所有标签
    @Delete("DELETE FROM activity_tags WHERE activity_id = #{activityId}")
    int deleteAllTagsByActivityId(Long activityId);

    // 批量添加标签到活动
    @Insert("<script>" +
            "INSERT INTO activity_tags (activity_id, tag_id) VALUES " +
            "<foreach collection='tagIds' item='tagId' separator=','>" +
            "(#{activityId}, #{tagId})" +
            "</foreach>" +
            "</script>")
    int batchAddTagsToActivity(@Param("activityId") Long activityId, @Param("tagIds") List<Long> tagIds);

    // 检查标签是否已关联到活动
    @Select("SELECT COUNT(*) FROM activity_tags WHERE activity_id = #{activityId} AND tag_id = #{tagId}")
    int checkTagExists(@Param("activityId") Long activityId, @Param("tagId") Long tagId);
}