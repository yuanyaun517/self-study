package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.entity.Address;
import cn.only.hw.secondmarketserver.service.AddressService;
import cn.only.hw.secondmarketserver.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;

import java.util.List;


/**
 * (Address)表控制层
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@RestController
@RequestMapping("/address")
@Slf4j
@Api(tags = "Address")
public class AddressController {
    /**
     * 服务对象
     */
    @Autowired
    private AddressService addressService;

    @ApiOperation("获取所有地址的方法")
    @PostMapping("/list")
    public Result<List<Address>> login() {
        log.info("获取所有地址:");
        List<Address> list = addressService.list();
        if (list.size() > 0) {
            return Result.success(list);
        }
        return Result.error("暂时没有数据");
    }


    @ApiOperation("通过id获取地址的方法")
    @PostMapping("/getById")
    public Result<Address> getById(Integer id) {
        log.info("获取所有地址:");
        Address address = addressService.getById(id);
        if (address != null) {
            return Result.success(address);
        }
        return Result.error("暂时没有数据");
    }



    @ApiOperation("按用户id获取地址的方法")
    @PostMapping("/getByUserId")
    public Result<List<Address>> getByType(String userid) {
        log.info("按用户id获取地址:{}",userid);
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserid,userid);
        List<Address> list = addressService.list(queryWrapper);
        list.sort((left, right) -> {
            int leftDefault = "1".equals(left.getIsdefault()) ? 1 : 0;
            int rightDefault = "1".equals(right.getIsdefault()) ? 1 : 0;
            if (leftDefault != rightDefault) {
                return Integer.compare(rightDefault, leftDefault);
            }
            Integer leftId = left.getId() == null ? 0 : left.getId();
            Integer rightId = right.getId() == null ? 0 : right.getId();
            return Integer.compare(rightId, leftId);
        });
        if (list.size() > 0) {
            return Result.success(list);
        }
        return Result.error("暂时没有数据");
    }

    @ApiOperation("保存地址的方法")
    @PostMapping("/save")
    public Result<String> save(@RequestBody Address address) {
        log.info("保存地址:{}",address.toString());
        if (address.getIsdefault() == null) {
            address.setIsdefault("0");
        }
        if (address.getUserid() != null) {
            LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Address::getUserid, address.getUserid());
            long count = addressService.count(queryWrapper);
            if (count == 0) {
                address.setIsdefault("1");
            }
        }
        boolean isSave = addressService.save(address);
        if (isSave) {
            return Result.success("保存成功");
        }
        return Result.error("保存失败");
    }

    @ApiOperation("更新地址的方法")
    @PostMapping("/update")
    public Result<String> update(@RequestBody Address address) {
        log.info("更新地址:{}", address);
        if (address.getId() == null) {
            return Result.error("地址id不能为空");
        }
        boolean isUpdate = addressService.updateById(address);
        if (isUpdate) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @ApiOperation("设置默认地址的方法")
    @PostMapping("/setDefault")
    public Result<String> setDefault(Integer id, Integer userid) {
        log.info("设置默认地址:{},{}", id, userid);
        if (id == null || userid == null) {
            return Result.error("参数不能为空");
        }
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserid, userid);
        List<Address> list = addressService.list(queryWrapper);
        for (Address item : list) {
            if (!"0".equals(item.getIsdefault())) {
                item.setIsdefault("0");
                addressService.updateById(item);
            }
        }
        Address address = new Address();
        address.setId(id);
        address.setIsdefault("1");
        boolean isUpdate = addressService.updateById(address);
        if (isUpdate) {
            return Result.success("设置成功");
        }
        return Result.error("设置失败");
    }


    @ApiOperation("删除地址的方法")
    @PostMapping("/delByid")
    public Result<String> delByid(Integer id) {
        log.info("删除地址:{}",id);
        boolean isSave = addressService.removeById(id);
        if (isSave) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }


}

