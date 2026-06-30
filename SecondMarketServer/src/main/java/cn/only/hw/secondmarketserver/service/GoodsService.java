package cn.only.hw.secondmarketserver.service;

import cn.only.hw.secondmarketserver.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * (Goods)表服务接口
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
public interface GoodsService extends IService<Goods>{

    List<Goods> getByType(String type);
}
