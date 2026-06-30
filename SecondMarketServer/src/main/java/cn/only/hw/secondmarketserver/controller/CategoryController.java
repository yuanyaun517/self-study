package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.entity.Catechild;
import cn.only.hw.secondmarketserver.entity.Category;
import cn.only.hw.secondmarketserver.entity.Categorys;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.service.CatechildService;
import cn.only.hw.secondmarketserver.service.CategoryService;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.util.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类控制器
 * 处理商品分类相关的HTTP请求，包括分类列表、子分类等功能
 */
@RestController
@RequestMapping("/category")
@Slf4j
@Api(tags = "Category")
public class CategoryController {

    /**
     * 分类服务
     */
    @Autowired
    private CategoryService categoryService;

    /**
     * 子分类服务
     */
    @Autowired
    private CatechildService catechildService;

    /**
     * 商品服务
     */
    @Autowired
    private GoodsService goodsService;

    @ApiOperation("List categories")
    @PostMapping("/list")
    public Result<List<Categorys>> list() {
        log.info("List categories");
        List<Category> categories = categoryService.list();

        List<Categorys> result = new ArrayList<>();
        for (Category category : categories) {
            List<Catechild> catechilds = catechildService.getByCateId(category.getCateid())
                    .stream()
                    .map(this::normalizeVisibleCatechild)
                    .filter(item -> item != null)
                    .collect(Collectors.toList());

            Categorys categorys = new Categorys();
            categorys.setCateid(category.getCateid());
            categorys.setCatename(category.getCatename());
            categorys.setIshavechild(!catechilds.isEmpty());
            categorys.setChildren(catechilds);
            result.add(categorys);
        }

        return Result.success(result);
    }

    private Catechild normalizeVisibleCatechild(Catechild catechild) {
        Goods goods = goodsService.getById(catechild.getGoodid());
        if (!isVisibleGoods(goods)) {
            return null;
        }

        catechild.setChildname(resolveChildName(goods, catechild.getChildname()));
        catechild.setImage(resolveChildImage(goods, catechild.getImage()));
        return catechild;
    }

    private boolean isVisibleGoods(Goods goods) {
        return goods != null
                && "1".equals(goods.getManage())
                && goods.getNumber() != null
                && goods.getNumber() > 0;
    }

    private String resolveChildName(Goods goods, String fallbackName) {
        if (goods.getName() != null && !goods.getName().trim().isEmpty()) {
            return goods.getName();
        }
        return fallbackName;
    }

    private String resolveChildImage(Goods goods, String fallbackImage) {
        if (goods.getIcon() != null && !goods.getIcon().trim().isEmpty()) {
            return goods.getIcon();
        }
        if (goods.getImgs() != null && !goods.getImgs().trim().isEmpty()) {
            return goods.getImgs().split(",")[0];
        }
        return fallbackImage;
    }
}
