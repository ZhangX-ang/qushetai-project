package com.qushetai.backend.mapper;

import com.qushetai.backend.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    // 修改插入语句，student_id不再必需
    @Insert("INSERT INTO user (student_id, phone, email, password_hash, nickname, interest_tags, free_time_slots, is_admin) " +
            "VALUES (#{studentId}, #{phone}, #{email}, #{passwordHash}, #{nickname}, #{interestTags}, #{freeTimeSlots}, #{isAdmin})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    // 使用 @Results 注解显式映射字段
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    @Results({
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "interest_tags", property = "interestTags"),
            @Result(column = "free_time_slots", property = "freeTimeSlots"),
            @Result(column = "is_active", property = "isActive"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "is_admin", property = "isAdmin")
    })
    User findByPhone(String phone);

    @Select("SELECT * FROM user WHERE email = #{email}")
    @Results({
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "interest_tags", property = "interestTags"),
            @Result(column = "free_time_slots", property = "freeTimeSlots"),
            @Result(column = "is_active", property = "isActive"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "is_admin", property = "isAdmin")
    })
    User findByEmail(String email);

    @Select("SELECT * FROM user WHERE student_id = #{studentId}")
    @Results({
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "interest_tags", property = "interestTags"),
            @Result(column = "free_time_slots", property = "freeTimeSlots"),
            @Result(column = "is_active", property = "isActive"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "is_admin", property = "isAdmin")
    })
    User findByStudentId(String studentId);

    @Select("SELECT * FROM user WHERE id = #{id}")
    @Results({
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "interest_tags", property = "interestTags"),
            @Result(column = "free_time_slots", property = "freeTimeSlots"),
            @Result(column = "is_active", property = "isActive"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "is_admin", property = "isAdmin")
    })
    User findById(Long id);

    @Update("UPDATE user SET nickname=#{nickname}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int update(User user);

    @Update("UPDATE user SET password_hash=#{passwordHash}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    @Update("UPDATE user SET interest_tags=#{interestTags}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int updateInterests(@Param("id") Long id, @Param("interestTags") String interestTags);

    @Update("UPDATE user SET free_time_slots=#{freeTimeSlots}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int updateFreeTime(@Param("id") Long id, @Param("freeTimeSlots") String freeTimeSlots);

    // === 新增方法：数据监控专用 ===

    // 统计总用户数
    @Select("SELECT COUNT(*) FROM user")
    int countUsers();

    // 统计活跃用户数
    @Select("SELECT COUNT(*) FROM user WHERE is_active = 1")
    int countActiveUsers();

    // 统计有行为数据的用户数
    @Select("SELECT COUNT(DISTINCT user_id) FROM user_behavior_log")
    int countUsersWithBehavior();

    // 统计用户兴趣标签分布
    @Select("SELECT interest_tags, COUNT(*) as count FROM user WHERE interest_tags IS NOT NULL AND interest_tags != '' GROUP BY interest_tags")
    @MapKey("interest_tags")
    Map<String, Map<String, Object>> getUserInterestDistribution();

    // 统计用户注册趋势（最近7天）
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM user " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> getRegistrationTrend();

    // 获取所有用户（分页，管理员用）
    @Select("SELECT * FROM user ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "password_hash", property = "passwordHash"),
            @Result(column = "interest_tags", property = "interestTags"),
            @Result(column = "free_time_slots", property = "freeTimeSlots"),
            @Result(column = "is_active", property = "isActive"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt"),
            @Result(column = "is_admin", property = "isAdmin")
    })
    List<User> selectAllUsersWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    // 统计所有用户数量
    @Select("SELECT COUNT(*) FROM user")
    int countAllUsers();

    // 更新用户状态（封禁/解封）
    @Update("UPDATE user SET is_active = #{status} WHERE id = #{userId}")
    int updateUserStatus(@Param("userId") Long userId, @Param("status") Integer status);

    // 检查用户是否是管理员
    @Select("SELECT is_admin FROM user WHERE id = #{userId}")
    Integer isAdmin(Long userId);
}