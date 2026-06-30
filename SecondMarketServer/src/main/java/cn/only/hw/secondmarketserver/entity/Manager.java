package cn.only.hw.secondmarketserver.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 管理员实体类
 * 用于存储和管理系统管理员信息
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@ApiModel("Manager")
public class Manager implements Serializable {
    private static final long serialVersionUID = -59889277545640309L;
    
    @ApiModelProperty("管理员ID")
    private Integer id;
    
    @ApiModelProperty("管理员账号")
    private String account;
    
    @ApiModelProperty("管理员密码")
    private String password;

    @ApiModelProperty("管理员头像")
    private String avatar;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}

