package com.fr.dp.service.filter;


import com.fr.dp.service.context.BuilderContext;
import com.fr.dp.service.context.RequestContext;

/**
 * This class created on 2023/7/7
 *
 * @author Kuifang.Liu
 */
public class ExpireFilter implements ServiceFilter {
    private final long expireTime;
    private final int order;

    public ExpireFilter(long expireTime, int order) {
        this.expireTime = expireTime;
        this.order = order;
    }

    @Override
    public void filter(RequestContext context, BuilderContext builderContext) throws Exception {
        if (expireTime > 0 && System.currentTimeMillis() > expireTime) {
            throw new Exception("接口过期了");
        }
    }


    @Override
    public int getOrder() {
        return order;
    }
}
