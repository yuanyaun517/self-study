package cn.only.hw.secondmarketserver.dao;

import cn.only.hw.secondmarketserver.entity.Notice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * (Notice)表数据库访问层
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
 @Mapper
public interface NoticeDao extends BaseMapper<Notice> {

}

