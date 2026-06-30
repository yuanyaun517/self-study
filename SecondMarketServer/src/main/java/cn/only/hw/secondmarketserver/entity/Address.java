package cn.only.hw.secondmarketserver.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 收货地址实体类
 * 用于存储和管理用户的收货地址信息
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@ApiModel("Address")
public class Address implements Serializable {
    private static final long serialVersionUID = -65838793718514934L;
    /**
     * 地址ID（主键）
     */
    private Integer id;
    /**
     * 收货人姓名
     */
    @ApiModelProperty("收货人姓名")    
    private String name;
    /**
     * 电话
     */
    @ApiModelProperty("电话")    
    private String tel;
    /**
     * 省
     */
    @ApiModelProperty("省")    
    private String province;
    /**
     * 市
     */
    @ApiModelProperty("市")    
    private String city;
    /**
     * 县
     */
    @ApiModelProperty("县")    
    private String county;
    /**
     * 详细地址
     */
    @ApiModelProperty("详细地址")    
    private String detail;
    /**
     * 是否默认地址
     */
    @ApiModelProperty("是否默认地址")    
    private String isdefault;
    /**
     * 哪个用户的地址信息
     */
    @ApiModelProperty("哪个用户的地址信息")    
    private Integer userid;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getIsdefault() {
        return isdefault;
    }

    public void setIsdefault(String isdefault) {
        this.isdefault = isdefault;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

}

