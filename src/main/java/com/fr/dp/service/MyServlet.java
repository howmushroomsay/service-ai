package com.fr.dp.service;


import com.fr.dp.service.context.RequestContext;
import com.fr.dp.service.context.RequestContextFactory;
import com.fr.dp.service.dto.ApiInfoDTO;
import com.fr.dp.service.entity.ExecuteParam;
import com.fr.dp.service.entity.RequestParam;
import com.fr.dp.service.utils.ApplicationContextUtils;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@WebServlet(name = "AsyncServlet1",
    urlPatterns = "/hellowork/*",
    asyncSupported = true
)
@Slf4j
public class MyServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doExecute(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doExecute(req, resp);
    }

    private void doExecute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Type", "text/json;charset=utf-8");
        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(40000);
        // 解析request
        RequestContext context = RequestContextFactory.createContext(asyncContext);
        log.info("doExecute:");
        MyExecutePool.getInstance().execute(() -> {
            ApiInfoDTO apiInfoDTO = ApplicationContextUtils.getBean(ApiRegistry.class).get(context.getPath());

            // 判断是否注册
            if (apiInfoDTO == null) {
                context.sendError(404, "未注册");
                return;
            }
            // 鉴定请求方法
            if (!apiInfoDTO.getMethod().equals(context.getMethod())) {
                context.sendError(405, "请求方法不匹配");
                return;
            }

            // post请求判断请求体的样式是否正确
            if ("POST".equals(apiInfoDTO.getMethod())) {
                String contentType = apiInfoDTO.getContentType();
                if (!context.getHeaders().get("content-type").get(0).equals(contentType)) {
                    context.sendError(400, String.format("请求体类型不匹配：%s", contentType));
                    return;
                }
            }
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // 鉴定参数是否都有
            Map<String, ExecuteParam> params = new HashMap<>();
            try {
                for (RequestParam requestParam : apiInfoDTO.getParams()) {
                    switch (requestParam.getPose()) {
                        case "header":
                            if (!context.getHeaders().containsKey(requestParam.getName())) {
                                context.sendError(400, String.format("缺少请求头参数：%s", requestParam.getName()));
                                return;
                            } else {
                                String s = context.getHeaders().get(requestParam.getName()).get(0);
                                params.put(requestParam.getName(), new ExecuteParam(requestParam.getName(), requestParam.getType(), parse(s, requestParam.getType())));
                            }
                            break;
                        case "query":
                            if (!context.getParameters().containsKey(requestParam.getName())) {
                                context.sendError(400, String.format("缺少请求参数：%s", requestParam.getName()));
                                return;
                            } else {
                                params.put(requestParam.getName(), new ExecuteParam(requestParam.getName(), requestParam.getType(), parse(context.getParameters().get(requestParam.getName()).get(0), requestParam.getType())));
                            }
                            break;
                        case "body":
                            if (!((Map<String, Object>) context.getBody()).containsKey(requestParam.getName())) {
                                context.sendError(400, String.format("缺少请求体参数：%s", requestParam.getName()));
                                return;
                            } else {
                                params.put(requestParam.getName(),
                                        new ExecuteParam(requestParam.getName(),
                                                requestParam.getType(), parse(((Map<String, Object>) context.getBody()).get(requestParam.getName()).toString(), requestParam.getType())
                                        )
                                );
                            }
                    }

                }
                // 通过params构建返回
                context.sendResponse(params);

            } catch (Exception e) {
                context.sendError(400, e.getMessage());
            }
        });
        log.info("finish execute");
    }

    private Object parse(String value, String type) {
        return switch (type) {
            case "bool" -> Boolean.parseBoolean(value) || "1".equals(value);
            case "number" -> {
                if (value.contains(".")) {
                    yield Double.parseDouble(value);
                }
                yield Integer.parseInt(value);
            }
            case "string" -> value;
            default -> null;
        };
    }
}
