package cn.only.hw.secondmarketserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息实体类
 * 用于存储和管理用户之间的聊天消息
 */
@ApiModel("ChatMessage")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID（主键）
     */
    @ApiModelProperty("Message id")
    private Long id;

    @ApiModelProperty("Session id")
    private Long sessionId;

    @ApiModelProperty("Sender user id")
    private Integer senderId;

    @ApiModelProperty("Receiver user id")
    private Integer receiverId;

    @ApiModelProperty("Message type")
    private String messageType;

    @ApiModelProperty("Message content")
    private String content;

    @ApiModelProperty("Read status")
    private Integer isRead;

    @ApiModelProperty("Read time")
    private Date readTime;

    @ApiModelProperty("Send time")
    @TableField(fill = FieldFill.INSERT)
    private Date sendTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }
}
