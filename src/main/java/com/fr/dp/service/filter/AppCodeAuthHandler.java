package com.fr.dp.service.filter;

import com.fr.dp.dataservice.context.GatewayContext;
import com.fr.dp.dataservice.error.GatewayRequestError;
import com.fr.dp.dataservice.handler.HandlerType;
import com.fr.dp.dataservice.handler.pre.auth.AuthHandler;
import com.fr.dp.util.StringUtils;

/**
 * This class created on 2023/7/7
 *
 * @author Kuifang.Liu
 */
public class AppCodeAuthHandler extends AuthHandler<AppCodeAuthConfigEntity> {

    private final HandlerType<AppCodeAuthConfigEntity, AppCodeAuthHandler> handlerType;
    private final AppCodeAuthConfigEntity configEntity;

    public AppCodeAuthHandler(AppCodeAuthConfigEntity configEntity, HandlerType<AppCodeAuthConfigEntity, AppCodeAuthHandler> handlerType) {
        this.configEntity = configEntity;
        this.handlerType = handlerType;
    }

    @Override
    public void handle(GatewayContext context) {
        String authorization = context.getRequest().getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)) {
            context.responseError(GatewayRequestError.ClientError.AuthenticationError.MISSING_AUTHENTICATION);
        } else if (!StringUtils.equalsIgnoreCase(authorization, configEntity.getCode())) {
            context.responseError(GatewayRequestError.ClientError.AuthenticationError.INVALID_APPCODE);
        }
    }

    @Override
    public HandlerType<AppCodeAuthConfigEntity, AppCodeAuthHandler> getType() {
        return this.handlerType;
    }

    @Override
    public int getSortIndex() {
        return 0;
    }
}
