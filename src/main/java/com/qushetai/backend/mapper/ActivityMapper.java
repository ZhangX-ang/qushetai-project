package com.qushetai.backend.mapper;

import com.qushetai.backend.entity.Activity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;  // 添加这行导入语句

@Mapper
public interface ActivityMapper {

    // 插入新活动
    @Insert("INSERT INTO activity (title, description, activity_type, start_time, end_time, location, " +
            "organizer_id, max_participants, current_participants, status, tags) " +
            "VALUES (#{title}, #{description}, #{activityType}, #{startTime}, #{endTime}, #{location}, " +
            "#{organizerId}, #{maxParticipants}, #{currentParticipants}, #{status}, #{tags})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertActivity(Activity activity);

    // 查询所有活动（按创建时间倒序）
    @Select("SELECT * FROM activity WHERE status = 1 ORDER BY created_at DESC")
    List<Activity> selectAllActivities();

    // 分页查询活动
    @Select("SELECT * FROM activity WHERE status = 1 ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Activity> selectActivitiesWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    // 根据ID查询活动
    @Select("SELECT * FROM activity WHERE id = #{id} AND status = 1")
    Activity selectActivityById(Long id);

    // 根据发起者ID查询活动
    @Select("SELECT * FROM activity WHERE organizer_id = #{organizerId} AND status = 1 ORDER BY created_at DESC")
    List<Activity> selectActivitiesByOrganizer(Long organizerId);

    // 更新活动信息
    @Update("UPDATE activity SET title=#{title}, description=#{description}, activity_type=#{activityType}, " +
            "start_time=#{startTime}, end_time=#{endTime}, location=#{location}, max_participants=#{maxParticipants}, " +
            "current_participants=#{currentParticipants}, tags=#{tags}, updated_at=CURRENT_TIMESTAMP " +
            "WHERE id=#{id}")
    int updateActivity(Activity activity);

    // 软删除活动（将status设为0）
    @Update("UPDATE activity SET status=0, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int deleteActivity(Long id);

    // 统计活动总数
    @Select("SELECT COUNT(*) FROM activity WHERE status = 1")
    int countActivities();

    // === 新增方法：推荐系统专用 ===

    // 查找所有活跃活动（不排序，供推荐系统使用）
    @Select("SELECT * FROM activity WHERE status = 1")
    List<Activity> findActiveActivities();

    // 根据标签查找活动
    @Select("SELECT * FROM activity WHERE status = 1 AND tags LIKE CONCAT('%', #{tag}, '%')")
    List<Activity> findByTag(String tag);

    // 查找热门活动（按参与人数排序）
    @Select("SELECT * FROM activity WHERE status = 1 ORDER BY current_participants DESC LIMIT #{limit}")
    List<Activity> findPopularActivities(@Param("limit") int limit);

    // 查找最新活动
    @Select("SELECT * FROM activity WHERE status = 1 ORDER BY created_at DESC LIMIT #{limit}")
    List<Activity> findRecentActivities(@Param("limit") int limit);

    // 根据多个标签查找活动（用于标签匹配推荐）
    @Select("<script>" +
            "SELECT * FROM activity WHERE status = 1 AND (" +
            "<foreach collection='tags' item='tag' separator=' OR '>" +
            "tags LIKE CONCAT('%', #{tag}, '%')" +
            "</foreach>" +
            ") ORDER BY created_at DESC" +
            "</script>")
    List<Activity> findByMultipleTags(@Param("tags") List<String> tags);

    // 更新活动参与人数
    @Update("UPDATE activity SET current_participants = #{count} WHERE id = #{activityId}")
    int updateParticipantCount(@Param("activityId") Long activityId, @Param("count") int count);

    // === 新增方法：数据监控专用 ===

    // 统计活动类型分布
    @Select("SELECT activity_type, COUNT(*) as count FROM activity WHERE status = 1 GROUP BY activity_type")
    @MapKey("activity_type")
    Map<String, Map<String, Object>> getActivityTypeDistribution();

    // 获取所有活动（分页，管理员用）
    @Select("SELECT * FROM activity ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Activity> selectAllActivitiesWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    // 统计所有活动数量
    @Select("SELECT COUNT(*) FROM activity")
    int countAllActivities();

    // 下架活动
    @Update("UPDATE activity SET status = 0 WHERE id = #{activityId}")
    int takeDownActivity(Long activityId);

    // 恢复活动
    @Update("UPDATE activity SET status = 1 WHERE id = #{activityId}")
    int restoreActivity(Long activityId);
}