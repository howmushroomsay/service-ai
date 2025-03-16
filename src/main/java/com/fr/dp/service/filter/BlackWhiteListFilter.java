package com.fr.dp.service.filter;

import com.fr.dp.service.context.BuilderContext;
import com.fr.dp.service.context.RequestContext;
import com.fr.dp.service.utils.IPUtil;
import com.fr.dp.service.utils.StringUtils;

import java.util.List;

public class BlackWhiteListFilter implements ServiceFilter{

    private final String checkType;

    private final List<String> blackList;

    private final List<String> whiteList;

    private int order;

    public BlackWhiteListFilter(List<String> blackList, List<String> whiteList, String checkType, int order) {
        this.blackList = blackList;
        this.whiteList = whiteList;
        this.checkType = checkType;
        this.order = order;
    }

    @Override
    public void filter(RequestContext context, BuilderContext builderContext) throws Exception {
        String ip = context.getIp();
        if (!checkBlacklist(ip)) {
            // todo 设计一下异常
            throw new Exception("IP在黑名单呢");
        } else if (!checkWhitelist(ip)) {
            throw new Exception("IP不在白名单呢");
        }
    }

    private boolean checkBlacklist(String ip) {
        if (StringUtils.isEmpty(checkType)) {
            return true;
        }
        if ("BLACK".equals(checkType)) {
            if (StringUtils.isEmpty(ip)) {
                return false;
            }
            return blackList.stream().noneMatch(b -> {
                if (IPUtil.isValidIp(b)) {
                    return b.equals(ip);
                } else if (IPUtil.isValidCidr(b)) {
                    return IPUtil.isIpInRange(ip, b);
                }
                return false;
            });
        }
        return true;
    }

    private boolean checkWhitelist(String ip) {
        if (StringUtils.isEmpty(checkType)) {
            return true;
        }
        if ("WHITE".equals(checkType)) {
            if (StringUtils.isEmpty(ip)) {
                return false;
            }
            return whiteList.stream().anyMatch(w -> {
                if (IPUtil.isValidIp(w)) {
                    return w.equals(ip);
                } else if (IPUtil.isValidCidr(w)) {
                    return IPUtil.isIpInRange(ip, w);
                }
                return false;
            });
        }
        return true;
    }

    @Override
    public int getOrder() {
        return order;
    }
}
