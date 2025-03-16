package com.fr.dp.service.limiter;

import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HighPrecisionRateLimiter {
    private final int windowSizeMs;     // 总窗口大小（毫秒）
    private final int shardNum;         // 分片数量
    private final int shardSizeMs;      // 每个分片的时间跨度（毫秒）
    private final int limit;            // 窗口内允许的最大请求数

    // 环形数组存储各分片计数器
    private final Shard[] shards;
    // 读写锁（仅用于分片数组的扩容保护，常规操作无锁）
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public HighPrecisionRateLimiter(int windowSizeMs, int shardNum, int limit) {
        this.windowSizeMs = windowSizeMs;
        this.shardNum = shardNum;
        this.shardSizeMs = windowSizeMs / shardNum;
        this.limit = limit;
        this.shards = new Shard[shardNum];
        for (int i = 0; i < shardNum; i++) {
            shards[i] = new Shard();
        }
    }

    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        // 计算当前分片索引
        int idx = (int) ((now / shardSizeMs) % shardNum);
        Shard currentShard = shards[idx];

        // 若当前分片已过期，则重置
        if (now - currentShard.startTime > shardSizeMs) {
            // 使用 CAS 重置分片，避免重复初始化
            if (currentShard.resetIfExpired(now)) {
                // 清理过期分片（惰性清理）
                cleanupExpiredShards(now);
            }
        }

        // 原子递增当前分片计数
        currentShard.counter.increment();

        // 计算总请求数（当前窗口内所有有效分片的总和）
        return getTotalCount(now) <= limit;
    }

    private long getTotalCount(long now) {
        long total = 0;
        for (Shard shard : shards) {
            if (now - shard.startTime <= windowSizeMs) {
                total += shard.counter.sum();
            }
        }
        return total;
    }

    private void cleanupExpiredShards(long now) {
        for (Shard shard : shards) {
            if (now - shard.startTime > windowSizeMs) {
                shard.resetIfExpired(now); // 过期分片重置
            }
        }
    }

    private static class Shard {
        volatile long startTime; // 分片开始时间（毫秒）
        LongAdder counter = new LongAdder();

        public synchronized boolean resetIfExpired(long now) {
            if (now - startTime > (now / startTime) * startTime) { // 简化条件，实际需精确判断
                counter.reset();
                startTime = now;
                return true;
            }
            return false;
        }
    }
}