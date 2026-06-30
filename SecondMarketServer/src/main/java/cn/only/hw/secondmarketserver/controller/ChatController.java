package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.dto.ChatMessageDto;
import cn.only.hw.secondmarketserver.dto.ChatOpenSessionDto;
import cn.only.hw.secondmarketserver.dto.ChatRoomDto;
import cn.only.hw.secondmarketserver.dto.ChatSendMessageDto;
import cn.only.hw.secondmarketserver.dto.ChatSessionDto;
import cn.only.hw.secondmarketserver.entity.ChatMessage;
import cn.only.hw.secondmarketserver.entity.ChatSession;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.ChatMessageService;
import cn.only.hw.secondmarketserver.service.ChatSessionService;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.service.UserService;
import cn.only.hw.secondmarketserver.util.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天控制器
 * 处理用户聊天相关的HTTP请求，包括会话管理、消息发送等功能
 */
@RestController
@RequestMapping("/chat")
@Slf4j
@Api(tags = "Chat")
public class ChatController {

    /**
     * 聊天会话服务
     */
    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 聊天消息服务
     */
    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 商品服务
     */
    @Autowired
    private GoodsService goodsService;

    @ApiOperation("Open chat session")
    @PostMapping("/session/open")
    public Result<ChatSessionDto> openSession(@RequestBody ChatOpenSessionDto dto) {
        log.info("open chat session: {}", dto);
        try {
            if (dto == null) {
                return Result.error("Chat session data required");
            }
            ChatSession session = chatSessionService.openSession(dto.getUserId(), dto.getTargetUserId(), dto.getGoodsId());
            return Result.success(buildSessionDto(session, dto.getUserId()));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("open chat session failed", e);
            return Result.error("Open chat session failed");
        }
    }

    @ApiOperation("List chat sessions by user id")
    @PostMapping("/session/list")
    public Result<List<ChatSessionDto>> listSessions(Integer userid) {
        log.info("list chat sessions, userId={}", userid);
        try {
            List<ChatSession> sessionList = chatSessionService.listByUserId(userid);
            List<ChatSessionDto> result = sessionList.stream()
                    .map(item -> buildSessionDto(item, userid))
                    .collect(Collectors.toList());
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("list chat sessions failed", e);
            return Result.error("Load chat sessions failed");
        }
    }

    @ApiOperation("Get chat room messages")
    @PostMapping("/message/list")
    public Result<ChatRoomDto> listMessages(Long sessionId, Integer userid) {
        log.info("list chat messages, sessionId={}, userId={}", sessionId, userid);
        try {
            ChatSession session = chatSessionService.requireParticipantSession(sessionId, userid);
            chatMessageService.markSessionMessagesRead(session, userid);
            ChatSession latestSession = chatSessionService.getById(session.getId());
            List<ChatMessage> messages = chatMessageService.listSessionMessages(session.getId());

            ChatRoomDto roomDto = new ChatRoomDto();
            roomDto.setSession(buildSessionDto(latestSession, userid));
            roomDto.setMessages(messages == null
                    ? Collections.emptyList()
                    : messages.stream().map(item -> buildMessageDto(item, userid)).collect(Collectors.toList()));
            return Result.success(roomDto);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("list chat messages failed", e);
            return Result.error("Load chat messages failed");
        }
    }

    @ApiOperation("Send chat message")
    @PostMapping("/message/send")
    public Result<ChatMessageDto> sendMessage(@RequestBody ChatSendMessageDto dto) {
        log.info("send chat message: {}", dto);
        try {
            if (dto == null) {
                return Result.error("Chat message data required");
            }

            ChatSession session = chatSessionService.requireParticipantSession(dto.getSessionId(), dto.getSenderId());
            ChatMessage message = chatMessageService.sendMessage(session, dto.getSenderId(), dto.getMessageType(), dto.getContent());
            return Result.success(buildMessageDto(message, dto.getSenderId()));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("send chat message failed", e);
            return Result.error("Send chat message failed");
        }
    }

    private ChatSessionDto buildSessionDto(ChatSession session, Integer currentUserId) {
        ChatSessionDto dto = new ChatSessionDto();
        if (session == null) {
            return dto;
        }

        BeanUtils.copyProperties(session, dto);
        dto.setUnreadCount(chatSessionService.getUnreadCount(session, currentUserId));

        Integer otherUserId = chatSessionService.getOtherUserId(session, currentUserId);
        dto.setOtherUserId(otherUserId);

        User otherUser = otherUserId == null ? null : userService.getById(otherUserId);
        dto.setOtherUserName(resolveDisplayName(otherUser));
        dto.setOtherUserIcon(otherUser == null ? "" : otherUser.getIcon());
        dto.setOtherUserCollege(otherUser == null ? "" : otherUser.getCollege());

        Goods goods = session.getGoodsId() == null ? null : goodsService.getById(session.getGoodsId());
        if (goods != null) {
            dto.setGoodsName(goods.getName());
            dto.setGoodsIcon(resolveGoodsIcon(goods));
            dto.setGoodsPrice(goods.getPrice());
            dto.setGoodsStatus(goods.getStatus());
        }

        if (!StringUtils.hasText(dto.getLastMessage())) {
            dto.setLastMessage("点击开始聊天");
        }
        return dto;
    }

    private ChatMessageDto buildMessageDto(ChatMessage message, Integer currentUserId) {
        ChatMessageDto dto = new ChatMessageDto();
        if (message == null) {
            return dto;
        }
        BeanUtils.copyProperties(message, dto);
        dto.setMine(currentUserId != null && currentUserId.equals(message.getSenderId()));
        return dto;
    }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return "校园用户";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getAccount())) {
            return user.getAccount().trim();
        }
        return "校园用户";
    }

    private String resolveGoodsIcon(Goods goods) {
        if (goods == null) {
            return "";
        }
        if (StringUtils.hasText(goods.getIcon())) {
            return goods.getIcon().trim();
        }
        if (StringUtils.hasText(goods.getImgs())) {
            return goods.getImgs().split(",")[0].trim();
        }
        return "";
    }
}
