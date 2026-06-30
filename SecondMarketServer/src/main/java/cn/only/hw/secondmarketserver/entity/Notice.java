package cn.only.hw.secondmarketserver.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 公告通知实体类
 * 用于存储和管理系统公告和通知信息
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@ApiModel("Notice")
public class Notice implements Serializable {
    private static final long serialVersionUID = 125753130324006487L;
    /**
     * 公告ID（主键）
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
     * 类型: (公告1 通知0)
     */
    @ApiModelProperty("类型: (公告1 通知0)")
    private String type;
    /**
     * 发布者
     */
    @ApiModelProperty("发布者")    
    private Integer sendUser;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}

