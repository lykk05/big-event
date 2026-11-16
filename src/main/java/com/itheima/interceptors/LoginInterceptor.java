package com.itheima.interceptors;

import com.itheima.anno.PublicApi;
import com.itheima.utils.JwtUtil;
import com.itheima.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

// 登录拦截器 --> 不用在每个Controller方法中写相同的拦截业务代码
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非 Controller 方法（如静态资源、错误页）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 注解声明的开放接口直接放行
        if (handlerMethod.hasMethodAnnotation(PublicApi.class)
                || handlerMethod.getBeanType().isAnnotationPresent(PublicApi.class)) {
            return true;
        }

        // 令牌验证
        String token = request.getHeader("Authorization");
        try {
            // 从Redis中查询token（白名单）
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            String redisToken = operations.get(token);
            if (redisToken == null) {
                // 令牌不存在
                throw new RuntimeException("令牌不存在");
            }
            // 验证token
            Map<String, Object> claims = jwtUtil.parseToken(token);
            // 把业务数据存储到ThreadLocal中
            ThreadLocalUtil.set(claims);
            // 放行
            return true;
        } catch (Exception e) {
            // 响应状态码401
            response.setStatus(401);
            // 拦截
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除ThreadLocal中的数据 --> 防止内存泄露
        ThreadLocalUtil.remove();
    }
}
