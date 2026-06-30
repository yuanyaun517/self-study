package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.dto.OrderGoodsDto;
import cn.only.hw.secondmarketserver.dto.SellerProfileDto;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.Orders;
import cn.only.hw.secondmarketserver.entity.User;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求，包括登录、注册、信息管理等功能
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "User")
public class UserController {

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 商品服务
     */
    @Autowired
    private GoodsService goodsService;

    /**
     * 订单服务
     */
    @Autowired
    private OrdersService ordersService;

    /**
     * 用户登录
     * @param user 登录信息（账号和密码）
     * @param session HTTP会话
     * @return 登录结果，包含用户信息
     */
    @ApiOperation("User login")
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user, HttpSession session) {
        String account = user == null ? null : user.getAccount();
        log.info("user login, account={}", account);
        User loginUser = userService.login(user);
        if (loginUser == null) {
            return Result.error("账号或密码错误");
        }
        session.setAttribute("user", loginUser.getId());
        return Result.success(loginUser);
    }

    /**
     * 用户注册
     * @param user 注册信息
     * @return 注册结果
     */
    @ApiOperation("User register")
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        if (user == null || !StringUtils.hasText(user.getAccount()) || !StringUtils.hasText(user.getPassword())) {
            return Result.error("账号或密码不能为空");
        }
        trimAllFields(user);
        Result<String> requiredFieldValidation = validateRegisterRequiredFields(user);
        if (requiredFieldValidation != null) {
            return requiredFieldValidation;
        }
        user.setBalance(null);
        return userService.register(user);
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新结果
     */
    @ApiOperation("Update user info")
    @PostMapping("/update")
    public Result<String> update(@RequestBody User user) {
        if (user == null || user.getId() == null) {
            return Result.error("user id is empty");
        }
        trimAllFields(user);
        user.setBalance(null);
        if (user.getAccount() != null && !StringUtils.hasText(user.getAccount())) {
            return Result.error("账号不能为空");
        }
        if (user.getPassword() != null && !StringUtils.hasText(user.getPassword())) {
            return Result.error("密码不能为空");
        }

        log.info("update user, id={}", user.getId());
        boolean updated = userService.updateById(user);
        if (updated) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 用户余额充值
     * @param user 用户ID和充值金额
     * @return 充值结果
     */
    @ApiOperation("Recharge user balance")
    @PostMapping("/rechargeBalance")
    public Result<String> rechargeBalance(@RequestBody User user) {
        return changeBalance(user, true);
    }

    /**
     * 用户余额提现
     * @param user 用户ID和提现金额
     * @return 提现结果
     */
    @ApiOperation("Withdraw user balance")
    @PostMapping("/withdrawBalance")
    public Result<String> withdrawBalance(@RequestBody User user) {
        return changeBalance(user, false);
    }

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @ApiOperation("根据ID获取用户")
    @PostMapping("/getById")
    public Result<User> getById(Integer id) {
        if (id == null) {
            return Result.error("用户ID不能为空");
        }
        log.info("get user by id={}", id);
        User user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 获取卖家公开信息（包含商品和评价）
     * @param id 卖家用户ID
     * @return 卖家公开档案信息
     */
    @ApiOperation("Get seller public profile")
    @PostMapping("/getSellerProfile")
    public Result<SellerProfileDto> getSellerProfile(Integer id) {
        if (id == null) {
            return Result.error("卖家用户ID不能为空");
        }

        log.info("get seller profile by id={}", id);
        User seller = userService.getById(id);
        if (seller == null) {
            return Result.error("卖家不存在");
        }

        List<Goods> goodsList = getSellerPublicGoods(id);
        List<OrderGoodsDto> reviewedOrders = getSellerReviewedOrders(goodsList);

        SellerProfileDto sellerProfileDto = new SellerProfileDto();
        sellerProfileDto.setUser(buildPublicSeller(seller));
        sellerProfileDto.setGoodsList(goodsList);
        sellerProfileDto.setReviewedOrders(reviewedOrders);
        sellerProfileDto.setGoodsCount(goodsList.size());
        sellerProfileDto.setReviewedOrderCount(reviewedOrders.size());
        return Result.success(sellerProfileDto);
    }

    /**
     * 去除用户信息字段的首尾空格
     * @param user 用户对象
     */
    private void trimAllFields(User user) {
        user.setAccount(trimValue(user.getAccount()));
        user.setPassword(trimValue(user.getPassword()));
        user.setNickname(trimValue(user.getNickname()));
        user.setSex(trimValue(user.getSex()));
        user.setTel(trimValue(user.getTel()));
        user.setIdcard(trimValue(user.getIdcard()));
        user.setCollege(trimValue(user.getCollege()));
        user.setGrade(trimValue(user.getGrade()));
        user.setRoomnumb(trimValue(user.getRoomnumb()));
        user.setIcon(trimValue(user.getIcon()));
    }

    /**
     * 验证注册必填字段
     * @param user 用户对象
     * @return 验证失败返回错误信息，成功返回null
     */
    private Result<String> validateRegisterRequiredFields(User user) {
        if (!StringUtils.hasText(user.getNickname())) {
            return Result.error("昵称不能为空");
        }
        if (!StringUtils.hasText(user.getSex())) {
            return Result.error("性别不能为空");
        }
        if (!StringUtils.hasText(user.getTel())) {
            return Result.error("手机号不能为空");
        }
        if (!StringUtils.hasText(user.getIdcard())) {
            return Result.error("身份证号不能为空");
        }
        if (!StringUtils.hasText(user.getCollege())) {
            return Result.error("学院不能为空");
        }
        if (!StringUtils.hasText(user.getGrade())) {
            return Result.error("班级不能为空");
        }
        return null;
    }

    /**
     * 变更用户余额（充值或提现）
     * @param user 用户信息
     * @param increase true为充值，false为提现
     * @return 操作结果
     */
    private Result<String> changeBalance(User user, boolean increase) {
        if (user == null || user.getId() == null) {
            return Result.error("user id is empty");
        }

        try {
            Double balance = increase
                    ? userService.rechargeBalance(user.getId(), user.getBalance())
                    : userService.withdrawBalance(user.getId(), user.getBalance());
            return Result.success(increase ? "鍏呭€兼垚鍔?" : "鎻愮幇鎴愬姛").add("balance", balance);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("change balance failed, id={}, increase={}", user.getId(), increase, ex);
            return Result.error("浣欓鏇存柊澶辫触锛岃绋嶅悗閲嶈瘯");
        }
    }

    private String trimValue(String value) {
        return value == null ? null : value.trim();
    }

    private List<Goods> getSellerPublicGoods(Integer sellerUserId) {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getSendUser, sellerUserId)
                .in(Goods::getManage, Arrays.asList("1", "3"))
                .orderByDesc(Goods::getUpdateTime)
                .orderByDesc(Goods::getSendTime)
                .orderByDesc(Goods::getId);
        return goodsService.list(queryWrapper);
    }

    private List<OrderGoodsDto> getSellerReviewedOrders(List<Goods> goodsList) {
        Map<Integer, Goods> goodsMap = (goodsList == null ? Collections.<Goods>emptyList() : goodsList).stream()
                .filter(item -> item != null && item.getId() != null)
                .collect(Collectors.toMap(Goods::getId, item -> item, (left, right) -> left));
        if (goodsMap.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Orders::getGoodsid, goodsMap.keySet())
                .isNotNull(Orders::getRating)
                .orderByDesc(Orders::getUpdateTime)
                .orderByDesc(Orders::getId);

        return ordersService.list(queryWrapper).stream()
                .map(item -> buildOrderGoodsDto(item, goodsMap.get(item.getGoodsid())))
                .collect(Collectors.toList());
    }

    private User buildPublicSeller(User seller) {
        User publicSeller = new User();
        publicSeller.setId(seller.getId());
        publicSeller.setAccount(seller.getAccount());
        publicSeller.setNickname(seller.getNickname());
        publicSeller.setSex(seller.getSex());
        publicSeller.setCollege(seller.getCollege());
        publicSeller.setGrade(seller.getGrade());
        publicSeller.setIcon(seller.getIcon());
        return publicSeller;
    }

    private OrderGoodsDto buildOrderGoodsDto(Orders orders, Goods goods) {
        OrderGoodsDto orderGoodsDto = new OrderGoodsDto();
        BeanUtils.copyProperties(orders, orderGoodsDto);
        orderGoodsDto.setGoods(goods == null ? new Goods() : goods);
        return orderGoodsDto;
    }
}
