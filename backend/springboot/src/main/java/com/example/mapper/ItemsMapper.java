package com.example.mapper;

import com.example.entity.Items;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ItemsMapper {
    int insert(Items items);

    @Delete("delete from `items` where id = #{id}")
    int deleteById(Integer id);

    int updateById(Items items);

    @Select("select items.*, user.name as userName, category.name as categoryName, " +
            "(select count(*) from `collect` where collect.item_id = items.id) as collectCount " +
            "from `items` left join user on items.user_id = user.id left join category on items.category_id = category.id where items.id = #{id}")
    Items selectById(Integer id);

    List<Items> selectAll(Items items);
}
