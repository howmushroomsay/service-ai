package com.fr.dp.service.context;

import java.util.List;
import java.util.Map;

// 通用通用上下文
public interface RequestContext {

    String getIp();
    String getType();
    String getMethod();
    String getPath();
    Map<String, List<String>> getHeaders();
    Map<String, List<String>> getParameters();
    Object getBody();
    void setAttribute(String key, Object value);
    Object getAttribute(String key);
    void sendResponse(Map<?,?> result);
    void sendError(int code, String message);
}