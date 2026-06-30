package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.entity.Catechild;
import cn.only.hw.secondmarketserver.dao.CatechildDao;
import cn.only.hw.secondmarketserver.service.CatechildService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * (Catechild)表服务实现类
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@Service
public class CatechildServiceImpl extends ServiceImpl<CatechildDao,Catechild> implements CatechildService {

    @Autowired
    private CatechildDao catechildDao;

    @Override
    public List<Catechild> getByCateId(Integer cateid) {
        LambdaQueryWrapper<Catechild> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Catechild::getCateid,cateid);
        return catechildDao.selectList(queryWrapper);
    }
}
