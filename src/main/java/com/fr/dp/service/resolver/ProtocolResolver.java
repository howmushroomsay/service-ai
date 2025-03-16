package com.fr.dp.service.resolver;

import com.fr.dp.service.context.RequestContext;

public interface ProtocolResolver {
    boolean supports(Object rawRequest);
    RequestContext parse(Object rawRequest);
}