package cn.only.hw.secondmarketserver.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 子分类实体类
 * 用于存储和管理商品分类的子分类信息
 */
@TableName("catechild")
@ApiModel("Catechild")
public class Catechild implements Serializable {
    private static final long serialVersionUID = 853544861278497715L;

    /**
     * 子分类ID（主键）
     */
    @TableId
    private Integer childid;

    /**
     * 父分类ID
     */
    private Integer cateid;

    /**
     * 商品ID
     */
    private String goodid;

    /**
     * 子分类图片
     */
    private String image;

    /**
     * 子分类名称
     */
    private String childname;


    public Integer getChildid() {
        return childid;
    }

    public void setChildid(Integer childid) {
        this.childid = childid;
    }

    public Integer getCateid() {
        return cateid;
    }

    public void setCateid(Integer cateid) {
        this.cateid = cateid;
    }

    public String getGoodid() {
        return goodid;
    }

    public void setGoodid(String goodid) {
        this.goodid = goodid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getChildname() {
        return childname;
    }

    public void setChildname(String childname) {
        this.childname = childname;
    }

}

