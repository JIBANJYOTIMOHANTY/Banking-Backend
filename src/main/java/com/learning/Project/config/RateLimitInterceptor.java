package com.learning.Project.config;

import com.learning.Project.exceptions.RateLimitException;
import com.learning.Project.validation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

            if (rateLimit != null) {
                String ip = request.getRemoteAddr();
                String method = handlerMethod.getMethod().getName();
                String key = "rate_limit:" + ip + ":" + method;

                Long count = redisTemplate.opsForValue().increment(key);
                if (count == null) {
                    return true;
                }
                if (count == 1) {
                    redisTemplate.expire(key, Duration.ofSeconds(rateLimit.period()));
                }
                if (count > rateLimit.limit()) {
                    throw new RateLimitException("Too many requests. Please try again later.");
                }
            }
        }
        return true;
    }
}
