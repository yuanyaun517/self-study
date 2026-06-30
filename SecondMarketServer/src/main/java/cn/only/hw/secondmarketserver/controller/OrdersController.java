package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.dto.OrderGoodsDto;
import cn.only.hw.secondmarketserver.dto.OrderReviewDto;
import cn.only.hw.secondmarketserver.dto.OrderShipDto;
import cn.only.hw.secondmarketserver.dto.OrdersDto;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.Orders;
import cn.only.hw.secondmarketserver.service.AddressService;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.service.OrdersService;
import cn.only.hw.secondmarketserver.service.UserService;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单控制器
 * 处理订单相关的HTTP请求，包括下单、支付、确认收货、取消订单等功能
 */
@RestController
@RequestMapping("/orders")
@Slf4j
@Api(tags = "Orders")
public class OrdersController {

    /**
     * 订单服务
     */
    @Autowired
    private OrdersService ordersService;

    /**
     * 商品服务
     */
    @Autowired
    private GoodsService goodsService;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 地址服务
     */
    @Autowired
    private AddressService addressService;

    @ApiOperation("Get all orders")
    @PostMapping("/getAllOrder")
    public Result<List<OrdersDto>> getAllOrder() {
        log.info("get all orders");
        List<Orders> ordersList = ordersService.list();
        if (ordersList.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<OrdersDto> result = ordersList.stream().map(item -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            ordersDto.setUser(item.getUserid() == null ? null : userService.getById(item.getUserid()));
            ordersDto.setGoods(item.getGoodsid() == null ? null : goodsService.getById(item.getGoodsid()));
            ordersDto.setAddress(item.getAddressid() == null ? null : addressService.getById(item.getAddressid()));
            return ordersDto;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @ApiOperation("Get order by id")
    @PostMapping("/getById")
    public Result<OrderGoodsDto> getById(Integer id) {
        log.info("get order by id={}", id);
        if (id == null) {
            return Result.error("order id is empty");
        }

        Orders orders = ordersService.getById(id);
        if (orders == null) {
            return Result.error("order not found");
        }

        OrderGoodsDto orderGoodsDto = buildOrderGoodsDto(orders, goodsService.getById(orders.getGoodsid()));
        return Result.success(orderGoodsDto);
    }

    @ApiOperation("Get orders by user id")
    @PostMapping("/getByUserId")
    public Result<List<OrderGoodsDto>> getByUserId(Integer userid) {
        log.info("get orders by user id={}", userid);
        if (userid == null) {
            return Result.error("user id is empty");
        }

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserid, userid)
                .orderByDesc(Orders::getSendTime)
                .orderByDesc(Orders::getId);
        List<Orders> ordersList = ordersService.list(queryWrapper);

        List<OrderGoodsDto> result = ordersList.stream()
                .map(item -> buildOrderGoodsDto(item, goodsService.getById(item.getGoodsid())))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            return Result.error("no orders found");
        }
        return Result.success(result);
    }

    @ApiOperation("Get orders by seller user id")
    @PostMapping("/getBySellerUserId")
    public Result<List<OrderGoodsDto>> getBySellerUserId(Integer sellerUserId) {
        log.info("get orders by seller user id={}", sellerUserId);
        if (sellerUserId == null) {
            return Result.error("seller user id is empty");
        }

        LambdaQueryWrapper<Goods> goodsQueryWrapper = new LambdaQueryWrapper<>();
        goodsQueryWrapper.eq(Goods::getSendUser, sellerUserId);
        List<Goods> sellerGoods = goodsService.list(goodsQueryWrapper);
        if (sellerGoods.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        Map<Integer, Goods> goodsMap = sellerGoods.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(Goods::getId, item -> item, (left, right) -> left));
        if (goodsMap.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        LambdaQueryWrapper<Orders> orderQueryWrapper = new LambdaQueryWrapper<>();
        orderQueryWrapper.in(Orders::getGoodsid, goodsMap.keySet())
                .orderByDesc(Orders::getSendTime)
                .orderByDesc(Orders::getId);

        List<OrderGoodsDto> result = ordersService.list(orderQueryWrapper).stream()
                .map(item -> buildOrderGoodsDto(item, goodsMap.get(item.getGoodsid())))
                .collect(Collectors.toList());
        return Result.success(result);
    }

    @ApiOperation("Create order")
    @PostMapping("/save")
    public Result<String> save(@RequestBody Orders orders) {
        log.info("create order: {}", orders);
        try {
            Orders savedOrder = ordersService.createOrder(orders);
            return Result.success("下单成功").add("orderId", savedOrder.getId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("create order failed", e);
            return Result.error("下单失败，请稍后重试");
        }
    }

    @ApiOperation("Pay order")
    @PostMapping("/pay")
    public Result<String> pay(@RequestBody Orders orders) {
        log.info("pay order: {}", orders);
        if (orders == null || orders.getId() == null) {
            return Result.error("order id is empty");
        }
        if (orders.getAddressid() == null) {
            return Result.error("address id is empty");
        }

        try {
            Double balance = ordersService.payOrder(orders.getId(), orders.getAddressid());
            return Result.success("支付成功").add("balance", balance);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("pay order failed", e);
            return Result.error("支付失败，请稍后重试");
        }
    }

    @ApiOperation("Confirm order receipt")
    @PostMapping("/confirmOrders")
    public Result<String> confirmOrders(@RequestBody Orders orders) {
        log.info("confirm order receipt: {}", orders);
        if (orders == null || orders.getId() == null) {
            return Result.error("order id is empty");
        }

        try {
            ordersService.confirmOrder(orders.getId());
            return Result.success("确认收货成功");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("confirm order receipt failed", e);
            return Result.error("确认收货失败");
        }
    }

    @ApiOperation("Cancel unpaid order")
    @PostMapping("/cancelOrder")
    public Result<String> cancelOrder(@RequestBody Orders orders) {
        log.info("cancel order: {}", orders);
        if (orders == null || orders.getId() == null) {
            return Result.error("order id is empty");
        }

        try {
            ordersService.cancelOrder(orders.getId());
            return Result.success("取消订单成功");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("cancel order failed", e);
            return Result.error("取消订单失败");
        }
    }

    @ApiOperation("Ship paid order")
    @PostMapping("/shipOrder")
    public Result<String> shipOrder(@RequestBody OrderShipDto orderShipDto) {
        log.info("ship order: {}", orderShipDto);
        if (orderShipDto == null || orderShipDto.getId() == null) {
            return Result.error("order id is empty");
        }
        if (orderShipDto.getSellerUserId() == null) {
            return Result.error("seller user id is empty");
        }

        try {
            ordersService.shipOrder(orderShipDto.getId(), orderShipDto.getSellerUserId(), orderShipDto.getLogistics());
            return Result.success("发货成功");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("ship order failed", e);
            return Result.error("发货失败");
        }
    }

    @ApiOperation("Review completed order")
    @PostMapping("/reviewOrder")
    public Result<String> reviewOrder(@RequestBody OrderReviewDto orderReviewDto) {
        log.info("review order: {}", orderReviewDto);
        if (orderReviewDto == null || orderReviewDto.getId() == null) {
            return Result.error("order id is empty");
        }
        if (orderReviewDto.getUserId() == null) {
            return Result.error("user id is empty");
        }

        try {
            ordersService.reviewOrder(
                    orderReviewDto.getId(),
                    orderReviewDto.getUserId(),
                    orderReviewDto.getRating(),
                    orderReviewDto.getReviewContent()
            );
            return Result.success("评价提交成功");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("review order failed", e);
            return Result.error("评价提交失败");
        }
    }

    @ApiOperation("Delete order")
    @PostMapping("/delOrder")
    public Result<String> delOrder(Integer id) {
        log.info("delete order: {}", id);
        if (id == null) {
            return Result.error("order id is empty");
        }

        boolean removed = ordersService.removeById(id);
        if (removed) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    private OrderGoodsDto buildOrderGoodsDto(Orders orders, Goods goods) {
        OrderGoodsDto orderGoodsDto = new OrderGoodsDto();
        BeanUtils.copyProperties(orders, orderGoodsDto);
        orderGoodsDto.setGoods(goods == null ? new Goods() : goods);
        return orderGoodsDto;
    }
}
