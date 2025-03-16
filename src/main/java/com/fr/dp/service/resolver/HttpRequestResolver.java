package com.fr.dp.service.resolver;

import com.fr.dp.service.context.HttpRequestContext;
import com.fr.dp.service.context.RequestContext;
import jakarta.servlet.AsyncContext;

public class HttpRequestResolver implements ProtocolResolver {
    @Override
    public boolean supports(Object rawRequest) {
        return rawRequest instanceof AsyncContext;
    }

    @Override
    public RequestContext parse(Object rawRequest) {

        return new HttpRequestContext((AsyncContext) rawRequest);
    }
}