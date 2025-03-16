package com.fr.dp.service.context;

import com.fr.dp.service.resolver.HttpRequestResolver;
import com.fr.dp.service.resolver.ProtocolResolver;

import java.util.ArrayList;
import java.util.List;

public class RequestContextFactory {
    private static final List<ProtocolResolver> resolvers = new ArrayList<>();

    static {
        resolvers.add(new HttpRequestResolver());
//        resolvers.add(new WebSocketResolver());
        // 添加其他协议解析器
    }

    public static RequestContext createContext(Object rawRequest) {
        return resolvers.stream()
                .filter(r -> r.supports(rawRequest))
                .findFirst()
                .map(r -> r.parse(rawRequest))
                .orElseThrow(() -> new UnsupportedOperationException("Unsupported protocol"));
    }
}