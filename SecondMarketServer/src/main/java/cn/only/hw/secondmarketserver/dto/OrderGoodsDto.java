package cn.only.hw.secondmarketserver.dto;

import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.Orders;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者          : 李淑娟
 * 创建日期       : Created in 2026/1/20
 * 描述          : TODO 订单-商品信息的dto
 * 类名          : OrderUserGoodsDto
 */

@Data
public class OrderGoodsDto extends Orders {
    private Goods goods = new Goods();

}
