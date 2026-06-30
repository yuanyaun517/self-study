package cn.only.hw.secondmarketserver.service;

import cn.only.hw.secondmarketserver.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrdersService extends IService<Orders> {

    Orders createOrder(Orders orders);

    Double payOrder(Integer orderId, Integer addressId);

    void cancelOrder(Integer orderId);

    void shipOrder(Integer orderId, Integer sellerUserId, String logistics);

    void confirmOrder(Integer orderId);

    void reviewOrder(Integer orderId, Integer userId, Integer rating, String reviewContent);
}
