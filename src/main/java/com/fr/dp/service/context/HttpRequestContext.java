package com.fr.dp.service.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequestContext implements RequestContext {
    private final HttpServletRequest request;
    private final AsyncContext ctx;
    private final Map<String, Object> attributes = new HashMap<>();
    private Map<String, List<String>> headers;
    private Map<String, List<String>> parameters;
    private Object body;

    public HttpRequestContext(AsyncContext ctx) {
        this.ctx = ctx;
        this.request = (HttpServletRequest) ctx.getRequest();
    }

    @Override
    public String getIp() {
        // 从请求头中获取IP地址，这个请求头有可能是客户端自己加的，也可能是代理服务器添加的
        String ipAddress = request.getHeader("X-Real-IP");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Forwarded-For");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddress != null && !"unknown".equalsIgnoreCase(ipAddress)) {
            String[] ipArray = ipAddress.split(",");
            // 如果经过了代理，获取到的IP地址形如：客户端IP,代理服务器1IP,代理服务器2IP...，这里只需要第一个IP地址（客户端的IP）
            // 需要配合nginx配置来防止客户端篡改IP地址，最外层nginx添加配置项：proxy_set_header X-Forwarded-For $remote_addr
            return ipArray[0];
        }

        // 真实的IP地址，不会被篡改，但是如果经过了代理，这里获得的将是代理服务器的IP
        return request.getRemoteAddr();
    }

    @Override
    public String getType() {
        return "HTTP";
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getPath() {
        return request.getRequestURI();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        if (headers == null) {
            headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                headers.put(name, Collections.list(request.getHeaders(name)));
            }
        }
        return headers;
    }

    @Override
    public Map<String, List<String>> getParameters() {
        if (parameters == null) {
            parameters = request.getParameterMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Arrays.asList(e.getValue())
                    ));
        }
        return parameters;
    }

    @Override
    public Object getBody() {
        if (body == null) {
            try {
                // 惰性加载，支持重复读取
                // 根据content-type解析
                if (request.getContentType() == null) {
                    switch (request.getContentType().toLowerCase()) {
                        case MediaType.APPLICATION_JSON_VALUE:
                            StringBuilder jsonString = new StringBuilder();
                            try (BufferedReader reader = request.getReader()) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    jsonString.append(line);
                                }
                            }
                            body = jsonString.toString();
                            break;
                        case MediaType.APPLICATION_FORM_URLENCODED_VALUE:
                            body = request.getParameterMap();
                            break;
                        case MediaType.MULTIPART_FORM_DATA_VALUE:
                            body = request.getParts();
                            break;
                        case MediaType.TEXT_PLAIN_VALUE:
                            body = request.getReader().lines().collect(Collectors.joining("\n"));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read request body", e);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }
        return body;
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void sendResponse(Map<?, ?> result) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("code", 200);
            resultMap.put("data", result);
            String json = new ObjectMapper().writeValueAsString(resultMap);
            ctx.getResponse().getOutputStream().write(json.getBytes());
            ctx.complete();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write response", e);
        }
    }

    @Override
    public void sendError(int code, String message) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("code", code);
            resultMap.put("message", message);
            String json = new ObjectMapper().writeValueAsString(resultMap);
            ctx.getResponse().getOutputStream().write(json.getBytes());
            ctx.complete();
        } catch (IOException e) {
            throw new RuntimeException("Failed to send error response", e);
        }
    }

    // HTTP特有方法（可选）
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }
}
