package cn.only.hw.secondmarketserver.service;

import cn.only.hw.secondmarketserver.entity.Forum;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * (Forum)表服务接口
 *
 * @author 李淑娟
 * @since 2026/1/20
 */
public interface ForumService extends IService<Forum>{

    List<Forum> getByType(String type);
}
