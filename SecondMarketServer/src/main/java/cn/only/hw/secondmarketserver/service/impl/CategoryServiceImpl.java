package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.entity.Category;
import cn.only.hw.secondmarketserver.dao.CategoryDao;
import cn.only.hw.secondmarketserver.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


/**
 * (Category)表服务实现类
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDao,Category> implements CategoryService {
 
}
