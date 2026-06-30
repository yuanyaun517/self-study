package cn.only.hw.secondmarketserver.dto;

import cn.only.hw.secondmarketserver.entity.Cart;
import cn.only.hw.secondmarketserver.entity.Goods;
import lombok.Data;

/**
 * 描述          : TODO
 * 类名          : CartGoodsDto
 */
@Data
public class CartGoodsDto extends Cart {
    private Goods goods = new Goods();

}
