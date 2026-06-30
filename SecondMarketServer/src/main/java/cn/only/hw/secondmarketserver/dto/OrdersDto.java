package cn.only.hw.secondmarketserver.dto;

import cn.only.hw.secondmarketserver.entity.Address;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.Orders;
import cn.only.hw.secondmarketserver.entity.User;
import lombok.Data;

/**
 * 作者          : 李淑娟
 * 创建日期       : Created in 2026/1/20
 * 描述          : TODO
 * 类名          : OrdersDto
 */

@Data
public class OrdersDto extends Orders {
    private User user = new User();
    private Goods goods = new Goods();
    private Address address = new Address();

}
