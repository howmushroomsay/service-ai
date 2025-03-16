package com.fr.dp.service.filter;

import com.fr.dp.dataservice.context.GatewayContext;
import com.fr.dp.dataservice.error.GatewayRequestError;
import com.fr.dp.dataservice.handler.HandlerType;
import com.fr.dp.dataservice.handler.pre.PreHandler;

/**
 * This class created on 2023/7/7
 *
 * @author Kuifang.Liu
 */
public class RateLimitHandler implements PreHandler<RateLimitConfigEntity> {
    private final RateLimitConfigEntity configEntity;
    private final HandlerType<RateLimitConfigEntity, RateLimitHandler> handlerType;

    public RateLimitHandler(RateLimitConfigEntity configEntity, HandlerType<RateLimitConfigEntity, RateLimitHandler> handlerType) {
        this.configEntity = configEntity;
        this.handlerType = handlerType;
    }

    @Override
    public void handle(GatewayContext context) {
        if (configEntity.getLimitCount() <= 0) {
            return;
        }
        if (!configEntity.getRateLimiter().tryAcquire()) {
            context.responseError(GatewayRequestError.ClientError.ApplicationError.FREQUENCY_LIMIT_EXCEEDS);
        }
    }

    @Override
    public HandlerType<RateLimitConfigEntity, RateLimitHandler> getType() {
        return this.handlerType;
    }

    @Override
    public int getSortIndex() {
        return 0;
    }
}
