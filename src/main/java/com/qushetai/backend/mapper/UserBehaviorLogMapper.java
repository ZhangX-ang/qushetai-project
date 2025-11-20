package com.qushetai.backend.mapper;

import com.qushetai.backend.entity.UserBehaviorLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserBehaviorLogMapper {

    // 插入行为日志
    @Insert("INSERT INTO user_behavior_log (user_id, session_id, event, page, referrer, item_id, " +
            "item_tags, organizer_id, capacity, dwell_ms, position, query, filters, context, " +
            "user_state, timestamp) " +
            "VALUES (#{userId}, #{sessionId}, #{event}, #{page}, #{referrer}, #{itemId}, " +
            "#{itemTags}, #{organizerId}, #{capacity}, #{dwellMs}, #{position}, #{query}, " +
            "#{filters}, #{context}, #{userState}, #{timestamp})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserBehaviorLog log);

    // 根据用户ID查询行为日志
    @Select("SELECT * FROM user_behavior_log WHERE user_id = #{userId} ORDER BY timestamp DESC")
    List<UserBehaviorLog> findByUserId(Long userId);

    // 根据事件类型查询
    @Select("SELECT * FROM user_behavior_log WHERE event = #{event} ORDER BY timestamp DESC")
    List<UserBehaviorLog> findByEvent(String event);

    // 根据时间范围查询
    @Select("SELECT * FROM user_behavior_log WHERE timestamp BETWEEN #{startTime} AND #{endTime} ORDER BY timestamp DESC")
    List<UserBehaviorLog> findByTimeRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    // === 新增方法：数据监控专用 ===

    // 统计总日志数
    @Select("SELECT COUNT(*) FROM user_behavior_log")
    int countTotalLogs();

    // 统计今日日志数
    @Select("SELECT COUNT(*) FROM user_behavior_log WHERE DATE(created_at) = CURDATE()")
    int countTodayLogs();

    // 获取事件类型分布
    @Select("SELECT event, COUNT(*) as count FROM user_behavior_log GROUP BY event")
    @MapKey("event")
    Map<String, Map<String, Object>> getEventDistribution();

    // 获取最近7天的行为数据趋势
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM user_behavior_log " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> getWeeklyTrend();

    // === 新增方法：测试数据管理 ===

    // 删除所有测试数据（开发用）
    @Delete("DELETE FROM user_behavior_log")
    int deleteAllTestData();
}