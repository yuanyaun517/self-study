package cn.only.hw.secondmarketserver.dto;

import java.io.Serializable;

public class ChatOpenSessionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;
    private Integer targetUserId;
    private Integer goodsId;

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
}
