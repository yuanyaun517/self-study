package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.entity.Forum;
import cn.only.hw.secondmarketserver.dao.ForumDao;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.service.ForumService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * (Forum)表服务实现类
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@Service
public class ForumServiceImpl extends ServiceImpl<ForumDao,Forum> implements ForumService {

    @Autowired
    private ForumDao forumDao;

    @Override
    public List<Forum> getByType(String type) {
        LambdaQueryWrapper<Forum> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Forum::getType,type);
        queryWrapper.eq(Forum::getManage, "1");
        List<Forum> list = forumDao.selectList(queryWrapper);
        return list;
    }
}
