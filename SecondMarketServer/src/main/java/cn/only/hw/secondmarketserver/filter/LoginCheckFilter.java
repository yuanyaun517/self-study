package cn.only.hw.secondmarketserver.filter;

import cn.only.hw.secondmarketserver.util.BaseContext;
import cn.only.hw.secondmarketserver.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 描述：登录校验过滤器
 * 类名：LoginCheckFilter
 */

//@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String requestURI = httpServletRequest.getRequestURI();
        log.info("拦截到请求：{}", requestURI);

        String[] urls = {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/manager/captcha",
                "/manager/login",
                "/user/sendMsg",
                "/user/login",
                "/user/register",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs",
        };
        // 判断当前请求是否需要放行
        boolean check = check(urls, requestURI);
        if (check) {
            // 直接放行
            log.info("本次请求不需要处理：{}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // 检查管理员是否已登录
        if (httpServletRequest.getSession().getAttribute("employee") != null) {
            // 已登录，直接放行
            log.info("员工已登录，员工 id 为：{}", httpServletRequest.getSession().getAttribute("employee"));
            int empId = (int) httpServletRequest.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            chain.doFilter(request, response);
            return;
        }

        // 检查普通用户是否已登录
        if (httpServletRequest.getSession().getAttribute("user") != null) {
            // 已登录，直接放行
            log.info("用户已登录，用户 id 为：{}", httpServletRequest.getSession().getAttribute("user"));
            int userId = (int) httpServletRequest.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            chain.doFilter(request, response);
            return;
        }

        // 未登录，通过输出流向客户端响应错误信息
        log.info("用户未登录：{}", requestURI);
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.error("NOTLOGIN")));
    }

    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
