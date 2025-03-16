package com.fr.dp.service.filter;

import com.fr.dp.service.context.BuilderContext;
import com.fr.dp.service.context.RequestContext;
import com.fr.dp.service.entity.ExecuteParam;
import com.fr.dp.service.entity.RequestParam;
import com.fr.dp.service.utils.StringUtils;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpParamFilter implements ServiceFilter {
    private final List<RequestParam> params;
    private final int order;

    public HttpParamFilter(List<RequestParam> params, int order) {
        this.params = params;
        this.order = order;
    }

    @Override
    public void filter(RequestContext context, BuilderContext builderContext) throws Exception{
        // 不应该根据请求方法来解析参数，而应该根据参数位置来解析
        List<String> requiredParamNames = new ArrayList<>();
        List<String> errorParamNames = new ArrayList<>();
        for (RequestParam param : params) {
            switch (param.getPose()) {
                case "header":
                    if (!context.getHeaders().containsKey(param.getName())) {
                        if (param.isRequired()) {
                            requiredParamNames.add(param.getName());
                        } else {
                            builderContext.addParam(param.getName(), param.getDefaultValue());
                        }
                    } else {
                        String s = context.getHeaders().get(param.getName()).get(0);
                        try {
                            builderContext.addParam(param.getName(), parse(s, param.getType()));
                        } catch (Exception e) {
                            errorParamNames.add(param.getName());
                        }
                    }
                    break;
                case "query":
                    if (!context.getParameters().containsKey(param.getName())) {
                        if (param.isRequired()) {
                            requiredParamNames.add(param.getName());
                        } else {
                            builderContext.addParam(param.getName(), param.getDefaultValue());
                        }
                    } else {
                        String s = context.getParameters().get(param.getName()).get(0);
                        try {
                            builderContext.addParam(param.getName(), parse(s, param.getType()));
                        } catch (Exception e) {
                            errorParamNames.add(param.getName());
                        }
                    }
                    break;
                case "body":
                    if (StringUtils.isNotEmpty(param.getJsonPath())) {
                        // 按jsonpath读取参数
                        Object read = JsonPath.read(context.getBody(), param.getJsonPath());
                        if (read == null) {
                            if (param.isRequired()) {
                                requiredParamNames.add(param.getName());
                            } else {
                                builderContext.addParam(param.getName(), param.getDefaultValue());
                            }
                        } else {
                            builderContext.addParam(param.getName(), parse(read.toString(), param.getType()));
                        }
                    } else {
                        // 没有jsonpath，在param里面
                        if (!context.getParameters().containsKey(param.getName())) {
                            if (param.isRequired()) {
                                requiredParamNames.add(param.getName());
                            } else {
                                builderContext.addParam(param.getName(), param.getDefaultValue());
                            }
                        } else {
                            String s = context.getParameters().get(param.getName()).get(0);
                            try {
                                builderContext.addParam(param.getName(), parse(s, param.getType()));
                            } catch (Exception e) {
                                errorParamNames.add(param.getName());
                            }
                        }
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported param pose:" + param.getPose());
            }
        }
        if (!requiredParamNames.isEmpty() || !errorParamNames.isEmpty()) {
            String errorMessage = "Required param not found:" + String.join(",", requiredParamNames) +
                    "Error param:" + String.join(",", errorParamNames);
            throw new Exception(errorMessage);
        }
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

    @Override
    public int getOrder() {
        return order;
    }
}
