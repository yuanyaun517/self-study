package cn.only.hw.secondmarketserver.entity;

import java.time.LocalDateTime;
import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 轮播图实体类
 * 用于存储和管理首页轮播图信息
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@ApiModel("Banner")
public class Banner implements Serializable {
    private static final long serialVersionUID = 184266966213713373L;
    /**
     * 轮播图ID（主键）
     */
    private Integer id;
    /**
     * 图片地址
     */
    @ApiModelProperty("图片地址")    
    private String imgUrl;
    /**
     * 发布者
     */
    @ApiModelProperty("发布者")    
    private String sendUser;
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


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSendUser() {
        return sendUser;
    }

    public void setSendUser(String sendUser) {
        this.sendUser = sendUser;
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
}

