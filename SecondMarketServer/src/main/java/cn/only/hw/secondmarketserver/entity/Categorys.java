package cn.only.hw.secondmarketserver.entity;

import java.util.List;

/**
 * 分类扩展实体类
 * 继承自Category，包含子分类列表信息
 */
public class Categorys extends Category{
    /**
     * 子分类列表
     */
    List<Catechild> children;


    public List<Catechild> getChildren() {
        return children;
    }

    public void setChildren(List<Catechild> children) {
        this.children = children;
    }
}