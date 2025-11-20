package com.qushetai.backend.mapper;

import com.qushetai.backend.entity.UserActivityInterest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserActivityInterestMapper {

    // 插入感兴趣记录
    @Insert("INSERT INTO user_activity_interest (user_id, activity_id, interest_type) " +
            "VALUES (#{userId}, #{activityId}, #{interestType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserActivityInterest interest);

    // 删除感兴趣记录
    @Delete("DELETE FROM user_activity_interest WHERE user_id = #{userId} AND activity_id = #{activityId}")
    int delete(@Param("userId") Long userId, @Param("activityId") Long activityId);

    // 检查是否已感兴趣
    @Select("SELECT * FROM user_activity_interest WHERE user_id = #{userId} AND activity_id = #{activityId}")
    UserActivityInterest findByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);

    // 获取活动的感兴趣用户列表
    @Select("SELECT uai.*, u.nickname FROM user_activity_interest uai " +
            "JOIN user u ON uai.user_id = u.id " +
            "WHERE uai.activity_id = #{activityId} " +
            "ORDER BY uai.created_at DESC")
    List<UserActivityInterest> findByActivityId(Long activityId);

    // 获取用户感兴趣的活动列表
    @Select("SELECT uai.* FROM user_activity_interest uai " +
            "WHERE uai.user_id = #{userId} " +
            "ORDER BY uai.created_at DESC")
    List<UserActivityInterest> findByUserId(Long userId);

    // 统计活动的感兴趣人数
    @Select("SELECT COUNT(*) FROM user_activity_interest WHERE activity_id = #{activityId}")
    int countByActivityId(Long activityId);
}