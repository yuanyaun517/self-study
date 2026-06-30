package cn.only.hw.secondmarketserver.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * (User)实体类
 *

 */
@ApiModel("User")
public class User implements Serializable {
    private static final long serialVersionUID = -77996028936132306L;
    /**
     * 用户ID（主键）
     */
    private Integer id;
    /**
     * 用户名(账号)
     */
    @ApiModelProperty("用户名(账号)")    
    private String account;
    /**
     * 性别
     */
    @ApiModelProperty("性别")    
    private String sex;
    /**
     * 昵称
     */
    @ApiModelProperty("昵称")    
    private String nickname;
    /**
     * 电话
     */
    @ApiModelProperty("电话")    
    private String tel;
    /**
     * 身份证号
     */
    @ApiModelProperty("身份证号")    
    private String idcard;
    /**
     * 用户密码
     */
    @ApiModelProperty("用户密码")    
    private String password;
    /**
     * 学院
     */
    @ApiModelProperty("学院")    
    private String college;
    /**
     * 班级
     */
    @ApiModelProperty("班级")    
    private String grade;
    /**
     * 宿舍号
     */
    @ApiModelProperty("宿舍号")    
    private String roomnumb;
    /**
     * 头像
     */
    @ApiModelProperty("头像")    
    private String icon;
    /**
     * 用户余额
     */
    private Double balance;


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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRoomnumb() {
        return roomnumb;
    }

    public void setRoomnumb(String roomnumb) {
        this.roomnumb = roomnumb;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

}

