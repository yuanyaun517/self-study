package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.entity.Catechild;
import cn.only.hw.secondmarketserver.service.CatechildService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;


/**
 * (Catechild)表控制层
 *
 */
@RestController
@RequestMapping("/catechild")
@Slf4j
@Api(tags = "Catechild")
public class CatechildController {
    /**
     * 服务对象
     */
    @Autowired
    private CatechildService catechildService;

}

