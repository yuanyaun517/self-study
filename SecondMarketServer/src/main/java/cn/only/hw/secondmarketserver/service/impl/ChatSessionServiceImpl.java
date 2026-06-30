package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.dao.ChatSessionDao;
import cn.only.hw.secondmarketserver.entity.ChatSession;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.ChatSessionService;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionDao, ChatSession> implements ChatSessionService {

    @Autowired
    private UserService userService;

    @Autowired
    private GoodsService goodsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSession openSession(Integer userId, Integer targetUserId, Integer goodsId) {
        validateOpenSessionRequest(userId, targetUserId, goodsId);

        ChatSession existing = findSession(userId, targetUserId, goodsId);
        if (existing != null) {
            return existing;
        }

        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTargetUserId(targetUserId);
        session.setGoodsId(goodsId);
        session.setUnreadCountUser(0);
        session.setUnreadCountTarget(0);
        this.save(session);
        return this.getById(session.getId());
    }

    @Override
    public List<ChatSession> listByUserId(Integer userId) {
        validateUserId(userId, "User id required");

        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, userId)
                .or()
                .eq(ChatSession::getTargetUserId, userId)
                .orderByDesc(ChatSession::getLastMessageTime)
                .orderByDesc(ChatSession::getUpdateTime)
                .orderByDesc(ChatSession::getId);
        return this.list(queryWrapper);
    }

    @Override
    public ChatSession requireParticipantSession(Long sessionId, Integer userId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session id required");
        }
        validateUserId(userId, "User id required");

        ChatSession session = this.getById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Chat session not found");
        }
        if (!userId.equals(session.getUserId()) && !userId.equals(session.getTargetUserId())) {
            throw new IllegalArgumentException("Current user cannot access this chat session");
        }
        return session;
    }

    @Override
    public Integer getOtherUserId(ChatSession session, Integer userId) {
        ChatSession checkedSession = requireParticipantSession(session == null ? null : session.getId(), userId);
        return userId.equals(checkedSession.getUserId())
                ? checkedSession.getTargetUserId()
                : checkedSession.getUserId();
    }

    @Override
    public Integer getUnreadCount(ChatSession session, Integer userId) {
        ChatSession checkedSession = requireParticipantSession(session == null ? null : session.getId(), userId);
        Integer unreadCount = userId.equals(checkedSession.getUserId())
                ? checkedSession.getUnreadCountUser()
                : checkedSession.getUnreadCountTarget();
        return unreadCount == null || unreadCount < 0 ? 0 : unreadCount;
    }

    @Override
    public void recordNewMessage(ChatSession session, Integer receiverId, String lastMessage, Date lastMessageTime) {
        if (session == null || session.getId() == null) {
            throw new IllegalArgumentException("Chat session not found");
        }
        if (receiverId == null) {
            throw new IllegalArgumentException("Receiver id required");
        }

        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getId, session.getId())
                .set(ChatSession::getLastMessage, lastMessage)
                .set(ChatSession::getLastMessageTime, lastMessageTime);

        if (receiverId.equals(session.getUserId())) {
            updateWrapper.setSql("unread_count_user = COALESCE(unread_count_user, 0) + 1");
        } else if (receiverId.equals(session.getTargetUserId())) {
            updateWrapper.setSql("unread_count_target = COALESCE(unread_count_target, 0) + 1");
        } else {
            throw new IllegalArgumentException("Receiver is not in this chat session");
        }

        this.update(updateWrapper);
    }

    @Override
    public void clearUnreadCount(ChatSession session, Integer userId) {
        ChatSession checkedSession = requireParticipantSession(session == null ? null : session.getId(), userId);

        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getId, checkedSession.getId());
        if (userId.equals(checkedSession.getUserId())) {
            updateWrapper.set(ChatSession::getUnreadCountUser, 0);
        } else {
            updateWrapper.set(ChatSession::getUnreadCountTarget, 0);
        }
        this.update(updateWrapper);
    }

    private void validateOpenSessionRequest(Integer userId, Integer targetUserId, Integer goodsId) {
        validateUserId(userId, "User id required");
        validateUserId(targetUserId, "Target user id required");

        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot chat with yourself");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Current user not found");
        }

        User targetUser = userService.getById(targetUserId);
        if (targetUser == null) {
            throw new IllegalArgumentException("Target user not found");
        }

        if (goodsId != null) {
            Goods goods = goodsService.getById(goodsId);
            if (goods == null) {
                throw new IllegalArgumentException("Goods not found");
            }
            if (goods.getSendUser() == null || !targetUserId.equals(goods.getSendUser())) {
                throw new IllegalArgumentException("Target user is not the publisher of this goods");
            }
        }
    }

    private void validateUserId(Integer userId, String message) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private ChatSession findSession(Integer userId, Integer targetUserId, Integer goodsId) {
        ChatSession directSession = this.getOne(buildPairQuery(userId, targetUserId, goodsId), false);
        if (directSession != null) {
            return directSession;
        }
        return this.getOne(buildPairQuery(targetUserId, userId, goodsId), false);
    }

    private LambdaQueryWrapper<ChatSession> buildPairQuery(Integer userId, Integer targetUserId, Integer goodsId) {
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getTargetUserId, targetUserId);

        if (goodsId == null) {
            queryWrapper.isNull(ChatSession::getGoodsId);
        } else {
            queryWrapper.eq(ChatSession::getGoodsId, goodsId);
        }
        queryWrapper.last("LIMIT 1");
        return queryWrapper;
    }
}
