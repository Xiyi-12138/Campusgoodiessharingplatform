package com.example.mapper;

import com.example.entity.Likes;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LikesMapper {
    int insert(Likes likes);

    @Delete("delete from `likes` where id = #{id}")
    int deleteById(Integer id);

    int updateById(Likes likes);

    @Select("select * from `likes` where id = #{id}")
    Likes selectById(Integer id);

    @Select("select * from `likes` where article_id = #{articleId} and user_id = #{userId} limit 1")
    Likes selectByArticleIdAndUserId(@Param("articleId") Integer articleId, @Param("userId") Integer userId);

    List<Likes> selectAll(Likes likes);
}
