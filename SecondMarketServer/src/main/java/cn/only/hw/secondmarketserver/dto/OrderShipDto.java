package cn.only.hw.secondmarketserver.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderShipDto implements Serializable {

    private static final long serialVersionUID = -2325192851960393438L;

    private Integer id;

    private Integer sellerUserId;

    private String logistics;
}
