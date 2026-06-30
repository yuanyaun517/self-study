package cn.only.hw.secondmarketserver.service;

import cn.only.hw.secondmarketserver.entity.Catechild;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * (Catechild)表服务接口
 *

 */
public interface CatechildService extends IService<Catechild>{

    List<Catechild> getByCateId(Integer cateid);
}
