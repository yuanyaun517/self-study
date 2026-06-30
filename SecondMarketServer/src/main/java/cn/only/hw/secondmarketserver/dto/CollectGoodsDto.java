package cn.only.hw.secondmarketserver.dto;

import cn.only.hw.secondmarketserver.entity.Collect;
import cn.only.hw.secondmarketserver.entity.Goods;
import lombok.Data;

/**
 * 描述          : TODO
 * 类名          : CollectGoodsDto
 */

@Data
public class CollectGoodsDto extends Collect {
    private Goods goods = new Goods();

}
