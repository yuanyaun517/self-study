package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.dto.CollectGoodsDto;
import cn.only.hw.secondmarketserver.entity.Collect;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.service.CollectService;
import cn.only.hw.secondmarketserver.service.GoodsService;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏控制器
 * 处理用户收藏相关的HTTP请求，包括添加收藏、查看收藏列表等功能
 */
@RestController
@RequestMapping("/collect")
@Slf4j
@Api(tags = "Collect")
public class CollectController {

    /**
     * 收藏服务
     */
    @Autowired
    private CollectService collectService;

    /**
     * 商品服务
     */
    @Autowired
    private GoodsService goodsService;

    @ApiOperation("获取全部收藏")
    @PostMapping("/list")
    public Result<List<Collect>> list() {
        log.info("获取全部收藏");
        List<Collect> list = collectService.list();
        if (list.isEmpty()) {
            return Result.error("暂无数据");
        }
        return Result.success(list);
    }

    @ApiOperation("通过id获取收藏")
    @PostMapping("/getById")
    public Result<Collect> getById(Integer id) {
        log.info("通过id获取收藏:{}", id);
        Collect collect = collectService.getById(id);
        if (collect == null) {
            return Result.error("暂无数据");
        }
        return Result.success(collect);
    }

    @ApiOperation("检查是否已收藏")
    @PostMapping("/check")
    public Result<List<Collect>> check(String type, Integer sid, Integer userid) {
        log.info("检查是否收藏:{},{},{}", type, sid, userid);
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getType, type);
        queryWrapper.eq(Collect::getSid, sid);
        if (userid != null) {
            queryWrapper.eq(Collect::getUserid, userid);
        }

        List<Collect> list = collectService.list(queryWrapper);
        if (list.isEmpty()) {
            return Result.error("暂无数据");
        }
        return Result.success(list);
    }

    @ApiOperation("通过用户id获取收藏")
    @PostMapping("/getByUserId")
    public Result<List<CollectGoodsDto>> getByUserId(Integer userid) {
        log.info("获取收藏:{}", userid);
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getUserid, userid);
        List<Collect> collectList = collectService.list(queryWrapper);
        List<CollectGoodsDto> list = collectList.stream().map(item -> {
            Goods goods = goodsService.getById(item.getSid());
            CollectGoodsDto collectGoodsDto = new CollectGoodsDto();
            collectGoodsDto.setGoods(goods);
            BeanUtils.copyProperties(item, collectGoodsDto);
            return collectGoodsDto;
        }).collect(Collectors.toList());

        if (list.isEmpty()) {
            return Result.error("暂无数据");
        }
        return Result.success(list);
    }

    @ApiOperation("删除收藏")
    @PostMapping("/del")
    public Result<String> del(String id) {
        log.info("删除收藏:{}", id);
        boolean removed = collectService.removeById(id);
        if (removed) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @ApiOperation("新增收藏")
    @PostMapping("/save")
    public Result<String> save(@RequestBody Collect collect) {
        log.info("新增收藏:{}", collect);
        boolean saved = collectService.save(collect);
        if (saved) {
            return Result.success("收藏成功");
        }
        return Result.error("收藏失败");
    }
}
