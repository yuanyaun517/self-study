package cn.only.hw.secondmarketserver.dao;

import cn.only.hw.secondmarketserver.entity.ChatSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionDao extends BaseMapper<ChatSession> {
}
