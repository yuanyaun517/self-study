package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.entity.Cart;
import cn.only.hw.secondmarketserver.dao.CartDao;
import cn.only.hw.secondmarketserver.service.CartService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;


/**
 * (Cart)表服务实现类
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartDao,Cart> implements CartService {

    @Override
    public Cart getById(Serializable id) {
        return normalizeCart(super.getById(id));
    }

    @Override
    public List<Cart> list() {
        return normalizeCartList(super.list());
    }

    @Override
    public List<Cart> list(Wrapper<Cart> queryWrapper) {
        return normalizeCartList(super.list(queryWrapper));
    }

    private Cart normalizeCart(Cart cart) {
        if (cart != null && (cart.getNumber() == null || cart.getNumber() <= 0)) {
            cart.setNumber(1);
        }
        return cart;
    }

    private List<Cart> normalizeCartList(List<Cart> cartList) {
        if (cartList != null) {
            cartList.forEach(this::normalizeCart);
        }
        return cartList;
    }
}
