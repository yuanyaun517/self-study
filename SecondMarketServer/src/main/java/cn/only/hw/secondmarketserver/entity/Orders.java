package cn.only.hw.secondmarketserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单实体类
 * 用于存储和管理校园二手市场中的订单信息
 */
@ApiModel("Orders")
public class Orders implements Serializable {
    private static final long serialVersionUID = -78662925882250114L;

    /**
     * 订单ID（主键）
     */
    private Integer id;

    @ApiModelProperty("下单用户ID")
    private Integer userid;

    @ApiModelProperty("商品ID")
    private Integer goodsid;

    @ApiModelProperty("下单时间")
    @TableField(fill = FieldFill.INSERT)
    private Date sendTime;

    @ApiModelProperty("订单金额")
    private Double price;

    @ApiModelProperty("购买数量")
    private Integer number;

    @ApiModelProperty("订单状态")
    private String state;

    @ApiModelProperty("订单评分")
    private Integer rating;

    @ApiModelProperty("订单评价内容")
    private String reviewContent;

    @ApiModelProperty("物流单号")
    private String logistics;

    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty("收货地址ID")
    private Integer addressid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getGoodsid() {
        return goodsid;
    }

    public void setGoodsid(Integer goodsid) {
        this.goodsid = goodsid;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public String getLogistics() {
        return logistics;
    }

    public void setLogistics(String logistics) {
        this.logistics = logistics;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getAddressid() {
        return addressid;
    }

    public void setAddressid(Integer addressid) {
        this.addressid = addressid;
    }
}
