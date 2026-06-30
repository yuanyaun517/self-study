package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.dao.ChatMessageDao;
import cn.only.hw.secondmarketserver.entity.ChatMessage;
import cn.only.hw.secondmarketserver.entity.ChatSession;
import cn.only.hw.secondmarketserver.service.ChatMessageService;
import cn.only.hw.secondmarketserver.service.ChatSessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageDao, ChatMessage> implements ChatMessageService {

    private static final String MESSAGE_TYPE_TEXT = "text";
    private static final int LAST_MESSAGE_MAX_LENGTH = 120;

    @Autowired
    private ChatSessionService chatSessionService;

    @Override
    public List<ChatMessage> listSessionMessages(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session id required");
        }

        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getSendTime)
                .orderByAsc(ChatMessage::getId);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendMessage(ChatSession session, Integer senderId, String messageType, String content) {
        if (session == null || session.getId() == null) {
            throw new IllegalArgumentException("Chat session not found");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("Sender id required");
        }

        String normalizedContent = content == null ? "" : content.trim();
        if (!StringUtils.hasText(normalizedContent)) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        Integer receiverId = resolveReceiverId(session, senderId);
        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageType(normalizeMessageType(messageType));
        message.setContent(normalizedContent);
        message.setIsRead(0);
        this.save(message);

        Date sendTime = message.getSendTime() == null ? new Date() : message.getSendTime();
        chatSessionService.recordNewMessage(
                session,
                receiverId,
                buildLastMessagePreview(message.getMessageType(), normalizedContent),
                sendTime
        );
        return this.getById(message.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSessionMessagesRead(ChatSession session, Integer receiverId) {
        if (session == null || session.getId() == null || receiverId == null) {
            return;
        }

        LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatMessage::getSessionId, session.getId())
                .eq(ChatMessage::getReceiverId, receiverId)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1)
                .set(ChatMessage::getReadTime, new Date());
        this.update(updateWrapper);
        chatSessionService.clearUnreadCount(session, receiverId);
    }

    private Integer resolveReceiverId(ChatSession session, Integer senderId) {
        if (senderId.equals(session.getUserId())) {
            return session.getTargetUserId();
        }
        if (senderId.equals(session.getTargetUserId())) {
            return session.getUserId();
        }
        throw new IllegalArgumentException("Sender is not in this chat session");
    }

    private String normalizeMessageType(String messageType) {
        String normalizedType = messageType == null ? "" : messageType.trim().toLowerCase();
        return StringUtils.hasText(normalizedType) ? normalizedType : MESSAGE_TYPE_TEXT;
    }

    private String buildLastMessagePreview(String messageType, String content) {
        if (!MESSAGE_TYPE_TEXT.equals(messageType)) {
            return "[" + messageType + "]";
        }
        if (content.length() <= LAST_MESSAGE_MAX_LENGTH) {
            return content;
        }
        return content.substring(0, LAST_MESSAGE_MAX_LENGTH) + "...";
    }
}
