package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.dto.ForumCommentDto;
import cn.only.hw.secondmarketserver.dto.ForumDetailDto;
import cn.only.hw.secondmarketserver.dto.ManagerDashboardDto;
import cn.only.hw.secondmarketserver.dto.ManagerLoginDto;
import cn.only.hw.secondmarketserver.dto.ManagerPasswordDto;
import cn.only.hw.secondmarketserver.dto.OrdersDto;
import cn.only.hw.secondmarketserver.entity.Banner;
import cn.only.hw.secondmarketserver.entity.Catechild;
import cn.only.hw.secondmarketserver.entity.Category;
import cn.only.hw.secondmarketserver.entity.Forum;
import cn.only.hw.secondmarketserver.entity.ForumComment;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.Manager;
import cn.only.hw.secondmarketserver.entity.Menu;
import cn.only.hw.secondmarketserver.entity.Notice;
import cn.only.hw.secondmarketserver.entity.Orders;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.AddressService;
import cn.only.hw.secondmarketserver.service.BannerService;
import cn.only.hw.secondmarketserver.service.CatechildService;
import cn.only.hw.secondmarketserver.service.ForumCommentService;
import cn.only.hw.secondmarketserver.service.CategoryService;
import cn.only.hw.secondmarketserver.service.ForumService;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.service.ManagerService;
import cn.only.hw.secondmarketserver.service.MenuService;
import cn.only.hw.secondmarketserver.service.NoticeService;
import cn.only.hw.secondmarketserver.service.OrdersService;
import cn.only.hw.secondmarketserver.service.UserService;
import cn.only.hw.secondmarketserver.util.ValidateCodeUtils;
import cn.only.hw.secondmarketserver.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员控制器
 * 处理管理员相关的HTTP请求，包括登录、商品审核、用户管理、数据统计等功能
 */
@RestController
@RequestMapping("/manager")
@Slf4j
@Api(tags = "Manager")
public class ManagerController {

    private static final String MANAGER_CAPTCHA_SESSION_KEY = "managerCaptcha";
    private static final int CAPTCHA_WIDTH = 120;
    private static final int CAPTCHA_HEIGHT = 40;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private UserService userService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CatechildService catechildService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private ForumCommentService forumCommentService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private AddressService addressService;

    private String trimValue(String value) {
        return value == null ? null : value.trim();
    }

    private void trimUserFields(User user) {
        if (user == null) {
            return;
        }
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

    private Result<String> normalizeUserBalance(User user) {
        if (user == null || user.getBalance() == null) {
            return null;
        }
        if (!Double.isFinite(user.getBalance())) {
            return Result.error("Balance must be a valid number");
        }
        if (user.getBalance() < 0) {
            return Result.error("Balance cannot be negative");
        }
        user.setBalance(BigDecimal.valueOf(user.getBalance())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue());
        return null;
    }

    private void trimCategoryFields(Category category) {
        if (category == null) {
            return;
        }
        category.setCatename(trimValue(category.getCatename()));
    }

    private void trimManagerFields(Manager manager) {
        if (manager == null) {
            return;
        }
        manager.setAccount(trimValue(manager.getAccount()));
        manager.setPassword(trimValue(manager.getPassword()));
        manager.setAvatar(trimValue(manager.getAvatar()));
    }

    private void trimManagerLoginFields(ManagerLoginDto loginDto) {
        if (loginDto == null) {
            return;
        }
        loginDto.setAccount(trimValue(loginDto.getAccount()));
        loginDto.setPassword(trimValue(loginDto.getPassword()));
        loginDto.setCaptcha(trimValue(loginDto.getCaptcha()));
    }

    private Manager sanitizeManager(Manager manager) {
        if (manager == null) {
            return null;
        }
        manager.setPassword(null);
        return manager;
    }

    private boolean containsKeyword(String source, String keyword) {
        return StringUtils.hasText(source)
                && StringUtils.hasText(keyword)
                && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private String resolveGoodsCover(Goods goods) {
        if (goods == null) {
            return "";
        }
        if (StringUtils.hasText(goods.getIcon())) {
            return goods.getIcon().trim();
        }
        if (StringUtils.hasText(goods.getImgs())) {
            String[] imgs = goods.getImgs().split(",");
            return imgs.length > 0 ? trimValue(imgs[0]) : "";
        }
        return "";
    }

    private void cleanupCatechildByCateid(Integer cateid) {
        if (cateid == null) {
            return;
        }
        LambdaQueryWrapper<Catechild> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Catechild::getCateid, cateid);
        catechildService.remove(queryWrapper);
    }

    private void cleanupCatechildByGoodsId(Integer goodsId) {
        if (goodsId == null) {
            return;
        }
        LambdaQueryWrapper<Catechild> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Catechild::getGoodid, String.valueOf(goodsId));
        catechildService.remove(queryWrapper);
    }

