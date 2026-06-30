package cn.only.hw.secondmarketserver.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 商品分类实体类
 * 用于存储和管理商品的分类信息
 */
@TableName("category")
@ApiModel("Category")
public class Category implements Serializable {
    private static final long serialVersionUID = -19607011195695563L;
    
    /**
     * 分类ID（主键）
     */
    @TableId
    private Integer cateid;
    
    /**
     * 分类名称
     */
    private String catename;
    
    /**
     * 是否有子分类（非数据库字段）
     */
    @TableField(exist = false)
    private Boolean ishavechild;


    public Integer getCateid() {
        return cateid;
    }

    public void setCateid(Integer cateid) {
        this.cateid = cateid;
    }

    public String getCatename() {
        return catename;
    }

    public void setCatename(String catename) {
        this.catename = catename;
    }

    public Boolean getIshavechild() {
        return ishavechild;
    }

    public void setIshavechild(Boolean ishavechild) {
        this.ishavechild = ishavechild;
    }

}

