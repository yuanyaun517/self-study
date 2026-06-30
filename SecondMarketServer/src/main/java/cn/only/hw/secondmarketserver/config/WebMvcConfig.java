package cn.only.hw.secondmarketserver.config;

import cn.only.hw.secondmarketserver.util.JacksonObjectMapper;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.List;

/**
 * Web MVC配置类
 * 配置Spring MVC的相关功能，包括：
 * - 静态资源映射（Swagger文档、前端页面等）
 * - 跨域请求配置
 * - 消息转换器扩展
 * - Swagger/Knife4j API文档配置
 *
 * @author 李淑娟
 */

@Slf4j
@Configuration
@EnableSwagger2WebMvc
@EnableKnife4j
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 配置静态资源处理器
     * 用于映射静态资源的访问路径和实际存储位置
     * 
     * @param registry 资源处理器注册表
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("静态资源放行初始化...");
        
        // Knife4j/Swagger 文档资源
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        // Swagger UI 资源（必须添加）
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/swagger-resources/**").addResourceLocations("classpath:/META-INF/resources/");

        // 其他静态资源
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }


    /**
     * 配置跨域资源共享（CORS）
     * 允许前端应用跨域访问后端接口
     * 
     * @param registry 跨域配置注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                    // 允许所有路径
                .allowedOriginPatterns("*")            // 允许所有来源
                .allowCredentials(true)                 // 允许携带认证信息（Cookie等）
                .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH")  // 允许的HTTP方法
                .maxAge(3600);                          // 预检请求缓存时间（秒）
    }


    /**
     * 扩展消息转换器
     * 自定义JSON序列化配置，使用JacksonObjectMapper处理日期等特殊格式
     * 
     * @param converters 消息转换器列表
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器启动.");
                
        // 创建消息转换器对象
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
                
        // 设置对象转换器，底层使用Jackson将Java对象转换为JSON对象
        mappingJackson2HttpMessageConverter.setObjectMapper(new JacksonObjectMapper());
                
        // 将自定义的消息转换器添加到MVC框架的转换器集合中（索引1，优先级较高）
        converters.add(1, mappingJackson2HttpMessageConverter);
    }



    /**
     * 创建Swagger API文档配置
     * 配置API文档的基本信息和扫描规则
     * 
     * @return Docket对象，用于构建API文档
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)  // 指定Swagger版本为2.0
                .apiInfo(apiInfo())                      // 设置API文档基本信息
                .select()
                .apis(RequestHandlerSelectors.basePackage("cn.only.hw.secondmarketserver.controller"))  // 扫描controller包
                .paths(PathSelectors.any())              // 匹配所有路径
                .build();
    }
    
    /**
     * 构建API文档的基本信息
     * 包括标题、作者、版本、描述等
     * 
     * @return ApiInfo对象，包含API文档的元数据
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("校园二手交易平台")                                    // 文档标题
                .contact(new Contact("李淑娟", "https://j.tewx.cn", "2720755460@qq.com"))  // 联系人信息
                .version("1.0")                                              // 版本号
                .description("校园二手交易平台接口文档")                       // 文档描述
                .build();
    }


}
