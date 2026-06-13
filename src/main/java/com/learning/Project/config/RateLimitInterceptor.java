package com.learning.Project.config;

import com.learning.Project.exceptions.RateLimitException;
import com.learning.Project.validation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final Environment env;

    RateLimitInterceptor(StringRedisTemplate redisTemplate, Environment env) {
        this.redisTemplate = redisTemplate;
        this.env = env;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

            if (rateLimit != null) {
                String ip = request.getRemoteAddr();
                String method = handlerMethod.getMethod().getName();
                String key = "rate_limit:" + ip + ":" + method;

                // Dynamic configuration lookup: check method-specific key first, then global
                // default, then fallback to annotation
                String limitProp = env.getProperty("ratelimit." + method + ".limit");
                if (limitProp == null) {
                    limitProp = env.getProperty("ratelimit.default.limit");
                }
                int limit = (limitProp != null) ? Integer.parseInt(limitProp) : rateLimit.limit();

                String periodProp = env.getProperty("ratelimit." + method + ".period");
                if (periodProp == null) {
                    periodProp = env.getProperty("ratelimit.default.period");
                }
                int period = (periodProp != null) ? Integer.parseInt(periodProp) : rateLimit.period();

                Long count = redisTemplate.opsForValue().increment(key);
                if (count == null) {
                    return true;
                }
                if (count == 1) {
                    redisTemplate.expire(key, Duration.ofSeconds(period));
                }
                if (count > limit) {
                    Long ttl = redisTemplate.getExpire(key);
                    String waitTime = (ttl != null && ttl > 0) ? ttl + " seconds" : "some time";
                    throw new RateLimitException("Too many requests. Please try again after " + waitTime + ".");
                }
            }
        }
        return true;
    }
}
