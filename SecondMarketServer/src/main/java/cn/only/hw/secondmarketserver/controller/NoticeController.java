package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.entity.Menu;
import cn.only.hw.secondmarketserver.entity.Notice;
import cn.only.hw.secondmarketserver.service.NoticeService;
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
 * (Notice)表控制层
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@RestController
@RequestMapping("/notice")
@Slf4j
@Api(tags = "公告通知相关操作")
public class NoticeController {
    /**
     * 服务对象
     */
    @Autowired
    private NoticeService noticeService;

    @ApiOperation("获取公告通知的方法")
    @PostMapping("/list")
    public Result<List<Notice>> login() {
        log.info("获取公告菜单:");
        List<Notice> menuList = noticeService.list();
        if (menuList.size() > 0) {
            return Result.success(menuList);
        }
        return Result.error("暂时没有公告通知数据");
    }

}

