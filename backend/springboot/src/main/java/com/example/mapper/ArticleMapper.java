package com.example.mapper;

import com.example.entity.Article;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleMapper {
    int insert(Article article);

    @Delete("delete from `article` where id = #{id}")
    int deleteById(Integer id);

    int updateById(Article article);

    @Select("select article.*, user.name as userName, user.avatar as avatar, " +
            "(select count(*) from `likes` where likes.article_id = article.id) as likeCount, " +
            "(select count(*) from `comments` where comments.article_id = article.id) as commentCount, " +
            "(select l.id from `likes` l where l.article_id = article.id and l.user_id = #{loginUserId} limit 1) as likedId " +
            "from `article` left join user on article.user_id = user.id where article.id = #{id}")
    Article selectById(@Param("id") Integer id, @Param("loginUserId") Integer loginUserId);

    List<Article> selectAll(Article article);
}
