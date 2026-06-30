package cn.only.hw.secondmarketserver.service;

import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.util.Result;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * (User)表服务接口
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
public interface UserService extends IService<User>{

    User login(User user);

    Result<String> register(User user);

    Double rechargeBalance(Integer userId, Double amount);

    Double withdrawBalance(Integer userId, Double amount);
}
