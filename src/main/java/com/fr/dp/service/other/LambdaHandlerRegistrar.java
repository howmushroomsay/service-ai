package com.fr.dp.service.other;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;

public class LambdaHandlerRegistrar {

    private final ApplicationContext applicationContext;

    public LambdaHandlerRegistrar(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // 注册 Lambda 表达式为处理器
    public void registerLambdaHandler(
            String path,
            RequestMethod httpMethod,
            DynamicRequestHandler handler
    ) throws Exception {
        // 创建动态代理 Bean
        Object proxyBean = createProxyBean(handler);

        // 获取 Lambda 的 Method 对象
        Method lambdaMethod = extractLambdaMethod(handler);

        // 注册到 HandlerMapping
        RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        RequestMappingInfo mappingInfo = RequestMappingInfo
                .paths(path)
                .methods(httpMethod)
                .build();

        DynamicHandlerAdapter adapter = new DynamicHandlerAdapter(handlerMapping, proxyBean, lambdaMethod);
        adapter.registerMapping(mappingInfo);
    }

    // 创建动态代理 Bean（模拟 Controller）
    private Object createProxyBean(DynamicRequestHandler handler) {
        return new Object() {
            @SuppressWarnings("unused")
            public Object handle(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    @PathVariable Map<String, String> pathVariables
            ) throws Exception {
                return handler.handleRequest(request, response, pathVariables);
            }
        };
    }

    // 通过反射提取 Lambda 的 Method（需要 Lambda 可序列化）
    private Method extractLambdaMethod(DynamicRequestHandler handler) throws Exception {

        return handler.getClass().getDeclaredMethod("handleRequest", HttpServletRequest.class, HttpServletResponse.class, Map.class);
    }
}
