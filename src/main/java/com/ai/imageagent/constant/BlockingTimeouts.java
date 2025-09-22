package com.ai.imageagent.constant;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 阻塞操作常用超时时间常量
 */
public interface BlockingTimeouts {

    // 默认阻塞超时时间
    Duration DEFAULT = Duration.ofSeconds(600); // 10 分钟

    // 短时间阻塞
    Duration SHORT = Duration.ofSeconds(30);   // 30 秒

    // 中等时间阻塞
    Duration MEDIUM = Duration.ofMinutes(5);   // 5 分钟

    // 长时间阻塞
    Duration LONG = Duration.ofMinutes(30);    // 30 分钟

    // 超长时间阻塞
    Duration VERY_LONG = Duration.ofHours(2);  // 2 小时

    // 纳秒形式常量（适用于 BlockingSingleSubscriber.blockingGet(long, TimeUnit)）
    long DEFAULT_NANOS = TimeUnit.SECONDS.toNanos(600);
    long SHORT_NANOS   = TimeUnit.SECONDS.toNanos(30);
    long MEDIUM_NANOS  = TimeUnit.MINUTES.toNanos(5);
    long LONG_NANOS    = TimeUnit.MINUTES.toNanos(30);
    long VERY_LONG_NANOS = TimeUnit.HOURS.toNanos(2);
}
