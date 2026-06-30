package cn.only.hw.secondmarketserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天会话实体类
 * 用于存储和管理用户之间的聊天会话信息
 */
@ApiModel("ChatSession")
public class ChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（主键）
     */
    @ApiModelProperty("Session id")
    private Long id;

    @ApiModelProperty("Initiator user id")
    private Integer userId;

    @ApiModelProperty("Target user id")
    private Integer targetUserId;

    @ApiModelProperty("Related goods id")
    private Integer goodsId;

    @ApiModelProperty("Last message preview")
    private String lastMessage;

    @ApiModelProperty("Last message time")
    private Date lastMessageTime;

    @ApiModelProperty("Unread count for initiator")
    private Integer unreadCountUser;

    @ApiModelProperty("Unread count for target user")
    private Integer unreadCountTarget;

    @ApiModelProperty("Create time")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty("Update time")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Integer targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Integer getUnreadCountUser() {
        return unreadCountUser;
    }

    public void setUnreadCountUser(Integer unreadCountUser) {
        this.unreadCountUser = unreadCountUser;
    }

    public Integer getUnreadCountTarget() {
        return unreadCountTarget;
    }

    public void setUnreadCountTarget(Integer unreadCountTarget) {
        this.unreadCountTarget = unreadCountTarget;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
