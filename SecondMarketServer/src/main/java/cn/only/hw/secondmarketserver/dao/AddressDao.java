package cn.only.hw.secondmarketserver.dao;

import cn.only.hw.secondmarketserver.entity.Address;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * (Address)表数据库访问层
 *

 */
 @Mapper
public interface AddressDao extends BaseMapper<Address> {

}

