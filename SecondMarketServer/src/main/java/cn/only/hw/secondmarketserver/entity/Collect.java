package cn.only.hw.secondmarketserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 收藏实体类
 * 用于存储和管理用户收藏的商品和帖子信息
 */
@ApiModel("Collect")
public class Collect implements Serializable {
    private static final long serialVersionUID = -51898547986564885L;

    /**
     * 收藏ID（主键）
     */
    private Integer id;

    @ApiModelProperty("收藏类型(0商品 1帖子)")
    private String type;

    @ApiModelProperty("用户id")
    @TableField("user")
    private Integer userid;

    @ApiModelProperty("帖子或商品id")
    private Integer sid;

    @ApiModelProperty("收藏时间")
    @TableField(value = "c_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty("收藏时间")
    @TableField(exist = false)
    private Date sendTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }
}
