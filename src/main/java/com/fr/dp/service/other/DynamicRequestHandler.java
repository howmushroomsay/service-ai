package com.fr.dp.service.other;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.Serializable;
import java.util.Map;

@FunctionalInterface
public interface DynamicRequestHandler extends Serializable {
    Object handleRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            Map<String, String> pathVariables
    ) throws Exception;
}