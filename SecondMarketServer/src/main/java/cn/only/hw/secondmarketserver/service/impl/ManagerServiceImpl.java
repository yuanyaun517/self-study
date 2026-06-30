package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.entity.Manager;
import cn.only.hw.secondmarketserver.dao.ManagerDao;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.ManagerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * (Manager)表服务实现类
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@Service
public class ManagerServiceImpl extends ServiceImpl<ManagerDao,Manager> implements ManagerService {

    @Autowired
    private ManagerDao managerDao;

    @Override
    public Manager login(Manager manager) {
        LambdaQueryWrapper<Manager> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Manager::getAccount,manager.getAccount());
        queryWrapper.eq(Manager::getPassword,manager.getPassword());
        Manager userLogin = managerDao.selectOne(queryWrapper);
        return userLogin;
    }


}
