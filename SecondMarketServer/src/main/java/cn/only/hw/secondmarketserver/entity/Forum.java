package cn.only.hw.secondmarketserver.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 论坛帖子实体类
 * 用于存储和管理校园二手市场中的论坛帖子信息
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@ApiModel("Forum")
public class Forum implements Serializable {
    private static final long serialVersionUID = 709716011969610210L;
    /**
     * 帖子ID（主键）
     */
    private Integer id;
    /**
     * 标题
     */
    @ApiModelProperty("标题")    
    private String title;
    /**
     * 内容
     */
    @ApiModelProperty("内容")    
    private String content;
    /**
     * 图片
     */
    @ApiModelProperty("图片")    
    private String imgs;
    /**
     * 发布用户
     */
    @ApiModelProperty("发布用户")    
    private Integer sendUser;
    /**
     * 发布时间
     */
    @ApiModelProperty("发布时间")
    @TableField(fill = FieldFill.INSERT)
    private Date sendTime;
    /**
     * 过审时间
     */
    @ApiModelProperty("过审时间")    
    private Date passTime;
    /**
     * 图标
     */
    @ApiModelProperty("图标")    
    private String icon;
    /**
     * 类型
     */
    @ApiModelProperty("类型")    
    private String type;
    /**
     * 管理员操作(0发布 1审核通过 2审核不通过)
     */
    @ApiModelProperty("管理员操作(0发布 1审核通过 2审核不通过)")    
    private String manage;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgs() {
        return imgs;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public Integer getSendUser() {
        return sendUser;
    }

    public void setSendUser(Integer sendUser) {
        this.sendUser = sendUser;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Date getPassTime() {
        return passTime;
    }

    public void setPassTime(Date passTime) {
        this.passTime = passTime;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getManage() {
        return manage;
    }

    public void setManage(String manage) {
        this.manage = manage;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}

