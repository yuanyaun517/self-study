package cn.only.hw.secondmarketserver.dto;

import java.io.Serializable;
import java.util.Date;

public class ChatSessionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer goodsId;
    private String lastMessage;
    private Date lastMessageTime;
    private Integer unreadCount;
    private Integer otherUserId;
    private String otherUserName;
    private String otherUserIcon;
    private String otherUserCollege;
    private String goodsName;
    private String goodsIcon;
    private Double goodsPrice;
    private String goodsStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Integer otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserIcon() {
        return otherUserIcon;
    }

    public void setOtherUserIcon(String otherUserIcon) {
        this.otherUserIcon = otherUserIcon;
    }

    public String getOtherUserCollege() {
        return otherUserCollege;
    }

    public void setOtherUserCollege(String otherUserCollege) {
        this.otherUserCollege = otherUserCollege;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsIcon() {
        return goodsIcon;
    }

    public void setGoodsIcon(String goodsIcon) {
        this.goodsIcon = goodsIcon;
    }

    public Double getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(Double goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public String getGoodsStatus() {
        return goodsStatus;
    }

    public void setGoodsStatus(String goodsStatus) {
        this.goodsStatus = goodsStatus;
    }
}