    private Integer parseGoodsId(String goodsId) {
        if (!StringUtils.hasText(goodsId)) {
            return null;
        }
        try {
            return Integer.valueOf(goodsId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<Catechild> normalizeCatechildList(List<Catechild> catechildList) {
        List<Integer> goodsIds = catechildList.stream()
                .map(Catechild::getGoodid)
                .map(this::parseGoodsId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, Goods> goodsMap = Collections.emptyMap();
        if (!goodsIds.isEmpty()) {
            LambdaQueryWrapper<Goods> goodsQueryWrapper = new LambdaQueryWrapper<>();
            goodsQueryWrapper.in(Goods::getId, goodsIds);
            goodsMap = goodsService.list(goodsQueryWrapper).stream()
                    .filter(goods -> goods != null && goods.getId() != null)
                    .collect(Collectors.toMap(Goods::getId, goods -> goods));
        }

        final Map<Integer, Goods> finalGoodsMap = goodsMap;
        return catechildList.stream().map(item -> {
            Goods goods = finalGoodsMap.get(parseGoodsId(item.getGoodid()));
            if (goods != null) {
                if (StringUtils.hasText(goods.getName())) {
                    item.setChildname(goods.getName().trim());
                }
                item.setImage(resolveGoodsCover(goods));
            }
            return item;
        }).collect(Collectors.toList());
    }

    private boolean syncCatechildRelation(Goods goods) {
        if (goods == null || goods.getId() == null || goods.getCateid() == null) {
            return true;
        }
        cleanupCatechildByGoodsId(goods.getId());
        Catechild catechild = new Catechild();
        catechild.setCateid(goods.getCateid());
        catechild.setGoodid(String.valueOf(goods.getId()));
        catechild.setImage(resolveGoodsCover(goods));
        catechild.setChildname(goods.getName());
        return catechildService.save(catechild);
    }

    private OrdersDto toOrdersDto(Orders orders) {
        OrdersDto ordersDto = new OrdersDto();
        if (orders == null) {
            return ordersDto;
        }
        BeanUtils.copyProperties(orders, ordersDto);
        ordersDto.setUser(userService.getById(orders.getUserid()));
        ordersDto.setGoods(goodsService.getById(orders.getGoodsid()));
        ordersDto.setAddress(addressService.getById(orders.getAddressid()));
        return ordersDto;
    }

    private List<String> splitForumImages(String imgs) {
        if (!StringUtils.hasText(imgs)) {
            return Collections.emptyList();
        }
        List<String> imageList = new ArrayList<String>();
        for (String value : imgs.split(",")) {
            String image = trimValue(value);
            if (StringUtils.hasText(image)) {
                imageList.add(image);
            }
        }
        return imageList;
    }

    private User buildPublicUser(User user) {
        User publicUser = new User();
        if (user == null) {
            return publicUser;
        }
        publicUser.setId(user.getId());
        publicUser.setAccount(user.getAccount());
        publicUser.setNickname(user.getNickname());
        publicUser.setSex(user.getSex());
        publicUser.setCollege(user.getCollege());
        publicUser.setGrade(user.getGrade());
        publicUser.setIcon(user.getIcon());
        return publicUser;
    }

    private long countGoodsByManage(String manage) {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<Goods>();
        queryWrapper.eq(Goods::getManage, manage);
        return goodsService.count(queryWrapper);
    }

    private long countForumByManage(String manage) {
        LambdaQueryWrapper<Forum> queryWrapper = new LambdaQueryWrapper<Forum>();
        queryWrapper.eq(Forum::getManage, manage);
        return forumService.count(queryWrapper);
    }

    private long countOrdersByState(String state) {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<Orders>();
        queryWrapper.eq(Orders::getState, state);
        return ordersService.count(queryWrapper);
    }

    private LocalDate toLocalDate(Date value) {
        if (value == null) {
            return null;
        }
        return value.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private Date resolveEventTime(Date updateTime, Date sendTime) {
        return updateTime != null ? updateTime : sendTime;
    }

    private Map<LocalDate, Long> initLongTrendMap(LocalDate start, int days) {
        Map<LocalDate, Long> trendMap = new LinkedHashMap<LocalDate, Long>();
        for (int i = 0; i < days; i++) {
            trendMap.put(start.plusDays(i), 0L);
        }
        return trendMap;
    }

    private Map<LocalDate, Double> initDoubleTrendMap(LocalDate start, int days) {
        Map<LocalDate, Double> trendMap = new LinkedHashMap<LocalDate, Double>();
        for (int i = 0; i < days; i++) {
            trendMap.put(start.plusDays(i), 0D);
        }
        return trendMap;
    }

    private List<String> buildTrendDates(LocalDate start, int days) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<String> trendDates = new ArrayList<String>();
        for (int i = 0; i < days; i++) {
            trendDates.add(start.plusDays(i).format(formatter));
        }
        return trendDates;
    }

    private List<Long> buildLongTrendValues(Map<LocalDate, Long> trendMap) {
        return new ArrayList<Long>(trendMap.values());
    }

    private List<Double> buildMoneyTrendValues(Map<LocalDate, Double> trendMap) {
        return trendMap.values().stream()
                .map(value -> BigDecimal.valueOf(value == null ? 0D : value)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue())
                .collect(Collectors.toList());
    }

    @ApiOperation("Manager captcha")
    @GetMapping("/captcha")
    public void captcha(HttpSession session, HttpServletResponse response) {
        String code = String.valueOf(ValidateCodeUtils.generateValidateCode(4));
        session.setAttribute(MANAGER_CAPTCHA_SESSION_KEY, code);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/png");

        BufferedImage image = new BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(245, 248, 255));
            graphics.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT);

            graphics.setColor(new Color(210, 220, 235));
            graphics.setStroke(new BasicStroke(1.2F));
            graphics.drawRoundRect(0, 0, CAPTCHA_WIDTH - 1, CAPTCHA_HEIGHT - 1, 10, 10);

            for (int i = 0; i < 8; i++) {
                graphics.setColor(new Color(180 + (i * 7 % 50), 190 + (i * 9 % 40), 210 + (i * 5 % 35)));
                int x1 = (i * 17 + 9) % CAPTCHA_WIDTH;
                int y1 = (i * 13 + 7) % CAPTCHA_HEIGHT;
                int x2 = (i * 23 + 31) % CAPTCHA_WIDTH;
                int y2 = (i * 19 + 17) % CAPTCHA_HEIGHT;
                graphics.drawLine(x1, y1, x2, y2);
            }

            graphics.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 28));
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(i % 2 == 0 ? new Color(37, 99, 235) : new Color(30, 64, 175));
                graphics.drawString(String.valueOf(code.charAt(i)), 18 + i * 22, 30 + (i % 2 == 0 ? 0 : 2));
            }

