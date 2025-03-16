package com.fr.dp.service.limiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class created on 2023/8/22
 *
 * @author Kuifang.Liu
 */
public class LocalRateLimiter implements RateLimiter {
    private static final Map<String, LimitEntity> limitCache = new ConcurrentHashMap<>();

    private final String key;
    //限制次数
    private final int limit;
    //时间段，单位为s
    private final int expire;

    public LocalRateLimiter(String key, int limit, int expire) {
        this.key = key;
        this.limit = limit;
        this.expire = expire;
    }

    @Override
    public boolean tryAcquire() {
        // todo 实现有点问题，非滑动窗口式精确限流
        return limitCache.compute(key, (s, limitEntity) -> {
            if (limitEntity == null || limitEntity.getExpireTimestamp() < System.currentTimeMillis()) {
                limitEntity = new LimitEntity(expire);
            }
            return limitEntity;
        }).tryAcquire(limit);
    }

    private static class LimitEntity {
        private final long expireTimestamp;
        private final AtomicInteger count = new AtomicInteger();

        public LimitEntity(int expire) {
            this.expireTimestamp = System.currentTimeMillis() + expire * 1000L;
        }

        public long getExpireTimestamp() {
            return expireTimestamp;
        }

        public boolean tryAcquire(int limit) {
            return incr() < limit;
        }

        public int incr() {
            return count.getAndIncrement();
        }
    }
}
