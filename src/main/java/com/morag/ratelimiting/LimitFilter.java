package com.morag.ratelimiting;

import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@WebFilter("/*")
public class LimitFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(LimitFilter.class);

    RRateLimiter limiter;

    public LimitFilter() {

        Config config = new Config();
        config.useSingleServer().setAddress("redis://172.17.0.2:6379");
        RedissonClient redisson = Redisson.create(config);
        limiter = redisson.getRateLimiter("myLimiter");
        limiter.trySetRate(RateType.OVERALL, 100, 1, RateIntervalUnit.HOURS);
        
    }


    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {


        if (limiter.tryAcquire(1, 0, TimeUnit.SECONDS)) {
            // let the request through and process as usual
            chain.doFilter(request, response);
            logger.info("request path " + ((HttpServletRequest) request).getRequestURI() + " accepted");
        } else {
            // handle limit case
            ((HttpServletResponse) response).sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Rate limit exceeded. Try again in 36 seconds\n");
            logger.info("request path " + ((HttpServletRequest) request).getRequestURI() + " rejected, too many requests");
        }

    }


}
