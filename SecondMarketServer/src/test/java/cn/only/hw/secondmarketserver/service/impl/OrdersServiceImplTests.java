package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.dao.GoodsDao;
import cn.only.hw.secondmarketserver.dao.OrdersDao;
import cn.only.hw.secondmarketserver.dao.UserDao;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.Orders;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.GoodsService;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdersServiceImplTests {

    @Mock
    private OrdersDao ordersDao;

    @Mock
    private GoodsService goodsService;

    @Mock
    private GoodsDao goodsDao;

    @Mock
    private UserDao userDao;

    private OrdersServiceImpl ordersService;

    @BeforeEach
    void setUp() {
        initializeTableInfo(Goods.class);
        initializeTableInfo(Orders.class);
        initializeTableInfo(User.class);
        ordersService = new OrdersServiceImpl();
        ReflectionTestUtils.setField(ordersService, "baseMapper", ordersDao);
        ReflectionTestUtils.setField(ordersService, "goodsService", goodsService);
        ReflectionTestUtils.setField(ordersService, "goodsDao", goodsDao);
        ReflectionTestUtils.setField(ordersService, "userDao", userDao);
    }

    @Test
    void shouldTransferBalanceAndPayOrder() {
        Orders order = new Orders();
        order.setId(1001);
        order.setUserid(1);
        order.setGoodsid(2001);
        order.setPrice(20D);
        order.setState("1");

        User buyer = new User();
        buyer.setId(1);
        buyer.setBalance(50D);

        User latestBuyer = new User();
        latestBuyer.setId(1);
        latestBuyer.setBalance(30D);

        Goods goods = new Goods();
        goods.setId(2001);
        goods.setSendUser(2);

        when(ordersDao.selectById(1001)).thenReturn(order);
        when(goodsService.getById(2001)).thenReturn(goods);
        when(userDao.selectById(1)).thenReturn(buyer, latestBuyer);
        when(userDao.update(isNull(), any())).thenReturn(1);
        when(ordersDao.update(isNull(), any())).thenReturn(1);

        Double balance = ordersService.payOrder(1001, 3001);

        assertEquals(30D, balance);
        verify(userDao, times(2)).update(isNull(), any());
        verify(ordersDao).update(isNull(), any());
    }

    @Test
    void shouldRejectPaymentWhenBalanceIsInsufficient() {
        Orders order = new Orders();
        order.setId(1002);
        order.setUserid(1);
        order.setGoodsid(2002);
        order.setPrice(20D);
        order.setState("1");

        User buyer = new User();
        buyer.setId(1);
        buyer.setBalance(10D);

        when(ordersDao.selectById(1002)).thenReturn(order);
        when(userDao.selectById(1)).thenReturn(buyer);

        assertThrows(IllegalStateException.class, () -> ordersService.payOrder(1002, 3002));
        verify(userDao, never()).update(isNull(), any());
        verify(ordersDao, never()).update(isNull(), any());
    }

    @Test
    void shouldRejectCreateOrderWhenBuyerIsSeller() {
        Orders order = new Orders();
        order.setUserid(8);
        order.setGoodsid(2008);
        order.setNumber(1);

        Goods goods = new Goods();
        goods.setId(2008);
        goods.setSendUser(8);
        goods.setManage("1");
        goods.setPrice(20D);
        goods.setNumber(5);

        when(goodsService.getById(2008)).thenReturn(goods);

        assertThrows(IllegalArgumentException.class, () -> ordersService.createOrder(order));
        verify(goodsDao, never()).update(isNull(), any());
        verify(ordersDao, never()).insert(any(Orders.class));
    }

    @Test
    void shouldRestoreStockWhenCancelUnpaidOrder() {
        Orders order = new Orders();
        order.setId(1003);
        order.setGoodsid(2003);
        order.setNumber(2);
        order.setState("1");

        Goods goods = new Goods();
        goods.setId(2003);
        goods.setNumber(0);
        goods.setManage("3");

        Goods latestGoods = new Goods();
        latestGoods.setId(2003);
        latestGoods.setNumber(2);
        latestGoods.setManage("3");

        when(ordersDao.selectById(1003)).thenReturn(order);
        when(goodsService.getById(2003)).thenReturn(goods, latestGoods);
        when(ordersDao.update(isNull(), any())).thenReturn(1);
        when(goodsDao.update(isNull(), any())).thenReturn(1);
        when(goodsService.updateById(any(Goods.class))).thenReturn(true);

        ordersService.cancelOrder(1003);

        verify(ordersDao).update(isNull(), any());
        verify(goodsDao).update(isNull(), any());
        verify(goodsService).updateById(any(Goods.class));
    }

    @Test
    void shouldAllowSellerToShipPaidOrder() {
        Orders order = new Orders();
        order.setId(1004);
        order.setGoodsid(2004);
        order.setState("2");

        Goods goods = new Goods();
        goods.setId(2004);
        goods.setSendUser(8);

        when(ordersDao.selectById(1004)).thenReturn(order);
        when(goodsService.getById(2004)).thenReturn(goods);
        when(ordersDao.update(isNull(), any())).thenReturn(1);

        ordersService.shipOrder(1004, 8, "SF123456789");

        verify(ordersDao).update(isNull(), any());
    }

    @Test
    void shouldAllowSellerToShipFaceToFaceOrderWithoutLogistics() {
        Orders order = new Orders();
        order.setId(1010);
        order.setGoodsid(2010);
        order.setState("2");

        Goods goods = new Goods();
        goods.setId(2010);
        goods.setSendUser(8);
        goods.setDealtypy("线下面对面交易");

        when(ordersDao.selectById(1010)).thenReturn(order);
        when(goodsService.getById(2010)).thenReturn(goods);
        when(ordersDao.update(isNull(), any())).thenReturn(1);

        ordersService.shipOrder(1010, 8, " ");

        verify(ordersDao).update(isNull(), any());
    }

    @Test
    void shouldRejectShipWhenExpressOrderHasNoLogistics() {
        Orders order = new Orders();
        order.setId(1011);
        order.setGoodsid(2011);
        order.setState("2");

        Goods goods = new Goods();
        goods.setId(2011);
        goods.setSendUser(8);
        goods.setDealtypy("线上快递发货");

        when(ordersDao.selectById(1011)).thenReturn(order);
        when(goodsService.getById(2011)).thenReturn(goods);

        assertThrows(IllegalArgumentException.class, () -> ordersService.shipOrder(1011, 8, " "));
        verify(ordersDao, never()).update(isNull(), any());
    }

    @Test
    void shouldRejectShipWhenOperatorIsNotSeller() {
        Orders order = new Orders();
        order.setId(1005);
        order.setGoodsid(2005);
        order.setState("2");

        Goods goods = new Goods();
        goods.setId(2005);
        goods.setSendUser(9);

        when(ordersDao.selectById(1005)).thenReturn(order);
        when(goodsService.getById(2005)).thenReturn(goods);

        assertThrows(IllegalStateException.class, () -> ordersService.shipOrder(1005, 10, "SF0001"));
        verify(ordersDao, never()).update(isNull(), any());
    }

    @Test
    void shouldAllowBuyerToReviewCompletedOrder() {
        Orders order = new Orders();
        order.setId(1006);
        order.setUserid(1);
        order.setState("4");

        when(ordersDao.selectById(1006)).thenReturn(order);
        when(ordersDao.update(isNull(), any())).thenReturn(1);

        ordersService.reviewOrder(1006, 1, 5, "Very satisfied");

        verify(ordersDao).update(isNull(), any());
    }

    @Test
    void shouldRejectReviewWhenOrderIsNotCompleted() {
        Orders order = new Orders();
        order.setId(1007);
        order.setUserid(1);
        order.setState("3");

        when(ordersDao.selectById(1007)).thenReturn(order);

        assertThrows(IllegalStateException.class, () -> ordersService.reviewOrder(1007, 1, 4, "Good"));
        verify(ordersDao, never()).update(isNull(), any());
    }

    @Test
    void shouldRejectReviewWhenReviewerIsNotBuyer() {
        Orders order = new Orders();
        order.setId(1008);
        order.setUserid(1);
        order.setState("4");

        when(ordersDao.selectById(1008)).thenReturn(order);

        assertThrows(IllegalStateException.class, () -> ordersService.reviewOrder(1008, 2, 4, "Good"));
        verify(ordersDao, never()).update(isNull(), any());
    }

    @Test
    void shouldRejectReviewWhenOrderAlreadyReviewed() {
        Orders order = new Orders();
        order.setId(1009);
        order.setUserid(1);
        order.setState("4");
        order.setRating(5);

        when(ordersDao.selectById(1009)).thenReturn(order);

        assertThrows(IllegalStateException.class, () -> ordersService.reviewOrder(1009, 1, 4, "Good"));
        verify(ordersDao, never()).update(isNull(), any());
    }

    private void initializeTableInfo(Class<?> entityClass) {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                entityClass
        );
    }
}
