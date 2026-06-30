package cn.only.hw.secondmarketserver.dao;

import cn.only.hw.secondmarketserver.entity.Cart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * (Cart)表数据库访问层
 *
 */
 @Mapper
public interface CartDao extends BaseMapper<Cart> {

}

