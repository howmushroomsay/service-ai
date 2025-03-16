package com.fr.dp.service.other;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.lang.reflect.Method;

public class DynamicHandlerAdapter {

    private final RequestMappingHandlerMapping handlerMapping;
    private final Object targetBean;  // 动态处理器实例
    private final Method targetMethod;  // 动态方法对象
    public DynamicHandlerAdapter(
            RequestMappingHandlerMapping handlerMapping,
            Object targetBean,
            Method targetMethod
    ) {
        this.handlerMapping = handlerMapping;
        this.targetBean = targetBean;
        this.targetMethod = targetMethod;
    }

    // 注册映射
    public void registerMapping(RequestMappingInfo mappingInfo) {
        HandlerMethod handlerMethod = new HandlerMethod(targetBean, targetMethod);
        handlerMapping.registerMapping(mappingInfo, handlerMethod, targetMethod);
    }

    // 注销映射
    public void unregisterMapping(RequestMappingInfo mappingInfo) {
        handlerMapping.unregisterMapping(mappingInfo);
    }
}