package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.dao.GoodsDao;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.util.DealTypeUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsDao, Goods> implements GoodsService {

    @Autowired
    private GoodsDao goodsDao;

    @Override
    public boolean save(Goods entity) {
        return super.save(applyAutoSoldOutState(fillDefaultNumber(entity)));
    }

    @Override
    public boolean updateById(Goods entity) {
        return super.updateById(applyAutoSoldOutState(entity));
    }

    @Override
    public Goods getById(Serializable id) {
        return normalizeGoodsForRead(super.getById(id));
    }

    @Override
    public List<Goods> list() {
        return normalizeGoodsListForRead(super.list());
    }

    @Override
    public List<Goods> list(Wrapper<Goods> queryWrapper) {
        return normalizeGoodsListForRead(super.list(queryWrapper));
    }

    @Override
    public List<Goods> getByType(String type) {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getType, type);
        queryWrapper.eq(Goods::getManage, "1");
        queryWrapper.gt(Goods::getNumber, 0);
        return normalizeGoodsListForRead(goodsDao.selectList(queryWrapper));
    }

    private Goods fillDefaultNumber(Goods goods) {
        if (goods != null && goods.getNumber() == null) {
            goods.setNumber(0);
        }
        return goods;
    }

    private Goods applyAutoSoldOutState(Goods goods) {
        if (goods != null && goods.getNumber() != null && goods.getNumber() <= 0) {
            goods.setManage("3");
        }
        return goods;
    }

    private Goods normalizeGoodsForRead(Goods goods) {
        if (goods != null) {
            if (goods.getNumber() == null) {
                goods.setNumber(0);
            }
            goods.setDealtypy(DealTypeUtils.normalizeForRead(goods.getDealtypy()));
        }
        return goods;
    }

    private List<Goods> normalizeGoodsListForRead(List<Goods> goodsList) {
        if (goodsList != null) {
            goodsList.forEach(this::normalizeGoodsForRead);
        }
        return goodsList;
    }
}
