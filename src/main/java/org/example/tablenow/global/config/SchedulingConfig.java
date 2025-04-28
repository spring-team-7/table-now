package org.example.tablenow.global.config;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableSchedulerLock(defaultLockAtLeastFor = "30s", defaultLockAtMostFor = "1m")
@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class SchedulingConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    public LockProvider lockProvider() {
        return new RedisLockProvider(redisConnectionFactory);
    }
}
