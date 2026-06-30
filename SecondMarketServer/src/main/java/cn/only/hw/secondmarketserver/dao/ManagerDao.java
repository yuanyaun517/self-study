package cn.only.hw.secondmarketserver.dao;

import cn.only.hw.secondmarketserver.entity.Manager;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * (Manager)表数据库访问层
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
 @Mapper
public interface ManagerDao extends BaseMapper<Manager> {

}

