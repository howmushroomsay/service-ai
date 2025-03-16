package com.fr.dp.service.filter;

import com.fr.dp.service.context.BuilderContext;
import com.fr.dp.service.context.RequestContext;
import com.fr.dp.service.entity.ExecuteParam;
import com.fr.dp.service.entity.RequestParam;
import com.fr.dp.service.utils.StringUtils;

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
                            builderContext.addParam(param.getName(),parse(s, param.getType()));
                        } catch (Exception e) {
                            errorParamNames.add(param.getName());
                        }
                    }
                    context.addConfig(param.getName(), context.getHeaders().get(param.getName()).get(0));
                    break;
                case "query":
                    context.addConfig(param.getName(), context.getParameters().get(param.getName()).get(0));
                    break;
                case "body":
                    if (StringUtils.isNotEmpty(param.getJsonPath())) {
                        builderContext.addParam(param.getName(), parse(((Map<String, String[]>)context.getBody()).get(param.getName())[0], param.getType()));
                    }
                    Configuration body = Configuration.from(context.getRequestBody());
                    Configuration paramConf = body.getConfiguration(param.getName());
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported param pose:" + param.getPose());
            }
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

    /**
     * 处理在body里面的参数
     */
    private void handleBodyParam(RequestContext context) throws IOException {
        String requestBody = context.getRequestBody();
        Configuration body = StringUtils.isEmpty(requestBody) ? Configuration.newDefault() : Configuration.from(requestBody);
        List<GatewayParam> params = getParamInPost(body);
        Map<String, Long> paging = new HashMap<>();
        try {
            // 检查分页参数是否合法
            Configuration pagingConf = body.getConfiguration("paging");
            if (pagingConf == null) {
                paging.put(DPConstants.PagingQuery.pageSize, configEntity.getDefaultPageSize());
                paging.put(DPConstants.PagingQuery.pageNum, 1L);
            } else {
                ParamCheckUtil.checkPaging(pagingConf, configEntity.getReturnDataMaxCount());
                paging.put(DPConstants.PagingQuery.pageNum, pagingConf.getLong(DPConstants.PagingQuery.pageNum));
                paging.put(DPConstants.PagingQuery.pageSize, pagingConf.getLong(DPConstants.PagingQuery.pageSize));
            }
            // 检查参数是否有效
            ParamCheckUtil.fillParamsWithDefaultValue(configEntity.getApiParams(), params);
            ParamCheckUtil.checkRequestParams(configEntity.getApiParams(), params);
            context.addConfig("params", params);
        } catch (FineDPException ex) {
            handleCheckParamError(context.getResponse(), params, configEntity.getApiParams(), ex);
            return;
        }
        context.addConfig("paging", paging);
    }

    /**
     * 处理在Parameter里面的参数
     */
    private void handleParameterMapParam(RequestContext context) {
        Map<String, String[]> parameterMap = context.getRequest().getParameterMap();
        // paging
        Map<String, Object> paging = new HashMap<>();
        paging.put(DPConstants.PagingQuery.pageNum, parameterMap.containsKey("pageNum") ? Long.parseLong(parameterMap.get("pageNum")[0]) : 1L);
        paging.put(DPConstants.PagingQuery.pageSize, parameterMap.containsKey("pageSize") ? Long.parseLong(parameterMap.get("pageSize")[0]) : configEntity.getDefaultPageSize());
        // params
        List<GatewayParam> params = new ArrayList<>();
        try {
            ParamCheckUtil.checkPaging(Configuration.from(paging), configEntity.getReturnDataMaxCount());
            context.addConfig("paging", paging);
            for (Map.Entry<String, String[]> e : parameterMap.entrySet()) {
                if (!e.getKey().equalsIgnoreCase("pageSize") && !e.getKey().equalsIgnoreCase("pageNum")) {
                    String valueString = e.getValue()[0];
                    params.add(GatewayParam.of(e.getKey(), ParamCheckUtil.convertParamValueToType(configEntity.getApiParams(), e.getKey(), valueString)));
                }
            }
            ParamCheckUtil.fillParamsWithDefaultValue(configEntity.getApiParams(), params);
            ParamCheckUtil.checkRequestParams(configEntity.getApiParams(), params);
        } catch (FineDPException ex) {
            handleCheckParamError(context.getResponse(), params, configEntity.getApiParams(), ex);
            return;
        }
        context.addConfig("params", params);
    }

    public static void handleCheckParamError(GatewayResponse response, List<GatewayParam> params, List<DataServiceParam> apiParams, FineDPException ex) {
        if (GatewayErrorCode.contains(ex.getErrorCode())) {
            response.failed();
            if (ex.getErrorCode() == GatewayErrorCode.REQUEST_MISSING_PARAM_ERROR ||
                    ex.getErrorCode() == GatewayErrorCode.REQUEST_INVALID_PARAM_ERROR) {
                // 缺失参数或者参数格式不正确
                String expectParam = apiParams.stream().map(p -> p.getName() + ":" + p.getValueType().name()).collect(Collectors.joining(","));
                String actualParam = params.stream().map(p -> p.getName() + ":" + p.getValueType().name()).collect(Collectors.joining(","));
                response.requestError(ex.getErrorCode() == GatewayErrorCode.REQUEST_MISSING_PARAM_ERROR ?
                                GatewayRequestError.ClientError.RequestParamError.MISSING_PARAMS : GatewayRequestError.ClientError.RequestParamError.INVALID_PARAMS,
                        expectParam, actualParam);
            } else if (ex.getErrorCode() == GatewayErrorCode.REQUEST_INVALID_PAGINATION_ERROR) {
                // 分页参数错误
                response.requestError(GatewayRequestError.ClientError.RequestParamError.INVALID_PAGINATION_PARAMS);
            } else if (ex.getErrorCode() == GatewayErrorCode.REQUEST_INVALID_PARAM_VALUE_ERROR) {
                String value = null;
                for (GatewayParam param : params) {
                    try {
                        ParamCheckUtil.validateParamValue(param);
                    } catch (FineDPException e) {
                        // 找到具体是哪个参数值有问题
                        value = param.valueToString();
                        break;
                    }
                }
                // 参数值存在非法字符
                response.requestError(GatewayRequestError.ClientError.RequestParamError.INVALID_PARAM_VALUE, value);
            } else {
                throw ex;
            }
            DPLogger.getLogger().error(ex.getMessage(), ex);
            return;
        }
        throw ex;
    }

    private List<GatewayParam> getParamInPost(Configuration requestBody) {
        List<Configuration> originParams = requestBody.getListConfiguration("params");
        return originParams == null ? new ArrayList<>() :
                originParams.stream().map(p -> GatewayParam.of((String) p.get("name"), p.get("value"))).collect(Collectors.toList());
    }

    @Override
    public int getOrder() {
        return order; // 参数最后判断
    }
}
