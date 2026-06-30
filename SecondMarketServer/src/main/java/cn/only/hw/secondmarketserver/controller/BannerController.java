package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.entity.Banner;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.BannerService;
import cn.only.hw.secondmarketserver.util.Result;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;

import javax.servlet.http.HttpSession;
import java.util.List;


/**
 * (Banner)表控制层
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@RestController
@RequestMapping("/banner")
@Slf4j
@Api(tags = "轮播图相关")
public class BannerController {
    /**
     * 服务对象
     */
    @Autowired
    private BannerService bannerService;

    @ApiOperation("获取轮播图的方法")
    @PostMapping("/list")
    public Result<List<Banner>> login() {
        log.info("获取轮播图:");
        List<Banner> bannerList = bannerService.list();
        if (bannerList.size() > 0) {
            return Result.success(bannerList);
        }
        return Result.error("暂时没有轮播图数据");
    }



}

