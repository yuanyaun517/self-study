package cn.only.hw.secondmarketserver.service;

import cn.only.hw.secondmarketserver.entity.ChatSession;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

public interface ChatSessionService extends IService<ChatSession> {

    ChatSession openSession(Integer userId, Integer targetUserId, Integer goodsId);

    List<ChatSession> listByUserId(Integer userId);

    ChatSession requireParticipantSession(Long sessionId, Integer userId);

    Integer getOtherUserId(ChatSession session, Integer userId);

    Integer getUnreadCount(ChatSession session, Integer userId);

    void recordNewMessage(ChatSession session, Integer receiverId, String lastMessage, Date lastMessageTime);

    void clearUnreadCount(ChatSession session, Integer userId);
}
