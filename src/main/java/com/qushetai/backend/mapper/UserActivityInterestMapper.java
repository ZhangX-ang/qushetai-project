package com.qushetai.backend.mapper;

import org.apache.ibatis.annotations.*;
import com.qushetai.backend.entity.UserActivityInterest;
import java.util.List;

@Mapper
public interface UserActivityInterestMapper {

    // 插入用户活动兴趣记录 - 修复：使用数据库的NOW()函数设置创建时间
    @Insert("INSERT INTO user_activity_interests (user_id, activity_id, interest_type, created_at) " +
            "VALUES (#{userId}, #{activityId}, #{interestType}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserActivityInterest userActivityInterest);

    // 根据用户ID和活动ID查询
    @Select("SELECT * FROM user_activity_interests WHERE user_id = #{userId} AND activity_id = #{activityId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "activity_id", property = "activityId"),
            @Result(column = "interest_type", property = "interestType"),
            @Result(column = "created_at", property = "createdAt")
    })
    UserActivityInterest findByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);

    // 删除用户对活动的兴趣
    @Delete("DELETE FROM user_activity_interests WHERE user_id = #{userId} AND activity_id = #{activityId}")
    int deleteByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);

    // 获取用户感兴趣的活动ID列表
    @Select("SELECT activity_id FROM user_activity_interests WHERE user_id = #{userId}")
    List<Long> findActivityIdsByUserId(@Param("userId") Long userId);

    // 获取活动感兴趣的用户ID列表
    @Select("SELECT user_id FROM user_activity_interests WHERE activity_id = #{activityId}")
    List<Long> findUserIdsByActivityId(@Param("activityId") Long activityId);

    // 统计活动感兴趣人数
    @Select("SELECT COUNT(*) FROM user_activity_interests WHERE activity_id = #{activityId}")
    int countByActivityId(@Param("activityId") Long activityId);

    // 检查用户是否对活动感兴趣
    @Select("SELECT COUNT(*) FROM user_activity_interests WHERE user_id = #{userId} AND activity_id = #{activityId}")
    int existsByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);

    // 获取用户的所有兴趣记录
    @Select("SELECT * FROM user_activity_interests WHERE user_id = #{userId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "activity_id", property = "activityId"),
            @Result(column = "interest_type", property = "interestType"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<UserActivityInterest> findAllByUserId(@Param("userId") Long userId);

    // 以下是原有的方法，为了向后兼容而保留

    // 插入感兴趣记录 - 修复：确保包含created_at字段（使用实体类的createdAt）
    @Insert("INSERT INTO user_activity_interests (user_id, activity_id, interest_type, created_at) " +
            "VALUES (#{userId}, #{activityId}, #{interestType}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertWithCreatedAt(UserActivityInterest interest);

    // 删除感兴趣记录（与deleteByUserIdAndActivityId功能相同）
    @Delete("DELETE FROM user_activity_interests WHERE user_id = #{userId} AND activity_id = #{activityId}")
    int delete(@Param("userId") Long userId, @Param("activityId") Long activityId);

    // 检查是否已感兴趣（与findByUserIdAndActivityId功能相同）
    @Select("SELECT * FROM user_activity_interests WHERE user_id = #{userId} AND activity_id = #{activityId}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "activity_id", property = "activityId"),
            @Result(column = "interest_type", property = "interestType"),
            @Result(column = "created_at", property = "createdAt")
    })
    UserActivityInterest findByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);

    // 获取活动的感兴趣用户列表（带用户昵称）
    @Select("SELECT uai.*, u.nickname FROM user_activity_interests uai " +
            "JOIN user u ON uai.user_id = u.id " +
            "WHERE uai.activity_id = #{activityId} " +
            "ORDER BY uai.created_at DESC")
    List<UserActivityInterest> findByActivityId(Long activityId);

    // 获取用户感兴趣的活动列表
    @Select("SELECT uai.* FROM user_activity_interests uai " +
            "WHERE uai.user_id = #{userId} " +
            "ORDER BY uai.created_at DESC")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "activity_id", property = "activityId"),
            @Result(column = "interest_type", property = "interestType"),
            @Result(column = "created_at", property = "createdAt")
    })
    List<UserActivityInterest> findByUserId(Long userId);
}