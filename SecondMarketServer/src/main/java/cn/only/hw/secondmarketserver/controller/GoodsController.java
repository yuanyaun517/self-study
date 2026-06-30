package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.entity.Catechild;
import cn.only.hw.secondmarketserver.entity.Category;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.service.CatechildService;
import cn.only.hw.secondmarketserver.service.CategoryService;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.util.CustomException;
import cn.only.hw.secondmarketserver.util.DealTypeUtils;
import cn.only.hw.secondmarketserver.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * 商品控制器
 * 处理商品相关的HTTP请求，包括商品列表、详情、发布、搜索等功能
 */
@RestController
@RequestMapping("/goods")
@Slf4j
@Api(tags = "Goods")
public class GoodsController {

    /**
     * 商品服务
     */
    @Autowired
    private GoodsService goodsService;

    /**
     * 子分类服务
     */
    @Autowired
    private CatechildService catechildService;

    /**
     * 分类服务
     */
    @Autowired
    private CategoryService categoryService;

    @ApiOperation("List approved goods")
    @PostMapping("/list")
    public Result<List<Goods>> list() {
        log.info("List approved goods");
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getManage, "1");
        queryWrapper.gt(Goods::getNumber, 0);
        List<Goods> list = goodsService.list(queryWrapper);
        if (list.isEmpty()) {
            return Result.error("No data");
        }
        return Result.success(list);
    }

    @ApiOperation("Get goods by id")
    @PostMapping("/getById")
    public Result<Goods> getById(Integer id, Integer userid) {
        log.info("Get goods by id: {}, userid: {}", id, userid);
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return Result.error("Goods unavailable");
        }
        if (!isVisibleGoods(goods) && !isOwnerGoods(goods, userid)) {
            return Result.error("Goods unavailable");
        }

        attachCateid(goods);
        String[] imgStrs = buildImgList(goods);
        return Result.success(goods).add("imgs", Arrays.asList(imgStrs));
    }

    @ApiOperation("Get goods by type")
    @PostMapping("/getByType")
    public Result<List<Goods>> getByType(String type) {
        log.info("Get goods by type: {}", type);
        List<Goods> list = goodsService.getByType(type);
        if (list.isEmpty()) {
            return Result.error("No data");
        }
        return Result.success(list);
    }

    @ApiOperation("Create goods")
    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> save(@RequestBody Goods goods) {
        log.info("Create goods: {}", goods);
        if (goods == null) {
            return Result.error("Goods data required");
        }

        Result<String> submitValidation = prepareGoodsForSubmit(goods);
        if (submitValidation != null) {
            return submitValidation;
        }

        goods.setManage("0");
        boolean saved = goodsService.save(goods);
        if (saved) {
            saveCatechildRelation(goods);
            return Result.success("Create success");
        }
        return Result.error("Create failed");
    }

    @ApiOperation("Search goods")
    @PostMapping("/searchGoods")
    public Result<List<Goods>> searchGoods(String type, String name, String des) {
        log.info("Search goods: {}, {}, {}", type, name, des);
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getManage, "1");
        queryWrapper.gt(Goods::getNumber, 0);
        queryWrapper.and(wrapper -> wrapper.like(Goods::getType, type)
                .or().like(Goods::getName, name)
                .or().like(Goods::getDescribes, des));
        List<Goods> list = goodsService.list(queryWrapper);
        if (list.isEmpty()) {
            return Result.error("No data");
        }
        return Result.success(list);
    }

    @ApiOperation("Get goods by user id")
    @PostMapping("/getByUserId")
    public Result<List<Goods>> getByUserId(Integer userid) {
        log.info("Get goods by user id: {}", userid);
        if (userid == null) {
            return Result.error("User id required");
        }

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getSendUser, userid);
        queryWrapper.orderByDesc(Goods::getUpdateTime)
                .orderByDesc(Goods::getSendTime)
                .orderByDesc(Goods::getId);
        return Result.success(goodsService.list(queryWrapper));
    }

    @ApiOperation("Update goods price by owner")
    @PostMapping("/updatePrice")
    public Result<String> updatePrice(@RequestBody Goods goods) {
        log.info("Update goods price: {}", goods);
        if (goods == null || goods.getId() == null || goods.getSendUser() == null) {
            return Result.error("Goods id and user id required");
        }

        Result<String> priceValidation = validatePrice(goods.getPrice());
        if (priceValidation != null) {
            return priceValidation;
        }

        Goods currentGoods = goodsService.getById(goods.getId());
        Result<String> ownerValidation = validateOwnerOperation(currentGoods, goods.getSendUser());
        if (ownerValidation != null) {
            return ownerValidation;
        }
        if ("3".equals(currentGoods.getManage())) {
            return Result.error("Goods already sold out");
        }

        Goods updateGoods = new Goods();
        updateGoods.setId(currentGoods.getId());
        updateGoods.setPrice(normalizePrice(goods.getPrice()));
        return goodsService.updateById(updateGoods)
                ? Result.success("Price updated")
                : Result.error("Price update failed");
    }

    @ApiOperation("Resubmit rejected goods by owner")
    @PostMapping("/resubmit")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> resubmit(@RequestBody Goods goods) {
        log.info("Resubmit goods: {}", goods);
        if (goods == null || goods.getId() == null || goods.getSendUser() == null) {
            return Result.error("Goods id and user id required");
        }

        Result<String> submitValidation = prepareGoodsForSubmit(goods);
        if (submitValidation != null) {
            return submitValidation;
        }

        Goods currentGoods = goodsService.getById(goods.getId());
        Result<String> ownerValidation = validateOwnerOperation(currentGoods, goods.getSendUser());
        if (ownerValidation != null) {
            return ownerValidation;
        }
        if (!"2".equals(currentGoods.getManage())) {
            return Result.error("Only rejected goods can be resubmitted");
        }

        Goods updateGoods = new Goods();
        updateGoods.setId(currentGoods.getId());
        updateGoods.setName(goods.getName());
        updateGoods.setType(goods.getType());
        updateGoods.setPrice(goods.getPrice());
        updateGoods.setNumber(goods.getNumber());
        updateGoods.setStatus(goods.getStatus());
        updateGoods.setDealtypy(goods.getDealtypy());
        updateGoods.setCateid(goods.getCateid());
        updateGoods.setImgs(goods.getImgs());
        updateGoods.setDescribes(goods.getDescribes());
        updateGoods.setIcon(goods.getIcon());
        updateGoods.setManage("0");

        boolean updated = goodsService.updateById(updateGoods);
        if (updated) {
            saveCatechildRelation(updateGoods);
            return Result.success("Resubmit success");
        }
        return Result.error("Resubmit failed");
    }

    @ApiOperation("Sold out goods by owner")
    @PostMapping("/soldOut")
    public Result<String> soldOut(@RequestBody Goods goods) {
        log.info("Sold out goods: {}", goods);
        if (goods == null || goods.getId() == null || goods.getSendUser() == null) {
            return Result.error("Goods id and user id required");
        }

        Goods currentGoods = goodsService.getById(goods.getId());
        Result<String> ownerValidation = validateOwnerOperation(currentGoods, goods.getSendUser());
        if (ownerValidation != null) {
            return ownerValidation;
        }
        if ("3".equals(currentGoods.getManage())) {
            return Result.error("Goods already sold out");
        }

        Goods updateGoods = new Goods();
        updateGoods.setId(currentGoods.getId());
        updateGoods.setManage("3");
        return goodsService.updateById(updateGoods)
                ? Result.success("Goods sold out success")
                : Result.error("Goods sold out failed");
    }

    private boolean isVisibleGoods(Goods goods) {
        return goods != null
                && "1".equals(goods.getManage())
                && goods.getNumber() != null
                && goods.getNumber() > 0;
    }

    private boolean isOwnerGoods(Goods goods, Integer userid) {
        return goods != null
                && userid != null
                && goods.getSendUser() != null
                && goods.getSendUser().equals(userid);
    }

    private Result<String> validateOwnerOperation(Goods goods, Integer userid) {
        if (goods == null) {
            return Result.error("Goods unavailable");
        }
        if (!isOwnerGoods(goods, userid)) {
            return Result.error("Only publisher can operate this goods");
        }
        return null;
    }

    private Result<String> validatePrice(Double price) {
        if (price == null || !Double.isFinite(price)) {
            return Result.error("Valid price required");
        }
        if (price < 0) {
            return Result.error("Price cannot be less than 0");
        }
        return null;
    }

    private Double normalizePrice(Double price) {
        return BigDecimal.valueOf(price)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Result<String> prepareGoodsForSubmit(Goods goods) {
        if (goods.getCateid() == null) {
            return Result.error("Category required");
        }

        Category category = categoryService.getById(goods.getCateid());
        if (category == null) {
            return Result.error("Category unavailable");
        }

        Result<String> priceValidation = validatePrice(goods.getPrice());
        if (priceValidation != null) {
            return priceValidation;
        }

        String normalizedDealType = DealTypeUtils.normalizeForSave(goods.getDealtypy());
        if (normalizedDealType == null) {
            return Result.error("Unsupported deal type");
        }

        if (goods.getNumber() != null && goods.getNumber() < 0) {
            return Result.error("Quantity cannot be less than 0");
        }

        goods.setName(trimText(goods.getName()));
        goods.setType(trimText(goods.getType()));
        goods.setStatus(trimText(goods.getStatus()));
        goods.setDescribes(trimText(goods.getDescribes()));
        goods.setImgs(trimText(goods.getImgs()));
        goods.setIcon(trimText(goods.getIcon()));
        goods.setDealtypy(normalizedDealType);
        goods.setPrice(normalizePrice(goods.getPrice()));
        return null;
    }

    private String[] buildImgList(Goods goods) {
        if (goods.getImgs() != null && !goods.getImgs().trim().isEmpty()) {
            return goods.getImgs().split(",");
        }
        if (goods.getIcon() != null && !goods.getIcon().trim().isEmpty()) {
            return new String[]{goods.getIcon()};
        }
        return new String[0];
    }

    private void attachCateid(Goods goods) {
        if (goods == null || goods.getId() == null) {
            return;
        }

        LambdaQueryWrapper<Catechild> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Catechild::getGoodid, String.valueOf(goods.getId()));
        queryWrapper.last("limit 1");
        List<Catechild> catechildList = catechildService.list(queryWrapper);
        if (!catechildList.isEmpty()) {
            goods.setCateid(catechildList.get(0).getCateid());
        }
    }

    private void saveCatechildRelation(Goods goods) {
        if (goods.getId() == null) {
            throw new CustomException("Goods id missing after create");
        }

        LambdaQueryWrapper<Catechild> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Catechild::getGoodid, String.valueOf(goods.getId()));
        catechildService.remove(queryWrapper);

        Catechild catechild = new Catechild();
        catechild.setCateid(goods.getCateid());
        catechild.setGoodid(String.valueOf(goods.getId()));
        catechild.setImage(resolveCatechildImage(goods));
        catechild.setChildname(goods.getName());

        if (!catechildService.save(catechild)) {
            throw new CustomException("Save goods category relation failed");
        }
    }

    private String resolveCatechildImage(Goods goods) {
        if (StringUtils.hasText(goods.getIcon())) {
            return goods.getIcon();
        }
        if (StringUtils.hasText(goods.getImgs())) {
            return goods.getImgs().split(",")[0];
        }
        return "";
    }

    private String trimText(String value) {
        return value == null ? null : value.trim();
    }
}
