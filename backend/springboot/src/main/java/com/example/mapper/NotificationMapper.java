package com.example.mapper;

import com.example.entity.Notification;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface NotificationMapper {
    int insert(Notification notification);
    List<Notification> selectAll(Notification notification);

    @Update("update `notification` set is_read = true where id = #{id}")
    int markRead(Integer id);

    @Update("update `notification` set is_read = true where user_id = #{userId}")
    int markAllRead(Integer userId);

    int insertInteraction(@Param("userId") Integer userId,
                          @Param("actorId") Integer actorId,
                          @Param("type") String type,
                          @Param("targetType") String targetType,
                          @Param("targetId") Integer targetId,
                          @Param("content") String content,
                          @Param("time") String time);
}
