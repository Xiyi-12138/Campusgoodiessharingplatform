package com.example.mapper;

import com.example.entity.Charge;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作charge相关数据接口
*/
public interface ChargeMapper {

    /**
      * 新增
    */
    int insert(Charge charge);

    /**
      * 删除
    */
    @Delete("delete from `charge` where id = #{id}")
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Charge charge);

    /**
      * 根据ID查询
    */
    @Select("select * from `charge` where id = #{id}")
    Charge selectById(Integer id);

    /**
      * 查询所有
    */
    List<Charge> selectAll(Charge charge);


}