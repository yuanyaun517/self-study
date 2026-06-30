package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.dao.GoodsDao;
import cn.only.hw.secondmarketserver.dao.OrdersDao;
import cn.only.hw.secondmarketserver.dao.UserDao;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.Orders;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.service.OrdersService;
import cn.only.hw.secondmarketserver.util.DealTypeUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersDao, Orders> implements OrdersService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private UserDao userDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders createOrder(Orders orders) {
        if (orders == null) {
            throw new IllegalArgumentException("订单信息不能为空");
        }
        if (orders.getUserid() == null) {
            throw new IllegalArgumentException("下单用户不能为空");
        }
        if (orders.getGoodsid() == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }

        int buyNumber = normalizeOrderNumber(orders.getNumber());
        if (buyNumber <= 0) {
            throw new IllegalArgumentException("购买数量必须大于0");
        }

        Goods goods = getRequiredGoods(orders.getGoodsid());
        if (goods.getSendUser() != null && goods.getSendUser().equals(orders.getUserid())) {
            throw new IllegalArgumentException("不能购买自己发布的商品");
        }
        if (!"1".equals(goods.getManage())) {
            throw new IllegalArgumentException("当前商品暂不可购买");
        }
        if (goods.getPrice() == null) {
            throw new IllegalArgumentException("商品价格异常");
        }

        int stock = goods.getNumber() == null ? 0 : goods.getNumber();
        if (stock < buyNumber) {
            throw new IllegalArgumentException("购买数量不能大于商品库存");
        }

        LambdaUpdateWrapper<Goods> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Goods::getId, goods.getId())
                .ge(Goods::getNumber, buyNumber)
                .setSql("number = number - " + buyNumber);
        int updateCount = goodsDao.update(null, updateWrapper);
        if (updateCount <= 0) {
            throw new IllegalArgumentException("商品库存不足，请刷新后重试");
        }

        Goods latestGoods = goodsService.getById(goods.getId());
        if (latestGoods != null
                && latestGoods.getNumber() != null
                && latestGoods.getNumber() <= 0
                && !"3".equals(latestGoods.getManage())) {
            Goods soldOutGoods = new Goods();
            soldOutGoods.setId(latestGoods.getId());
            soldOutGoods.setManage("3");
            goodsService.updateById(soldOutGoods);
        }

        orders.setNumber(buyNumber);
        orders.setPrice(calculateTotalPrice(goods.getPrice(), buyNumber));
        orders.setState("1");

        boolean saved = this.save(orders);
        if (!saved) {
            throw new IllegalStateException("下单失败");
        }
        return normalizeOrder(orders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Double payOrder(Integer orderId, Integer addressId) {
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        if (addressId == null) {
            throw new IllegalArgumentException("请选择收货地址");
        }

        Orders storedOrder = getRequiredOrder(orderId);
        if (!"1".equals(storedOrder.getState())) {
            throw new IllegalStateException("当前订单状态无法支付");
        }
        if (storedOrder.getUserid() == null) {
            throw new IllegalStateException("订单缺少用户信息");
        }
        if (storedOrder.getGoodsid() == null) {
            throw new IllegalStateException("订单缺少商品信息");
        }

        BigDecimal payAmount = normalizeMoney(storedOrder.getPrice());
        User buyer = userDao.selectById(storedOrder.getUserid());
        if (buyer == null) {
            throw new IllegalStateException("下单用户不存在");
        }
        if (normalizeBalance(buyer.getBalance()).compareTo(payAmount) < 0) {
            throw new IllegalStateException("余额不足");
        }

        Goods goods = getRequiredGoods(storedOrder.getGoodsid());
        if (goods.getSendUser() == null) {
            throw new IllegalStateException("商品卖家不存在");
        }

        if (deductUserBalance(buyer.getId(), payAmount) <= 0) {
            throw new IllegalStateException("余额不足，请刷新后重试");
        }
        if (addUserBalance(goods.getSendUser(), payAmount) <= 0) {
            throw new IllegalStateException("卖家账户不存在");
        }
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, storedOrder.getId())
                .eq(Orders::getState, "1")
                .set(Orders::getAddressid, addressId)
                .set(Orders::getState, "2");
        if (baseMapper.update(null, updateWrapper) <= 0) {
            throw new IllegalStateException("支付失败，请刷新后重试");
        }

        User latestBuyer = userDao.selectById(buyer.getId());
        return normalizeBalance(latestBuyer == null ? null : latestBuyer.getBalance()).doubleValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }

        Orders storedOrder = getRequiredOrder(orderId);
        if (!"1".equals(storedOrder.getState())) {
            throw new IllegalStateException("当前订单状态无法取消");
        }
        if (storedOrder.getGoodsid() == null) {
            throw new IllegalStateException("订单缺少商品信息");
        }

        Goods goods = getRequiredGoods(storedOrder.getGoodsid());
        int restoreNumber = normalizeOrderNumber(storedOrder.getNumber());
        Integer currentStock = goods.getNumber();

        LambdaUpdateWrapper<Orders> orderUpdateWrapper = new LambdaUpdateWrapper<>();
        orderUpdateWrapper.eq(Orders::getId, storedOrder.getId())
                .eq(Orders::getState, "1")
                .set(Orders::getState, "5");
        if (baseMapper.update(null, orderUpdateWrapper) <= 0) {
            throw new IllegalStateException("取消订单失败，请刷新后重试");
        }

        LambdaUpdateWrapper<Goods> goodsUpdateWrapper = new LambdaUpdateWrapper<>();
        goodsUpdateWrapper.eq(Goods::getId, goods.getId())
                .setSql("number = COALESCE(number, 0) + " + restoreNumber);
        if (goodsDao.update(null, goodsUpdateWrapper) <= 0) {
            throw new IllegalStateException("恢复商品库存失败");
        }

        Goods latestGoods = goodsService.getById(goods.getId());
        Integer latestStock = latestGoods == null ? null : latestGoods.getNumber();
        if (latestGoods != null
                && "3".equals(latestGoods.getManage())
                && (currentStock == null || currentStock <= 0)
                && latestStock != null
                && latestStock > 0) {
            Goods restoreGoods = new Goods();
            restoreGoods.setId(latestGoods.getId());
            restoreGoods.setManage("1");
            goodsService.updateById(restoreGoods);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Integer orderId, Integer sellerUserId, String logistics) {
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        if (sellerUserId == null) {
            throw new IllegalArgumentException("卖家ID不能为空");
        }

        Orders storedOrder = getRequiredOrder(orderId);
        if (!"2".equals(storedOrder.getState())) {
            throw new IllegalStateException("当前订单状态无法发货");
        }
        if (storedOrder.getGoodsid() == null) {
            throw new IllegalStateException("订单缺少商品信息");
        }

        Goods goods = getRequiredGoods(storedOrder.getGoodsid());
        if (goods.getSendUser() == null || !sellerUserId.equals(goods.getSendUser())) {
            throw new IllegalStateException("只有卖家可以填写物流");
        }

        String logisticsNo = logistics == null ? "" : logistics.trim();
        if (DealTypeUtils.requiresLogistics(goods.getDealtypy()) && logisticsNo.isEmpty()) {
            throw new IllegalArgumentException("快递发货必须填写物流单号");
        }

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, storedOrder.getId())
                .eq(Orders::getState, "2")
                .set(Orders::getLogistics, logisticsNo.isEmpty() ? null : logisticsNo)
                .set(Orders::getState, "3");
        if (baseMapper.update(null, updateWrapper) <= 0) {
            throw new IllegalStateException("发货失败，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }

        Orders storedOrder = getRequiredOrder(orderId);
        if (!"3".equals(storedOrder.getState())) {
            throw new IllegalStateException("当前订单状态无法确认收货");
        }

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, storedOrder.getId())
                .eq(Orders::getState, "3")
                .set(Orders::getState, "4");
        if (baseMapper.update(null, updateWrapper) <= 0) {
            throw new IllegalStateException("确认收货失败，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewOrder(Integer orderId, Integer userId, Integer rating, String reviewContent) {
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        int normalizedRating = normalizeRating(rating);
        String normalizedReviewContent = normalizeReviewContent(reviewContent);

        Orders storedOrder = getRequiredOrder(orderId);
        if (!"4".equals(storedOrder.getState())) {
            throw new IllegalStateException("只有已完成的订单可以评价");
        }
        if (storedOrder.getUserid() == null || !userId.equals(storedOrder.getUserid())) {
            throw new IllegalStateException("只有下单用户可以评价订单");
        }

        if (storedOrder.getRating() != null) {
            throw new IllegalStateException("褰撳墠璁㈠崟宸茶瘎浠凤紝涓嶅彲鍐嶆淇敼");
        }

        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, storedOrder.getId())
                .eq(Orders::getUserid, userId)
                .eq(Orders::getState, "4")
                .isNull(Orders::getRating)
                .set(Orders::getRating, normalizedRating)
                .set(Orders::getReviewContent, normalizedReviewContent);
        if (baseMapper.update(null, updateWrapper) <= 0) {
            throw new IllegalStateException("提交评价失败，请刷新后重试");
        }
    }

    @Override
    public Orders getById(Serializable id) {
        return normalizeOrder(super.getById(id));
    }

    @Override
    public List<Orders> list() {
        return normalizeOrderList(super.list());
    }

    @Override
    public List<Orders> list(Wrapper<Orders> queryWrapper) {
        return normalizeOrderList(super.list(queryWrapper));
    }

    private Orders getRequiredOrder(Integer orderId) {
        Orders storedOrder = super.getById(orderId);
        if (storedOrder == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return storedOrder;
    }

    private Goods getRequiredGoods(Integer goodsId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        return goods;
    }

    private int normalizeOrderNumber(Integer number) {
        return number == null ? 1 : number;
    }

    private int normalizeRating(Integer rating) {
        if (rating == null) {
            throw new IllegalArgumentException("评分不能为空");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1到5分之间");
        }
        return rating;
    }

    private String normalizeReviewContent(String reviewContent) {
        String normalized = reviewContent == null ? "" : reviewContent.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("评价内容不能超过500字");
        }
        return normalized.isEmpty() ? null : normalized;
    }

    private Double calculateTotalPrice(Double price, int buyNumber) {
        return BigDecimal.valueOf(price)
                .multiply(BigDecimal.valueOf(buyNumber))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private int deductUserBalance(Integer userId, BigDecimal amount) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .ge(User::getBalance, amount.doubleValue())
                .setSql("balance = ROUND(COALESCE(balance, 0) - " + amount.toPlainString() + ", 2)");
        return userDao.update(null, updateWrapper);
    }

    private int addUserBalance(Integer userId, BigDecimal amount) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .setSql("balance = ROUND(COALESCE(balance, 0) + " + amount.toPlainString() + ", 2)");
        return userDao.update(null, updateWrapper);
    }

    private BigDecimal normalizeMoney(Double amount) {
        if (amount == null || !Double.isFinite(amount) || amount < 0) {
            throw new IllegalStateException("订单金额异常");
        }
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeBalance(Double balance) {
        if (balance == null || !Double.isFinite(balance) || balance < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP);
    }

    private Orders normalizeOrder(Orders orders) {
        if (orders != null && orders.getNumber() == null) {
            orders.setNumber(1);
        }
        return orders;
    }

    private List<Orders> normalizeOrderList(List<Orders> ordersList) {
        if (ordersList != null) {
            ordersList.forEach(this::normalizeOrder);
        }
        return ordersList;
    }
}
