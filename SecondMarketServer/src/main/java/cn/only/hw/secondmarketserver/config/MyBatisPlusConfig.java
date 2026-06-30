package cn.only.hw.secondmarketserver.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus配置类
 * 配置MyBatis Plus的分页插件，支持分页查询功能
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置MyBatis Plus拦截器
     * 添加分页插件以支持分页查询
     *
     * @return MybatisPlusInterceptor 拦截器对象
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }
}
