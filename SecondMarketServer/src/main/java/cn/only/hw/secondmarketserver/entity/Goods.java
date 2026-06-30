package cn.only.hw.secondmarketserver.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 商品实体类
 * 用于存储和管理校园二手市场中的商品信息
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@ApiModel("Goods")
public class Goods implements Serializable {
    private static final long serialVersionUID = -84998824855087645L;
    /**
     * 商品名称
     */
    @ApiModelProperty("商品名称")    
    private String name;
    /**
     * 类别
     */
    @ApiModelProperty("类别")    
    private String type;
    /**
     * 价格
     */
    @ApiModelProperty("价格")    
    private Double price;
    /**
     * 商品数量
     */
    @ApiModelProperty("鍟嗗搧鏁伴噺")
    private Integer number;
    /**
     * 成色
     */
    @ApiModelProperty("成色")    
    private String status;
    /**
     * 交易方式
     */
    @ApiModelProperty("交易方式")    
    private String dealtypy;
    /**
     * 发布者
     */
    @ApiModelProperty("发布者")    
    private Integer sendUser;
    /**
     * 联系方式
     */
    @ApiModelProperty("联系方式")    
    private String contactWay;
    /**
     * 发布时间
     */
    @ApiModelProperty("发布时间")
    @TableField(fill = FieldFill.INSERT)
    private Date sendTime;
    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 商品ID（主键）
     */
    private Integer id;

    /**
     * 分类ID（非数据库字段）
     */
    @TableField(exist = false)
    private Integer cateid;

    /**
     * 卖家电话（非数据库字段）
     */
    @TableField(exist = false)
    private String sellerTel;
    /**
     * 图片
     */
    @ApiModelProperty("图片")    
    private String imgs;
    /**
     * 描述
     */
    @ApiModelProperty("描述")    
    private String describes;
    /**
     * 商品图标
     */
    @ApiModelProperty("商品图标")    
    private String icon;
    /**
     * 管理员对商品操作:0发布 1管理员审核通过 2管理员审核不通过  3删除
     */
    @ApiModelProperty("管理员对商品操作:0发布 1管理员审核通过 2管理员审核不通过  3删除")    
    private String manage;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDealtypy() {
        return dealtypy;
    }

    public void setDealtypy(String dealtypy) {
        this.dealtypy = dealtypy;
    }

    public Integer getSendUser() {
        return sendUser;
    }

    public void setSendUser(Integer sendUser) {
        this.sendUser = sendUser;
    }

    public String getContactWay() {
        return contactWay;
    }

    public void setContactWay(String contactWay) {
        this.contactWay = contactWay;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCateid() {
        return cateid;
    }

    public void setCateid(Integer cateid) {
        this.cateid = cateid;
    }

    public String getSellerTel() {
        return sellerTel;
    }

    public void setSellerTel(String sellerTel) {
        this.sellerTel = sellerTel;
    }

    public String getImgs() {
        return imgs;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public String getDescribes() {
        return describes;
    }

    public void setDescribes(String describes) {
        this.describes = describes;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getManage() {
        return manage;
    }

    public void setManage(String manage) {
        this.manage = manage;
    }

}

