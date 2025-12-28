package com.qushetai.backend.mapper;

import com.qushetai.backend.entity.Activity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityMapper {

    // 插入新活动 - 修改：表名改为复数 activities
    @Insert("INSERT INTO activities (title, description, activity_type, start_time, end_time, location, " +
            "organizer_id, max_participants, current_participants, status, tags, created_at, updated_at) " +
            "VALUES (#{title}, #{description}, #{activityType}, #{startTime}, #{endTime}, #{location}, " +
            "#{organizerId}, #{maxParticipants}, #{currentParticipants}, #{status}, #{tags}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertActivity(Activity activity);

    // 查询所有活动（按创建时间倒序）- 修改：表名改为复数 activities
    @Select("SELECT * FROM activities WHERE status = 'active' ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "activity_type", property = "activityType"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "organizer_id", property = "organizerId"),
            @Result(column = "max_participants", property = "maxParticipants"),
            @Result(column = "current_participants", property = "currentParticipants"),
            @Result(column = "status", property = "status"),
            @Result(column = "tags", property = "tags"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Activity> selectAllActivities();

    // 分页查询活动 - 修改：表名改为复数 activities
    @Select("SELECT * FROM activities WHERE status = 'active' ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "activity_type", property = "activityType"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "organizer_id", property = "organizerId"),
            @Result(column = "max_participants", property = "maxParticipants"),
            @Result(column = "current_participants", property = "currentParticipants"),
            @Result(column = "status", property = "status"),
            @Result(column = "tags", property = "tags"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Activity> selectActivitiesWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    // 根据ID查询活动 - 修改：表名改为复数 activities
    @Select("SELECT * FROM activities WHERE id = #{id} AND status = 'active'")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "activity_type", property = "activityType"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "organizer_id", property = "organizerId"),
            @Result(column = "max_participants", property = "maxParticipants"),
            @Result(column = "current_participants", property = "currentParticipants"),
            @Result(column = "status", property = "status"),
            @Result(column = "tags", property = "tags"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    Activity selectActivityById(Long id);

    // 根据发起者ID查询活动 - 修改：表名改为复数 activities
    @Select("SELECT * FROM activities WHERE organizer_id = #{organizerId} AND status = 'active' ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "activity_type", property = "activityType"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "organizer_id", property = "organizerId"),
            @Result(column = "max_participants", property = "maxParticipants"),
            @Result(column = "current_participants", property = "currentParticipants"),
            @Result(column = "status", property = "status"),
            @Result(column = "tags", property = "tags"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Activity> selectActivitiesByOrganizer(Long organizerId);

    // 更新活动信息 - 修改：表名改为复数 activities
    @Update("UPDATE activities SET title=#{title}, description=#{description}, activity_type=#{activityType}, " +
            "start_time=#{startTime}, end_time=#{endTime}, location=#{location}, max_participants=#{maxParticipants}, " +
            "current_participants=#{currentParticipants}, tags=#{tags}, updated_at=CURRENT_TIMESTAMP " +
            "WHERE id=#{id}")
    int updateActivity(Activity activity);

    // 软删除活动（将status设为'inactive'）- 修改：表名改为复数 activities
    @Update("UPDATE activities SET status='inactive', updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int deleteActivity(Long id);

    // 统计活动总数 - 修改：表名改为复数 activities
    @Select("SELECT COUNT(*) FROM activities WHERE status = 'active'")
    int countActivities();

    // === 新增方法：推荐系统专用 ===

    // 查找所有活跃活动（不排序，供推荐系统使用）- 修改：表名改为复数 activities
    @Select("SELECT * FROM activities WHERE status = 'active'")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "activity_type", property = "activityType"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "organizer_id", property = "organizerId"),
            @Result(column = "max_participants", property = "maxParticipants"),
            @Result(column = "current_participants", property = "currentParticipants"),
            @Result(column = "status", property = "status"),
            @Result(column = "tags", property = "tags"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Activity> findActiveActivities();

    // 根据标签查找活动 - 修改：表名改为复数 activities
    @Select("SELECT * FROM activities WHERE status = 'active' AND tags LIKE CONCAT('%', #{tag}, '%')")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "activity_type", property = "activityType"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "organizer_id", property = "organizerId"),
            @Result(column = "max_participants", property = "maxParticipants"),
            @Result(column = "current_participants", property = "currentParticipants"),
            @Result(column = "status", property = "status"),
            @Result(column = "tags", property = "tags"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Activity> findByTag(String tag);

    // 查找热门活动（按参与人数排序）- 修改：表名改为复数 activities
    @Select("SELECT * FROM activities WHERE status = 'active' ORDER BY current_participants DESC LIMIT #{limit}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "activity_type", property = "activityType"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "organizer_id", property = "organizerId"),
            @Result(column = "max_participants", property = "maxParticipants"),
            @Result(column = "current_participants", property = "currentParticipants"),
            @Result(column = "status", property = "status"),
            @Result(column = "tags", property = "tags"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Activity> findPopularActivities(@Param("limit") int limit);

    // 查找最新活动 - 修改：表名改为复数 activities
    @Select("SELECT * FROM activities WHERE status = 'active' ORDER BY created_at DESC LIMIT #{limit}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "activity_type", property = "activityType"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "organizer_id", property = "organizerId"),
            @Result(column = "max_participants", property = "maxParticipants"),
            @Result(column = "current_participants", property = "currentParticipants"),
            @Result(column = "status", property = "status"),
            @Result(column = "tags", property = "tags"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Activity> findRecentActivities(@Param("limit") int limit);

    // 根据多个标签查找活动（用于标签匹配推荐）- 修改：表名改为复数 activities
    @Select("<script>" +
            "SELECT * FROM activities WHERE status = 'active' AND (" +
            "<foreach collection='tags' item='tag' separator=' OR '>" +
            "tags LIKE CONCAT('%', #{tag}, '%')" +
            "</foreach>" +
            ") ORDER BY created_at DESC" +
            "</script>")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "activity_type", property = "activityType"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "organizer_id", property = "organizerId"),
            @Result(column = "max_participants", property = "maxParticipants"),
            @Result(column = "current_participants", property = "currentParticipants"),
            @Result(column = "status", property = "status"),
            @Result(column = "tags", property = "tags"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Activity> findByMultipleTags(@Param("tags") List<String> tags);

    // 更新活动参与人数 - 修改：表名改为复数 activities
    @Update("UPDATE activities SET current_participants = #{count} WHERE id = #{activityId}")
    int updateParticipantCount(@Param("activityId") Long activityId, @Param("count") int count);

    // === 新增方法：数据监控专用 ===

    // 统计活动类型分布 - 修改：表名改为复数 activities
    @Select("SELECT activity_type, COUNT(*) as count FROM activities WHERE status = 'active' GROUP BY activity_type")
    @MapKey("activity_type")
    Map<String, Map<String, Object>> getActivityTypeDistribution();

    // 获取所有活动（分页，管理员用）- 修改：表名改为复数 activities
    @Select("SELECT * FROM activities ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "title", property = "title"),
            @Result(column = "description", property = "description"),
            @Result(column = "activity_type", property = "activityType"),
            @Result(column = "start_time", property = "startTime"),
            @Result(column = "end_time", property = "endTime"),
            @Result(column = "location", property = "location"),
            @Result(column = "organizer_id", property = "organizerId"),
            @Result(column = "max_participants", property = "maxParticipants"),
            @Result(column = "current_participants", property = "currentParticipants"),
            @Result(column = "status", property = "status"),
            @Result(column = "tags", property = "tags"),
            @Result(column = "created_at", property = "createdAt"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    List<Activity> selectAllActivitiesWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    // 统计所有活动数量 - 修改：表名改为复数 activities
    @Select("SELECT COUNT(*) FROM activities")
    int countAllActivities();

    // 下架活动 - 修改：表名改为复数 activities
    @Update("UPDATE activities SET status = 'inactive' WHERE id = #{activityId}")
    int takeDownActivity(Long activityId);

    // 恢复活动 - 修改：表名改为复数 activities
    @Update("UPDATE activities SET status = 'active' WHERE id = #{activityId}")
    int restoreActivity(Long activityId);
}