package com.qfnu.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.qfnu.reggie.common.BaseContext;
import com.qfnu.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录的过滤器
 */

@Slf4j
@WebFilter(filterName = "loginCheckfilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    // 路径匹配器，支持通配符*匹配
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //强转
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1.获取本次请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求：{}", requestURI);

        // 定义不需要处理的请求路径
        String[] urls = new String[] {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",        //移动端发短信
                "/user/login"           //移动端登录
        };

        // 2.判断本次请求是否需要处理，true说明在里面不用处理直接放行
        boolean check = check(urls, requestURI);

        // 3.如果不需要处理，直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);

            filterChain.doFilter(request, response);
            return;
        }

        // 4.1 判断后端登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，id为{}", request.getSession().getAttribute("employee"));

            // 把当前用户的id放入到当前线程中，以便在同线程的其他方法中调用该值
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        // 4.2 判断移动登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，id为{}", request.getSession().getAttribute("user"));

            // 把当前用户的id放入到当前线程中，以便在同线程的其他方法中调用该值
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        // 5.如果未登录，则返回未登录结果，通过输出流的方式向客户端响应数据
        /*
           这里不直接跳转页面是因为前端有个响应拦截器（backend/js/request.js），
           这个地方做了登录判断，当code=0 msg=NOTLOGIN时会跳转到登录页面
           所以后端这里只需要设置code和msg的值即可
         */
        log.info("用户未登录");

        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }


    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        //判断requestURI是否在urls中
        for (String url : urls) {
            boolean flag = PATH_MATCHER.match(url, requestURI);  // 遍历数组，逐个匹配
            if (flag) {
                return true;
            }
        }
        return false;
    }
}
