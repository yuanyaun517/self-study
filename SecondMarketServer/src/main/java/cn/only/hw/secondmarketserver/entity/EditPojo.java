package cn.only.hw.secondmarketserver.entity;

import lombok.Data;

/**
 * 编辑数据传输对象
 * 用于封装前端传递的编辑字段信息
 *
 * @author 李淑娟
 * @since 2026/1/20
 */

@Data
public class EditPojo {
    /**
     * 字段名
     */
    private String field;
    
    /**
     * 字段值
     */
    private String val;
    
    /**
     * 记录ID
     */
    private String id;
}
