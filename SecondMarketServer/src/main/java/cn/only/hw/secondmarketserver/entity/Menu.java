package cn.only.hw.secondmarketserver.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 菜单实体类
 * 用于存储和管理系统菜单信息
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@ApiModel("菜单")
public class Menu implements Serializable {
    private static final long serialVersionUID = -96867622369176793L;
    /**
     * 菜单ID（主键）
     */
    private Integer id;
    /**
     * 图标地址
     */
    @ApiModelProperty("图标地址")    
    private String icon;
    /**
     * 菜单名称
     */
    @ApiModelProperty("菜单名称")    
    private String name;
    /**
     * 跳转地址
     */
    @ApiModelProperty("跳转地址")    
    private String url;
    /**
     * 顺序
     */
    @ApiModelProperty("顺序")    
    private String sort;
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

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
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

