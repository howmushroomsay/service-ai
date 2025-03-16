package com.fr.dp.service.filter;

import com.fr.dp.service.context.BuilderContext;
import com.fr.dp.service.context.RequestContext;

public interface ServiceFilter {

    int getOrder();

    void filter(RequestContext context, BuilderContext builderContext) throws Exception;
}
