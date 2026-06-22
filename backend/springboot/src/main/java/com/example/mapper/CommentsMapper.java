package com.example.mapper;

import com.example.entity.Comments;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作comments相关数据接口
*/
public interface CommentsMapper {

    /**
      * 新增
    */
    int insert(Comments comments);

    /**
      * 删除
    */
    @Delete("delete from `comments` where id = #{id}")
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Comments comments);

    /**
      * 根据ID查询
    */
    @Select("select * from `comments` where id = #{id}")
    Comments selectById(Integer id);

    /**
      * 查询所有
    */
    List<Comments> selectAll(Comments comments);


}