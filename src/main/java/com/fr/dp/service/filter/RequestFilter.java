package com.fr.dp.service.filter;

import com.fr.dp.service.context.BuilderContext;
import com.fr.dp.service.context.RequestContext;
import com.fr.dp.service.utils.StringUtils;

/**
 * 检查请求的方法和contentType是否合法
 * This class created on 2023/8/1
 *
 * @author Kuifang.Liu
 */
public class RequestFilter implements ServiceFilter {

    private final String requestMethod;
    private final String requestContentType;
    private final int order;

    public RequestFilter(String requestMethod, String requestContentType, int order) {
        this.requestMethod = requestMethod;
        this.requestContentType = requestContentType;
        this.order = order;
    }

    @Override
    public void filter(RequestContext context, BuilderContext builderContext) throws Exception{
        String method = context.getMethod();
        if (!StringUtils.equalsIgnoreCase(method, requestMethod)) {
            throw new Exception("请求方法错误");
        }
        String contentType = context.getHeaders().get("Content-Type").get(0);
        if (StringUtils.equalsIgnoreCase("POST", method) &&
                !StringUtils.equalsIgnoreCase(requestContentType, contentType)) {
            throw new Exception("请求体类型不匹配");
        }
    }



    @Override
    public int getOrder() {
        return order;
    }
}