            for (int i = 0; i < 30; i++) {
                graphics.setColor(new Color(140 + (i * 3 % 80), 160 + (i * 5 % 70), 190 + (i * 7 % 60)));
                int x = (i * 11 + 3) % CAPTCHA_WIDTH;
                int y = (i * 17 + 5) % CAPTCHA_HEIGHT;
                graphics.fillOval(x, y, 2, 2);
            }

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                ImageIO.write(image, "png", outputStream);
                outputStream.flush();
            }
        } catch (Exception e) {
            log.error("Generate manager captcha failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            graphics.dispose();
        }
    }

    @ApiOperation("Manager login")
    @PostMapping("/login")
    public Result<Manager> login(@RequestBody ManagerLoginDto loginDto, HttpSession session) {
        log.info("Manager login, account={}", loginDto == null ? null : loginDto.getAccount());
        trimManagerLoginFields(loginDto);
        if (loginDto == null
                || !StringUtils.hasText(loginDto.getAccount())
                || !StringUtils.hasText(loginDto.getPassword())
                || !StringUtils.hasText(loginDto.getCaptcha())) {
            return Result.error("Account, password and captcha are required");
        }
        String sessionCaptcha = trimValue((String) session.getAttribute(MANAGER_CAPTCHA_SESSION_KEY));
        if (!StringUtils.hasText(sessionCaptcha)) {
            return Result.error("Captcha expired, please refresh");
        }
        if (!sessionCaptcha.equalsIgnoreCase(loginDto.getCaptcha())) {
            session.removeAttribute(MANAGER_CAPTCHA_SESSION_KEY);
            return Result.error("Captcha is incorrect");
        }
        session.removeAttribute(MANAGER_CAPTCHA_SESSION_KEY);

        Manager manager = new Manager();
        manager.setAccount(loginDto.getAccount());
        manager.setPassword(loginDto.getPassword());
        Manager loginUser = managerService.login(manager);
        if (loginUser == null) {
            return Result.error("Login failed");
        }
        sanitizeManager(loginUser);
        session.setAttribute("user", loginUser.getId());
        return Result.success(loginUser);
    }

    @ApiOperation("Manager profile")
    @PostMapping("/profile")
    public Result<Manager> profile(@RequestBody(required = false) Manager manager) {
        Integer managerId = manager == null ? null : manager.getId();
        if (managerId == null) {
            return Result.error("Manager id required");
        }
        Manager currentManager = managerService.getById(managerId);
        if (currentManager == null) {
            return Result.error("Manager unavailable");
        }
        return Result.success(sanitizeManager(currentManager));
    }

    @ApiOperation("Manager update profile")
    @PostMapping("/updateProfile")
    public Result<Manager> updateProfile(@RequestBody(required = false) Manager manager) {
        trimManagerFields(manager);
        if (manager == null || manager.getId() == null) {
            return Result.error("Manager id required");
        }
        if (!StringUtils.hasText(manager.getAccount())) {
            return Result.error("Manager account required");
        }
        if (manager.getAccount().length() > 50) {
            return Result.error("Manager account must be within 50 characters");
        }
        if (manager.getAvatar() != null && manager.getAvatar().length() > 500) {
            return Result.error("Manager avatar is too long");
        }

        Manager currentManager = managerService.getById(manager.getId());
        if (currentManager == null) {
            return Result.error("Manager unavailable");
        }

        LambdaQueryWrapper<Manager> accountQueryWrapper = new LambdaQueryWrapper<Manager>();
        accountQueryWrapper.eq(Manager::getAccount, manager.getAccount());
        accountQueryWrapper.ne(Manager::getId, manager.getId());
        if (managerService.count(accountQueryWrapper) > 0) {
            return Result.error("Manager account already exists");
        }

        Manager updateManager = new Manager();
        updateManager.setId(currentManager.getId());
        updateManager.setAccount(manager.getAccount());
        updateManager.setAvatar(manager.getAvatar());
        if (!managerService.updateById(updateManager)) {
            return Result.error("Manager profile update failed");
        }

        Manager updatedManager = managerService.getById(currentManager.getId());
        if (updatedManager == null) {
            return Result.error("Manager profile updated but unavailable");
        }
        return Result.success(sanitizeManager(updatedManager));
    }

    @ApiOperation("Manager change password")
    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestBody ManagerPasswordDto dto) {
        if (dto == null || dto.getId() == null) {
            return Result.error("Manager id required");
        }
        String oldPassword = trimValue(dto.getOldPassword());
        String newPassword = trimValue(dto.getNewPassword());
        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            return Result.error("Old password and new password are required");
        }
        if (newPassword.length() < 6) {
            return Result.error("New password must be at least 6 characters");
        }
        if (newPassword.equals(oldPassword)) {
            return Result.error("New password must be different from old password");
        }

        Manager currentManager = managerService.getById(dto.getId());
        if (currentManager == null) {
            return Result.error("Manager unavailable");
        }
        if (!oldPassword.equals(trimValue(currentManager.getPassword()))) {
            return Result.error("Old password is incorrect");
        }

        Manager updateManager = new Manager();
        updateManager.setId(currentManager.getId());
        updateManager.setPassword(newPassword);
        return managerService.updateById(updateManager)
                ? Result.success("Password updated")
                : Result.error("Password update failed");
    }

    @ApiOperation("Manager dashboard")
    @PostMapping("/dashboard")
    public Result<ManagerDashboardDto> dashboard() {
        ManagerDashboardDto dashboardDto = new ManagerDashboardDto();
        List<Goods> allGoods = goodsService.list();
        List<Forum> allForums = forumService.list();
        List<Orders> allOrders = ordersService.list();

        dashboardDto.setUserCount((long) userService.count());
        dashboardDto.setGoodsCount((long) allGoods.size());
        dashboardDto.setGoodsPendingCount(countGoodsByManage("0"));
        dashboardDto.setGoodsApprovedCount(countGoodsByManage("1"));
        dashboardDto.setGoodsRejectedCount(countGoodsByManage("2"));
        dashboardDto.setGoodsSoldOutCount(countGoodsByManage("3"));
        dashboardDto.setForumCount((long) allForums.size());
        dashboardDto.setForumPendingCount(countForumByManage("0"));
        dashboardDto.setForumApprovedCount(countForumByManage("1"));
        dashboardDto.setForumRejectedCount(countForumByManage("2"));
        dashboardDto.setOrderCount((long) allOrders.size());
        dashboardDto.setUnpaidOrderCount(countOrdersByState("1"));
        dashboardDto.setPaidOrderCount(countOrdersByState("2"));
        dashboardDto.setShippingOrderCount(countOrdersByState("3"));
        dashboardDto.setCompletedOrderCount(countOrdersByState("4"));
        dashboardDto.setCancelledOrderCount(countOrdersByState("5"));
        dashboardDto.setBannerCount((long) bannerService.count());
        dashboardDto.setNoticeCount((long) noticeService.count());
        dashboardDto.setCategoryCount((long) categoryService.count());

        double paidAmount = allOrders.stream()
                .filter(item -> item != null && ("2".equals(item.getState()) || "3".equals(item.getState()) || "4".equals(item.getState())))
                .map(Orders::getPrice)
                .filter(item -> item != null && Double.isFinite(item))
                .reduce(0D, Double::sum);
        dashboardDto.setPaidAmount(BigDecimal.valueOf(paidAmount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue());

        Comparator<Goods> goodsComparator = Comparator
                .comparing(Goods::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Goods::getSendTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Goods::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        dashboardDto.setRecentGoods(allGoods.stream()
                .sorted(goodsComparator)
                .limit(5)
                .collect(Collectors.toList()));

        Comparator<Forum> forumComparator = Comparator
                .comparing(Forum::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Forum::getSendTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Forum::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        dashboardDto.setRecentForums(allForums.stream()
                .sorted(forumComparator)
                .limit(5)
                .collect(Collectors.toList()));

        Comparator<Orders> ordersComparator = Comparator
                .comparing(Orders::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Orders::getSendTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Orders::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        dashboardDto.setRecentOrders(allOrders.stream()
                .sorted(ordersComparator)
                .limit(5)
                .map(this::toOrdersDto)
                .collect(Collectors.toList()));

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        int trendDays = 7;
        Map<LocalDate, Long> goodsTrend = initLongTrendMap(startDate, trendDays);
        Map<LocalDate, Long> forumTrend = initLongTrendMap(startDate, trendDays);
        Map<LocalDate, Long> orderTrend = initLongTrendMap(startDate, trendDays);
        Map<LocalDate, Double> paidAmountTrend = initDoubleTrendMap(startDate, trendDays);

        allGoods.stream()
                .filter(item -> item != null)
                .map(item -> toLocalDate(resolveEventTime(item.getUpdateTime(), item.getSendTime())))
                .filter(day -> day != null && !day.isBefore(startDate) && !day.isAfter(today))
                .forEach(day -> goodsTrend.put(day, goodsTrend.get(day) + 1));

        allForums.stream()
                .filter(item -> item != null)
                .map(item -> toLocalDate(resolveEventTime(item.getUpdateTime(), item.getSendTime())))
                .filter(day -> day != null && !day.isBefore(startDate) && !day.isAfter(today))
                .forEach(day -> forumTrend.put(day, forumTrend.get(day) + 1));

        allOrders.stream()
                .filter(item -> item != null)
                .forEach(item -> {
                    LocalDate orderDay = toLocalDate(resolveEventTime(item.getUpdateTime(), item.getSendTime()));
                    if (orderDay != null && !orderDay.isBefore(startDate) && !orderDay.isAfter(today)) {
                        orderTrend.put(orderDay, orderTrend.get(orderDay) + 1);
                        if ("2".equals(item.getState()) || "3".equals(item.getState()) || "4".equals(item.getState())) {
                            double amount = item.getPrice() != null && Double.isFinite(item.getPrice()) ? item.getPrice() : 0D;
                            paidAmountTrend.put(orderDay, paidAmountTrend.get(orderDay) + amount);
                        }
                    }
                });

        dashboardDto.setTrendDates(buildTrendDates(startDate, trendDays));
        dashboardDto.setGoodsTrend(buildLongTrendValues(goodsTrend));
        dashboardDto.setForumTrend(buildLongTrendValues(forumTrend));
        dashboardDto.setOrderTrend(buildLongTrendValues(orderTrend));
        dashboardDto.setPaidAmountTrend(buildMoneyTrendValues(paidAmountTrend));

        return Result.success(dashboardDto);
    }

    @ApiOperation("Manager list users")
    @PostMapping("/getAllUser")
    public Result<List<User>> getAllUser() {
        return Result.success(userService.list());
    }

    @ApiOperation("Manager edit user")
    @PostMapping("/editUser")
    public Result<String> editUser(@RequestBody User user) {
        if (user == null || user.getId() == null) {
            return Result.error("User id required");
        }
        trimUserFields(user);
        Result<String> balanceValidation = normalizeUserBalance(user);
        if (balanceValidation != null) {
            return balanceValidation;
        }
        if (user.getAccount() != null && !StringUtils.hasText(user.getAccount())) {
            return Result.error("Account cannot be blank");
        }
        if (user.getPassword() != null && !StringUtils.hasText(user.getPassword())) {
            return Result.error("Password cannot be blank");
        }
        return userService.updateById(user)
                ? Result.success("Edit success")
                : Result.error("Edit failed");
    }

    @ApiOperation("Manager delete user")
    @PostMapping("/delUser")
    public Result<String> delUser(@RequestBody User user) {
        if (user == null || user.getId() == null) {
            return Result.error("User id required");
        }
        return userService.removeById(user.getId())
                ? Result.success("Delete success")
                : Result.error("Delete failed");
    }

    @ApiOperation("Manager add user")
    @PostMapping("/addUser")
    public Result<String> addUser(@RequestBody User user) {
        if (user == null || !StringUtils.hasText(user.getAccount()) || !StringUtils.hasText(user.getPassword())) {
            return Result.error("Account and password are required");
        }
        trimUserFields(user);
        if (!StringUtils.hasText(user.getNickname())
                || !StringUtils.hasText(user.getSex())
                || !StringUtils.hasText(user.getTel())
                || !StringUtils.hasText(user.getIdcard())
                || !StringUtils.hasText(user.getCollege())
                || !StringUtils.hasText(user.getGrade())) {
            return Result.error("Nickname, sex, tel, idcard, college and grade are required");
        }
        if (!"\u7537".equals(user.getSex()) && !"\u5973".equals(user.getSex())) {
            return Result.error("Sex must be \u7537 or \u5973");
        }
        Result<String> balanceValidation = normalizeUserBalance(user);
        if (balanceValidation != null) {
            return balanceValidation;
        }
        return userService.register(user);
    }

    @ApiOperation("Manager search users")
    @PostMapping("/getUserByTel")
    public Result<List<User>> getUserByTel(@RequestBody User user) {
        String keyword = trimValue(user == null ? null : user.getTel());
        List<User> allUsers = userService.list();
        if (!StringUtils.hasText(keyword)) {
            return Result.success(allUsers);
        }
        List<User> list = allUsers.stream()
                .filter(item -> containsKeyword(item.getAccount(), keyword)
                        || containsKeyword(item.getTel(), keyword)
                        || containsKeyword(item.getNickname(), keyword))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @ApiOperation("Manager list banners")
    @PostMapping("/getAllBanner")
    public Result<List<Banner>> getAllBanner() {
        return Result.success(bannerService.list());
    }

    @ApiOperation("Manager edit banner")
    @PostMapping("/editBanner")
    public Result<String> editBanner(@RequestBody Banner banner) {
        if (banner == null || banner.getId() == null) {
            return Result.error("Banner id required");
        }
        return bannerService.updateById(banner)
                ? Result.success("Edit success")
                : Result.error("Edit failed");
    }

    @ApiOperation("Manager delete banner")
    @PostMapping("/delBanner")
    public Result<String> delBanner(@RequestBody Banner banner) {
        if (banner == null || banner.getId() == null) {
            return Result.error("Banner id required");
        }
        return bannerService.removeById(banner.getId())
                ? Result.success("Delete success")
                : Result.error("Delete failed");
    }

    @ApiOperation("Manager add banner")
    @PostMapping("/addBanner")
    public Result<String> addBanner(@RequestBody Banner banner) {
        return bannerService.save(banner)
                ? Result.success("Add success")
                : Result.error("Add failed");
    }

    @ApiOperation("Manager list notices")
    @PostMapping("/getAllNotice")
    public Result<List<Notice>> getAllNotice() {
        return Result.success(noticeService.list());
    }

    @ApiOperation("Manager edit notice")
    @PostMapping("/editNotice")
    public Result<String> editNotice(@RequestBody Notice notice) {
        if (notice == null || notice.getId() == null) {
            return Result.error("Notice id required");
        }
        return noticeService.updateById(notice)
                ? Result.success("Edit success")
                : Result.error("Edit failed");
    }

    @ApiOperation("Manager delete notice")
    @PostMapping("/delNotice")
    public Result<String> delNotice(@RequestBody Notice notice) {
        if (notice == null || notice.getId() == null) {
            return Result.error("Notice id required");
        }
        return noticeService.removeById(notice.getId())
                ? Result.success("Delete success")
                : Result.error("Delete failed");
    }

    @ApiOperation("Manager add notice")
    @PostMapping("/addNotice")
    public Result<String> addNotice(@RequestBody Notice notice) {
        return noticeService.save(notice)
                ? Result.success("Add success")
                : Result.error("Add failed");
    }

    @ApiOperation("Manager search notices")
    @PostMapping("/getNoticeByContent")
    public Result<List<Notice>> getNoticeByContent(@RequestBody Notice notice) {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();
        if (notice != null && StringUtils.hasText(notice.getContent())) {
            queryWrapper.like(Notice::getContent, notice.getContent());
        }
        return Result.success(noticeService.list(queryWrapper));
    }

    @ApiOperation("Manager list menus")
    @PostMapping("/getAllMenu")
    public Result<List<Menu>> getAllMenu() {
        return Result.success(menuService.list());
    }

    @ApiOperation("Manager edit menu")
    @PostMapping("/editMenu")
    public Result<String> editMenu(@RequestBody Menu menu) {
        if (menu == null || menu.getId() == null) {
            return Result.error("Menu id required");
        }
        return menuService.updateById(menu)
                ? Result.success("Edit success")
                : Result.error("Edit failed");
    }

    @ApiOperation("Manager delete menu")
    @PostMapping("/delMenu")
    public Result<String> delMenu(@RequestBody Menu menu) {
        if (menu == null || menu.getId() == null) {
            return Result.error("Menu id required");
        }
        return menuService.removeById(menu.getId())
                ? Result.success("Delete success")
                : Result.error("Delete failed");
    }

    @ApiOperation("Manager add menu")
    @PostMapping("/addMenu")
    public Result<String> addMenu(@RequestBody Menu menu) {
        return menuService.save(menu)
                ? Result.success("Add success")
                : Result.error("Add failed");
    }

    @ApiOperation("Manager list categories")
    @PostMapping("/getAllCategory")
    public Result<List<Category>> getAllCategory() {
        return Result.success(categoryService.list());
    }

    @ApiOperation("Manager edit category")
    @PostMapping("/editCategory")
    public Result<String> editCategory(@RequestBody Category category) {
        if (category == null || category.getCateid() == null) {
            return Result.error("Category id required");
        }
        trimCategoryFields(category);
        if (!StringUtils.hasText(category.getCatename())) {
            return Result.error("Category name required");
        }
        return categoryService.updateById(category)
                ? Result.success("Edit success")
                : Result.error("Edit failed");
    }

    @ApiOperation("Manager delete category")
    @PostMapping("/delCategory")
    public Result<String> delCategory(@RequestBody Category category) {
        if (category == null || category.getCateid() == null) {
            return Result.error("Category id required");
        }
        cleanupCatechildByCateid(category.getCateid());
        return categoryService.removeById(category.getCateid())
                ? Result.success("Delete success")
                : Result.error("Delete failed");
    }

    @ApiOperation("Manager add category")
    @PostMapping("/addCategory")
    public Result<String> addCategory(@RequestBody Category category) {
        if (category == null) {
            return Result.error("Category data required");
        }
        trimCategoryFields(category);
        if (!StringUtils.hasText(category.getCatename())) {
            return Result.error("Category name required");
        }
        return categoryService.save(category)
                ? Result.success("Add success")
                : Result.error("Add failed");
    }

    @ApiOperation("Manager list category goods relations")
    @PostMapping("/getAllCatechild")
    public Result<List<Catechild>> getAllCatechild() {
        return Result.success(normalizeCatechildList(catechildService.list()));
    }

    @ApiOperation("Manager edit category goods relation")
    @PostMapping("/editCatechild")
    public Result<String> editCatechild(@RequestBody Catechild catechild) {
        if (catechild == null || catechild.getChildid() == null) {
            return Result.error("Relation id required");
        }
        if (catechild.getCateid() != null && categoryService.getById(catechild.getCateid()) == null) {
            return Result.error("Category unavailable");
        }
        if (StringUtils.hasText(catechild.getGoodid())) {
            catechild.setGoodid(catechild.getGoodid().trim());
            Goods goods = goodsService.getById(catechild.getGoodid());
            if (goods == null) {
                return Result.error("Goods unavailable");
            }
            if (!StringUtils.hasText(catechild.getChildname())) {
                catechild.setChildname(goods.getName());
            }
            if (!StringUtils.hasText(catechild.getImage())) {
                catechild.setImage(resolveGoodsCover(goods));
            }
        }
        return catechildService.updateById(catechild)
                ? Result.success("Edit success")
                : Result.error("Edit failed");
    }

    @ApiOperation("Manager delete category goods relation")
    @PostMapping("/delCatechild")
    public Result<String> delCatechild(@RequestBody Catechild catechild) {
        if (catechild == null || catechild.getChildid() == null) {
            return Result.error("Relation id required");
        }
        return catechildService.removeById(catechild.getChildid())
                ? Result.success("Delete success")
                : Result.error("Delete failed");
    }

    @ApiOperation("Manager add category goods relation")
    @PostMapping("/addCatechild")
    public Result<String> addCatechild(String cateid, String goodsid) {
        if (!StringUtils.hasText(cateid) || !StringUtils.hasText(goodsid)) {
            return Result.error("Category and goods required");
        }
        Category category = categoryService.getById(Integer.valueOf(cateid));
        if (category == null) {
            return Result.error("Category unavailable");
        }
        Goods goods = goodsService.getById(goodsid.trim());
        if (goods == null) {
            return Result.error("Goods unavailable");
        }
        cleanupCatechildByGoodsId(goods.getId());
        Catechild catechild = new Catechild();
        catechild.setCateid(Integer.valueOf(cateid));
        catechild.setGoodid(goodsid.trim());
        catechild.setImage(resolveGoodsCover(goods));
        catechild.setChildname(goods.getName());
        return catechildService.save(catechild)
                ? Result.success("Add success")
                : Result.error("Add failed");
    }

    @ApiOperation("Manager list goods")
    @PostMapping("/getAllGoods")
    public Result<List<Goods>> getAllGoods() {
        List<Goods> goodsList = goodsService.list();
        List<Integer> sellerIds = goodsList.stream()
                .map(Goods::getSendUser)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, User> userMap = Collections.emptyMap();
        if (!sellerIds.isEmpty()) {
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<User>();
            userQueryWrapper.in(User::getId, sellerIds);
            userMap = userService.list(userQueryWrapper).stream()
                    .filter(user -> user != null && user.getId() != null)
                    .collect(Collectors.toMap(User::getId, user -> user));
        }

        for (Goods goods : goodsList) {
            if (goods == null || goods.getSendUser() == null) {
                continue;
            }
            User seller = userMap.get(goods.getSendUser());
            if (seller != null) {
                goods.setSellerTel(seller.getTel());
                if (!StringUtils.hasText(goods.getContactWay())) {
                    goods.setContactWay(seller.getTel());
                }
            }
        }
        return Result.success(goodsList);
    }

    @ApiOperation("Manager edit goods")
    @PostMapping("/editGoods")
    public Result<String> editGoods(@RequestBody Goods goods) {
        if (goods == null || goods.getId() == null) {
            return Result.error("Goods id required");
        }
        if (goods.getCateid() != null && categoryService.getById(goods.getCateid()) == null) {
            return Result.error("Category unavailable");
        }
        if (goods.getNumber() != null && goods.getNumber() < 0) {
            return Result.error("Quantity cannot be less than 0");
        }
        boolean updated = goodsService.updateById(goods);
        if (!updated) {
            return Result.error("Edit failed");
        }
        if (!syncCatechildRelation(goods)) {
            return Result.error("Edit success but category relation sync failed");
        }
        return Result.success("Edit success");
    }

    @ApiOperation("Manager delete goods")
    @PostMapping("/delGoods")
    public Result<String> delGoods(@RequestBody Goods goods) {
        if (goods == null || goods.getId() == null) {
            return Result.error("Goods id required");
        }
        cleanupCatechildByGoodsId(goods.getId());
        return goodsService.removeById(goods.getId())
                ? Result.success("Delete success")
                : Result.error("Delete failed");
    }

    @ApiOperation("Manager add goods")
    @PostMapping("/addGoods")
    public Result<String> addGoods(@RequestBody Goods goods) {
        if (goods == null) {
            return Result.error("Goods data required");
        }
        if (goods.getCateid() != null && categoryService.getById(goods.getCateid()) == null) {
            return Result.error("Category unavailable");
        }
        if (goods.getNumber() != null && goods.getNumber() < 0) {
            return Result.error("Quantity cannot be less than 0");
        }
        boolean saved = goodsService.save(goods);
        if (!saved) {
            return Result.error("Add failed");
        }
        if (!syncCatechildRelation(goods)) {
            return Result.error("Add success but category relation sync failed");
        }
        return Result.success("Add success");
    }

    @ApiOperation("Manager search goods by name")
    @PostMapping("/getGoodsByName")
    public Result<List<Goods>> getGoodsByName(@RequestBody Goods goods) {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        if (goods != null && StringUtils.hasText(goods.getName())) {
            queryWrapper.like(Goods::getName, goods.getName());
        }
        return Result.success(goodsService.list(queryWrapper));
    }

    @ApiOperation("Manager approve goods")
    @PostMapping("/checkGoods")
    public Result<String> checkGoods(Integer id, String manage) {
        if (id == null || !StringUtils.hasText(manage)) {
            return Result.error("Goods id and manage status required");
        }
        String targetManage = manage.trim();
        if (!"1".equals(targetManage) && !"2".equals(targetManage) && !"3".equals(targetManage)) {
            return Result.error("Unsupported goods status");
        }

        Goods currentGoods = goodsService.getById(id);
        if (currentGoods == null) {
            return Result.error("Goods unavailable");
        }

        String currentManage = StringUtils.hasText(currentGoods.getManage()) ? currentGoods.getManage().trim() : "";
        boolean validTransition = ("0".equals(currentManage) && ("1".equals(targetManage) || "2".equals(targetManage)))
                || ("1".equals(currentManage) && ("2".equals(targetManage) || "3".equals(targetManage)));
        if (!validTransition) {
            return Result.error("Current goods status does not support this operation");
        }

        Goods goods = new Goods();
        goods.setId(id);
        goods.setManage(targetManage);
        return goodsService.updateById(goods)
                ? Result.success("Operate success")
                : Result.error("Operate failed");
    }

    @ApiOperation("Manager list forums")
    @PostMapping("/getAllForum")
    public Result<List<Forum>> getAllForum() {
        List<Forum> forums = forumService.list().stream()
                .sorted(Comparator.comparing(Forum::getUpdateTime, Comparator.nullsLast(Date::compareTo)).reversed()
                        .thenComparing(Forum::getSendTime, Comparator.nullsLast(Date::compareTo)).reversed()
                        .thenComparing(Forum::getId, Comparator.nullsLast(Integer::compareTo)).reversed())
                .collect(Collectors.toList());
        return Result.success(forums);
    }

    @ApiOperation("Manager forum detail")
    @PostMapping("/getForumDetail")
    public Result<ForumDetailDto> getForumDetail(Integer id) {
        if (id == null) {
            return Result.error("Forum id required");
        }
        Forum forum = forumService.getById(id);
        if (forum == null) {
            return Result.error("Forum unavailable");
        }

        ForumDetailDto forumDetailDto = new ForumDetailDto();
        BeanUtils.copyProperties(forum, forumDetailDto);
        forumDetailDto.setImgList(splitForumImages(forum.getImgs()));
        forumDetailDto.setCommentCount((int) forumCommentService.countVisibleByForumId(id));
        if (forum.getSendUser() != null) {
            forumDetailDto.setUser(buildPublicUser(userService.getById(forum.getSendUser())));
        }
        return Result.success(forumDetailDto);
    }

    @ApiOperation("Manager edit forum")
    @PostMapping("/editForum")
    public Result<String> editForum(@RequestBody Forum forum) {
        if (forum == null || forum.getId() == null) {
            return Result.error("Forum id required");
        }
        return forumService.updateById(forum)
                ? Result.success("Edit success")
                : Result.error("Edit failed");
    }

    @ApiOperation("Manager delete forum")
    @PostMapping("/delForum")
    public Result<String> delForum(@RequestBody Forum forum) {
        if (forum == null || forum.getId() == null) {
            return Result.error("Forum id required");
        }

        LambdaQueryWrapper<ForumComment> commentQueryWrapper = new LambdaQueryWrapper<ForumComment>();
        commentQueryWrapper.eq(ForumComment::getForumId, forum.getId());
        forumCommentService.remove(commentQueryWrapper);

        return forumService.removeById(forum.getId())
                ? Result.success("Delete success")
                : Result.error("Delete failed");
    }

    @ApiOperation("Manager add forum")
    @PostMapping("/addForum")
    public Result<String> addForum(@RequestBody Forum forum) {
        return forumService.save(forum)
                ? Result.success("Add success")
                : Result.error("Add failed");
    }

    @ApiOperation("Manager search forums by content")
    @PostMapping("/getForumByContent")
    public Result<List<Forum>> getForumByContent(@RequestBody Forum forum) {
        LambdaQueryWrapper<Forum> queryWrapper = new LambdaQueryWrapper<>();
        if (forum != null && StringUtils.hasText(forum.getContent())) {
            queryWrapper.like(Forum::getContent, forum.getContent());
        }
        return Result.success(forumService.list(queryWrapper));
    }

    @ApiOperation("Manager approve forum")
    @PostMapping("/checkForum")
    public Result<String> checkForum(Integer id, String manage) {
        if (id == null || !StringUtils.hasText(manage)) {
            return Result.error("Forum id and manage status required");
        }
        Forum forum = new Forum();
        forum.setId(id);
        forum.setManage(manage);
        return forumService.updateById(forum)
                ? Result.success("Operate success")
                : Result.error("Operate failed");
    }

    @ApiOperation("Manager list forum comments by forum id")
    @PostMapping("/getForumComments")
    public Result<List<ForumCommentDto>> getForumComments(Integer forumId) {
        if (forumId == null) {
            return Result.error("Forum id required");
        }
        return Result.success(forumCommentService.listByForumId(forumId));
    }

    @ApiOperation("Manager delete forum comment")
    @PostMapping("/delForumComment")
    public Result<String> delForumComment(@RequestBody ForumComment forumComment) {
        if (forumComment == null || forumComment.getId() == null) {
            return Result.error("Comment id required");
        }
        return forumCommentService.removeCommentCascadeById(forumComment.getId())
                ? Result.success("Delete success")
                : Result.error("Delete failed");
    }
}

