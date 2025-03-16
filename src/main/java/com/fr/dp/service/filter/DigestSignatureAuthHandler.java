//package com.fr.dp.service.filter;
//
//import com.fr.dp.dataservice.context.GatewayContext;
//import com.fr.dp.dataservice.error.GatewayRequestError;
//import com.fr.dp.dataservice.handler.HandlerType;
//import com.fr.dp.dataservice.handler.pre.auth.AuthHandler;
//import com.fr.dp.dataservice.util.GatewayUtil;
//import com.fr.dp.log.DPLogger;
//import com.fr.dp.util.StringUtils;
//import com.fr.third.guava.cache.Cache;
//import com.fr.third.guava.cache.CacheBuilder;
//import com.fr.third.org.apache.http.entity.ContentType;
//
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
///**
// * This class created on 2023/8/7
// *
// * @author Kuifang.Liu
// */
//public class DigestSignatureAuthHandler extends AuthHandler<DigestSignatureAuthConfigEntity> {
//    private static final long REQUEST_EXPIRE_TIME = 5 * 60 * 1000; // 请求过期时间为5分钟
//
//    //Nonce 5分钟过期
//    private static final Cache<String, String> nonceCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
//    private final DigestSignatureAuthConfigEntity configEntity;
//    private final HandlerType<DigestSignatureAuthConfigEntity, DigestSignatureAuthHandler> handlerType;
//
//    public DigestSignatureAuthHandler(DigestSignatureAuthConfigEntity configEntity, HandlerType<DigestSignatureAuthConfigEntity, DigestSignatureAuthHandler> handlerType) {
//        this.configEntity = configEntity;
//        this.handlerType = handlerType;
//    }
//
//    /**
//     * <p>
//     * 从请求的header里获取Authorization值，比较签名摘要是否一致
//     * <p/>
//     * 待签名字符串的结构：
//     * 「HTTPMethod」+ "\n"
//     * +「Nonce」+ "\n"
//     * +「Timestamp」+ "\n"
//     * +「PathAndParameters」+ "\n"
//     * +「Content-Type」+ "\n"
//     * +「Content-MD5」
//     * <p>
//     * Authorization值的结构：算法名称 Signature=Signature值, Nonce=Nonce随机数, Timestamp=Timestamp时间戳
//     * <p/>
//     */
//    @Override
//    public void handle(GatewayContext context) {
//        try {
//            HttpServletRequest request = context.getRequest();
//            String method = request.getMethod();
//            String contentType = StringUtils.isEmpty(request.getHeader("Content-Type")) ? StringUtils.EMPTY : request.getHeader("Content-Type");
//            String authorization = request.getHeader("Authorization");
//            if (StringUtils.isEmpty(authorization)) {
//                context.responseError(GatewayRequestError.ClientError.AuthenticationError.MISSING_AUTHENTICATION);
//                return;
//            }
//            String[] s = authorization.split(" ", 2);
//            Map<String, String> signatureMap = new HashMap<>();
//            // 从Authorization里面获取Signature、Nonce和Timestamp的值
//            for (String s1 : s[1].split(",")) {
//                String[] split = s1.split("=", 2);
//                signatureMap.put(split[0].trim(), split[1].trim());
//            }
//            String nonce = signatureMap.get("Nonce");
//            if (nonce == null || nonceCache.getIfPresent(nonce) != null || nonce.length() > 40) {
//                fail(context, GatewayRequestError.ClientError.AuthenticationError.DUPLICATED_DIGEST_NONCE);
//                return;
//            }
//            nonceCache.put(nonce, nonce);
//            long timestamp = signatureMap.containsKey("Timestamp") ? Long.parseLong(signatureMap.get("Timestamp")) : 0L;
//            if (System.currentTimeMillis() - timestamp > REQUEST_EXPIRE_TIME || System.currentTimeMillis() < timestamp) {
//                fail(context, GatewayRequestError.ClientError.AuthenticationError.INVALID_DIGEST_TIMESTAMP);
//                return;
//            }
//
//            String data = method + "\n"
//                    + nonce + "\n"
//                    + timestamp + "\n"
//                    + getPathInfo(request) + "\n"
//                    + contentType + "\n"
//                    + getContentMD5(context);
//            if (!StringUtils.equalsIgnoreCase(SignatureUtil.hmacSHA256(configEntity.getAppSecret(), data), signatureMap.get("Signature"))) {
//                fail(context, GatewayRequestError.ClientError.AuthenticationError.SIGNATURE_NOT_MATCH);
//            }
//        } catch (Exception e) {
//            DPLogger.getLogger().error("Digest signature error.", e);
//            fail(context, GatewayRequestError.ClientError.AuthenticationError.DIGEST_AUTH_FAILED);
//            context.setException(GatewayUtil.exceptionToString(e));
//        }
//    }
//
//    @Override
//    public HandlerType<DigestSignatureAuthConfigEntity, DigestSignatureAuthHandler> getType() {
//        return this.handlerType;
//    }
//
//    /**
//     * post请求，BODY中的参数字符串计算MD5值
//     */
//    private String getContentMD5(GatewayContext context) throws IOException {
//        HttpServletRequest request = context.getRequest();
//        if ("POST".equalsIgnoreCase(request.getMethod())) {
//            String contentType = request.getHeader("Content-Type");
//            String body = "";
//            if (ContentType.APPLICATION_JSON.getMimeType().equalsIgnoreCase(contentType)) {
//                body = context.getRequestBody();
//            } else if (ContentType.APPLICATION_FORM_URLENCODED.getMimeType().equalsIgnoreCase(contentType)) {
//                body = getParamMapAsString(request);
//            }
//            // FDL-7594 windows的换行符是\r\n，统一替换为\n
//            body = body.replace("\r\n", "\n");
//            return StringUtils.isEmpty(body) ? StringUtils.EMPTY : SignatureUtil.md5(body);
//        }
//        return StringUtils.EMPTY;
//    }
//
//    private String getPathInfo(HttpServletRequest request) {
//        String pathInfo = request.getPathInfo().substring(1);
//        return StringUtils.isEmpty(request.getQueryString()) ? pathInfo : pathInfo + "?" + request.getQueryString();
//    }
//
//    /**
//     * 请求参数的键值对的参数用&连接起来
//     */
//    private String getParamMapAsString(HttpServletRequest request) throws UnsupportedEncodingException {
//        List<String> paramList = new ArrayList<>();
//        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
//            paramList.add(URLEncoder.encode(entry.getKey(), "utf-8") + "=" + URLEncoder.encode(entry.getValue()[0], "utf-8"));
//        }
//        return String.join("&", paramList);
//    }
//
//    private void fail(GatewayContext context, GatewayRequestError.RequestErrorEntity error) {
//        context.responseError(error);
//    }
//
//    @Override
//    public int getSortIndex() {
//        return 0;
//    }
//}
