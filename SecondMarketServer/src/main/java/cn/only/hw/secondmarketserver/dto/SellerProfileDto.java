package cn.only.hw.secondmarketserver.dto;

import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SellerProfileDto {
    private User user = new User();
    private List<Goods> goodsList = new ArrayList<>();
    private List<OrderGoodsDto> reviewedOrders = new ArrayList<>();
    private Integer goodsCount = 0;
    private Integer reviewedOrderCount = 0;
}
