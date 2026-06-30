package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.entity.Banner;
import cn.only.hw.secondmarketserver.entity.Menu;
import cn.only.hw.secondmarketserver.service.MenuService;
import cn.only.hw.secondmarketserver.util.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;

import java.util.List;


/**
 * (Menu)表控制层
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@RestController
@RequestMapping("/menu")
@Slf4j
@Api(tags = "菜单相关操作")
public class MenuController {
    /**
     * 服务对象
     */
    @Autowired
    private MenuService menuService;


    @ApiOperation("获取菜单的方法")
    @PostMapping("/list")
    public Result<List<Menu>> login() {
        log.info("获取菜单:");
        List<Menu> menuList = menuService.list();
        if (menuList.size() > 0) {
            return Result.success(menuList);
        }
        return Result.error("暂时没有菜单数据");
    }


}

