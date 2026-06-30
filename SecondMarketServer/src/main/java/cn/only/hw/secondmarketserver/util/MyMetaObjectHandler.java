package cn.only.hw.secondmarketserver.util;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Log4j2
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());

        Date now = new Date();
        if (metaObject.hasSetter("createTime")) {
            metaObject.setValue("createTime", now);
        }
        if (metaObject.hasSetter("sendTime")) {
            metaObject.setValue("sendTime", now);
        }
        if (metaObject.hasSetter("updateTime")) {
            metaObject.setValue("updateTime", now);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());

        if (metaObject.hasSetter("updateTime")) {
            metaObject.setValue("updateTime", new Date());
        }
    }
}
