package cn.only.hw.secondmarketserver.service;

import cn.only.hw.secondmarketserver.entity.Manager;
import cn.only.hw.secondmarketserver.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * (Manager)表服务接口
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
public interface ManagerService extends IService<Manager>{

    Manager login(Manager manager);
}
