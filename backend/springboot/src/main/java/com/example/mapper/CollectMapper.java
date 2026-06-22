package com.example.mapper;

import com.example.entity.Collect;
import com.example.entity.Items;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作collect相关数据接口
*/
public interface CollectMapper {

    /**
      * 新增
    */
    int insert(Collect collect);

    /**
      * 删除
    */
    @Delete("delete from `collect` where id = #{id}")
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Collect collect);

    /**
      * 根据ID查询
    */
    @Select("select * from `collect` where id = #{id}")
    Collect selectById(Integer id);

    /**
      * 查询所有
    */
    List<Collect> selectAll(Collect collect);

    @Select("select * from `collect` where item_id = #{itemId} and user_id = #{userId}")
    Collect selectByItemIdAndUserId(@Param("itemId") Integer itemId, @Param("userId") Integer userId);
}