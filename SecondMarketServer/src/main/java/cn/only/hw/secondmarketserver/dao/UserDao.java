package cn.only.hw.secondmarketserver.dao;

import cn.only.hw.secondmarketserver.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * (User)表数据库访问层
 *
 */
 @Mapper
public interface UserDao extends BaseMapper<User> {

}

