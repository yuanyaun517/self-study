package cn.only.hw.secondmarketserver;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Slf4j
@SpringBootApplication
@ServletComponentScan // 开启自动注册
public class SecondMarketServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondMarketServerApplication.class, args);
        log.info("项目启动成功....");

    }

}
