package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.dto.CartGoodsDto;
import cn.only.hw.secondmarketserver.entity.Cart;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.service.CartService;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 购物车控制器
 * 处理购物车相关的HTTP请求，包括添加商品、更新数量、删除等功能
 */
@RestController
@RequestMapping("/cart")
@Slf4j
@Api(tags = "Cart")
public class CartController {

    /**
     * 购物车服务
     */
    @Autowired
    private CartService cartService;

    /**
     * 商品服务
     */
    @Autowired
    private GoodsService goodsService;

    @ApiOperation("获取所有购物车")
    @PostMapping("/list")
    public Result<List<Cart>> list() {
        log.info("获取所有购物车");
        return Result.success(cartService.list());
    }

    @ApiOperation("通过id获取购物车")
    @PostMapping("/getById")
    public Result<Cart> getById(Integer id) {
        log.info("通过id获取购物车: {}", id);
        if (id == null) {
            return Result.error("购物车id不能为空");
        }

        Cart cart = cartService.getById(id);
        if (cart == null) {
            return Result.error("购物车商品不存在");
        }
        return Result.success(cart);
    }

    @ApiOperation("按用户id获取购物车")
    @PostMapping("/getByUserId")
    public Result<List<CartGoodsDto>> getByUserId(Integer userid) {
        log.info("按用户id获取购物车: {}", userid);
        if (userid == null) {
            return Result.error("用户id不能为空");
        }

        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserid, userid)
                .orderByDesc(Cart::getSendTime);

        List<CartGoodsDto> list = cartService.list(queryWrapper).stream()
                .map(item -> buildCartGoodsDto(item))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Result.success(list);
    }

    @ApiOperation("加入购物车")
    @PostMapping("/save")
    public Result<String> save(@RequestBody Cart cart) {
        log.info("加入购物车: {}", cart);
        if (cart == null) {
            return Result.error("购物车信息不能为空");
        }
        if (cart.getUserid() == null) {
            return Result.error("用户id不能为空");
        }
        if (cart.getGoodsid() == null) {
            return Result.error("商品id不能为空");
        }

        int buyNumber = normalizeCartNumber(cart.getNumber());
        if (buyNumber <= 0) {
            return Result.error("购买数量必须大于0");
        }

        Goods goods = goodsService.getById(cart.getGoodsid());
        if (goods == null) {
            return Result.error("商品不存在");
        }
        if (!"1".equals(goods.getManage())) {
            return Result.error("当前商品不可加入购物车");
        }

        int stock = goods.getNumber() == null ? 0 : goods.getNumber();
        if (stock <= 0) {
            return Result.error("当前商品库存不足");
        }
        if (buyNumber > stock) {
            return Result.error("购买数量不能大于商品库存");
        }

        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserid, cart.getUserid())
                .eq(Cart::getGoodsid, cart.getGoodsid());
        Cart existsCart = cartService.getOne(queryWrapper, false);
        if (existsCart != null) {
            int totalNumber = normalizeCartNumber(existsCart.getNumber()) + buyNumber;
            if (totalNumber > stock) {
                return Result.error("购买数量不能大于商品库存");
            }

            Cart updateCart = new Cart();
            updateCart.setId(existsCart.getId());
            updateCart.setPrice(goods.getPrice());
            updateCart.setNumber(totalNumber);
            boolean updated = cartService.updateById(updateCart);
            if (updated) {
                return Result.success("购物车数量已更新");
            }
            return Result.error("购物车数量更新失败");
        }

        cart.setPrice(goods.getPrice());
        cart.setNumber(buyNumber);
        boolean saved = cartService.save(cart);
        if (saved) {
            return Result.success("加入购物车成功");
        }
        return Result.error("加入购物车失败");
    }

    @ApiOperation("更新购物车数量")
    @PostMapping("/update")
    public Result<String> update(@RequestBody Cart cart) {
        log.info("更新购物车: {}", cart);
        if (cart == null || cart.getId() == null) {
            return Result.error("购物车id不能为空");
        }

        int buyNumber = normalizeCartNumber(cart.getNumber());
        if (buyNumber <= 0) {
            return Result.error("购买数量必须大于0");
        }

        Cart existsCart = cartService.getById(cart.getId());
        if (existsCart == null) {
            return Result.error("购物车商品不存在");
        }

        Goods goods = goodsService.getById(existsCart.getGoodsid());
        if (goods == null) {
            return Result.error("商品不存在");
        }
        if (!"1".equals(goods.getManage())) {
            return Result.error("当前商品不可继续购买");
        }

        int stock = goods.getNumber() == null ? 0 : goods.getNumber();
        if (stock <= 0) {
            return Result.error("当前商品库存不足");
        }
        if (buyNumber > stock) {
            return Result.error("购买数量不能大于商品库存");
        }

        Cart updateCart = new Cart();
        updateCart.setId(existsCart.getId());
        updateCart.setPrice(goods.getPrice());
        updateCart.setNumber(buyNumber);
        boolean updated = cartService.updateById(updateCart);
        if (updated) {
            return Result.success("购物车数量更新成功");
        }
        return Result.error("购物车数量更新失败");
    }

    @ApiOperation("删除购物车物品")
    @PostMapping("/del")
    public Result<String> del(Integer id) {
        log.info("删除购物车物品: {}", id);
        if (id == null) {
            return Result.error("购物车id不能为空");
        }

        boolean removed = cartService.removeById(id);
        if (removed) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    private CartGoodsDto buildCartGoodsDto(Cart cart) {
        Goods goods = goodsService.getById(cart.getGoodsid());
        if (goods == null) {
            return null;
        }

        CartGoodsDto cartGoodsDto = new CartGoodsDto();
        BeanUtils.copyProperties(cart, cartGoodsDto);
        if (goods.getPrice() != null) {
            cartGoodsDto.setPrice(goods.getPrice());
        }
        cartGoodsDto.setGoods(goods);
        return cartGoodsDto;
    }

    private int normalizeCartNumber(Integer number) {
        return number == null ? 1 : number;
    }
}
