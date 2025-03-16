package com.fr.dp.service.limiter;

/**
 * This class created on 2023/8/21
 *
 * @author Kuifang.Liu
 */
public interface RateLimiter {
    /**
     * 尝试获取请求资格
     *
     * @return 请求频率未超出限制，返回true；超出限制返回false
     */
    boolean tryAcquire();
}
