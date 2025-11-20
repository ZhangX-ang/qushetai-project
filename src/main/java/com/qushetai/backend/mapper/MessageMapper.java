package com.qushetai.backend.mapper;

import com.qushetai.backend.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper {

    // 插入消息
    @Insert("INSERT INTO message (sender_id, receiver_id, title, content, message_type, related_type, related_id) " +
            "VALUES (#{senderId}, #{receiverId}, #{title}, #{content}, #{messageType}, #{relatedType}, #{relatedId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);

    // 根据ID查询消息
    @Select("SELECT * FROM message WHERE id = #{id} AND is_deleted = 0")
    Message findById(Long id);

    // 查询用户的消息列表（分页）
    @Select("SELECT * FROM message WHERE receiver_id = #{receiverId} AND is_deleted = 0 ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Message> findByReceiverWithPagination(@Param("receiverId") Long receiverId,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit);

    // 查询用户的未读消息
    @Select("SELECT * FROM message WHERE receiver_id = #{receiverId} AND is_read = 0 AND is_deleted = 0 ORDER BY created_at DESC")
    List<Message> findUnreadByReceiver(Long receiverId);

    // 统计用户的消息数量
    @Select("SELECT COUNT(*) FROM message WHERE receiver_id = #{receiverId} AND is_deleted = 0")
    int countByReceiver(Long receiverId);

    // 统计用户的未读消息数量
    @Select("SELECT COUNT(*) FROM message WHERE receiver_id = #{receiverId} AND is_read = 0 AND is_deleted = 0")
    int countUnreadByReceiver(Long receiverId);

    // 标记消息为已读
    @Update("UPDATE message SET is_read = 1, read_at = CURRENT_TIMESTAMP WHERE id = #{id} AND receiver_id = #{receiverId}")
    int markAsRead(@Param("id") Long id, @Param("receiverId") Long receiverId);

    // 批量标记消息为已读
    @Update("UPDATE message SET is_read = 1, read_at = CURRENT_TIMESTAMP WHERE receiver_id = #{receiverId} AND is_read = 0")
    int markAllAsRead(Long receiverId);

    // 软删除消息
    @Update("UPDATE message SET is_deleted = 1 WHERE id = #{id} AND receiver_id = #{receiverId}")
    int deleteMessage(@Param("id") Long id, @Param("receiverId") Long receiverId);

    // 获取消息类型统计
    @Select("SELECT message_type, COUNT(*) as count FROM message WHERE receiver_id = #{receiverId} AND is_deleted = 0 GROUP BY message_type")
    @MapKey("message_type")
    Map<Integer, Map<String, Object>> getMessageTypeStats(Long receiverId);

    // 系统方法：发送活动通知（当有人对活动感兴趣时调用）
    @Insert("INSERT INTO message (sender_id, receiver_id, title, content, message_type, related_type, related_id) " +
            "VALUES (0, #{receiverId}, #{title}, #{content}, 2, 1, #{activityId})")
    int sendActivityNotification(@Param("receiverId") Long receiverId,
                                 @Param("title") String title,
                                 @Param("content") String content,
                                 @Param("activityId") Long activityId);
}