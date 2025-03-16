package com.fr.dp.service.filter;

import com.fr.dp.service.context.BuilderContext;
import com.fr.dp.service.context.RequestContext;
import com.fr.dp.service.limiter.LocalRateLimiter;
import com.fr.dp.service.limiter.RateLimiter;

/**
 * This class created on 2023/7/7
 *
 * @author Kuifang.Liu
 */
public class RateLimitFilter implements ServiceFilter {
    private final RateLimiter ratelimiter;

    public RateLimitFilter(String key, int limit, int expire) {
        this.ratelimiter = new LocalRateLimiter(key, limit, expire);
    }

    @Override
    public void filter(RequestContext context, BuilderContext builderContext) throws Exception {
        if (!ratelimiter.tryAcquire()) {
            throw new Exception("限流了");
        }
    }


    @Override
    public int getOrder() {
        return 0;
    }


}
