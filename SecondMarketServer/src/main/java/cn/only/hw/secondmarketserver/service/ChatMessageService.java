package cn.only.hw.secondmarketserver.service;

import cn.only.hw.secondmarketserver.entity.ChatMessage;
import cn.only.hw.secondmarketserver.entity.ChatSession;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {

    List<ChatMessage> listSessionMessages(Long sessionId);

    ChatMessage sendMessage(ChatSession session, Integer senderId, String messageType, String content);

    void markSessionMessagesRead(ChatSession session, Integer receiverId);
}
