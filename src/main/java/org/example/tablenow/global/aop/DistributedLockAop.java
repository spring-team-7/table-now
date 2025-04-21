package org.example.tablenow.global.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.tablenow.global.annotation.DistributedLock;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {

    private final RedissonClient redissonClient;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String prefix = distributedLock.prefix();
        String keyExpression = distributedLock.key();
        long waitTime = distributedLock.waitTime();
        long leaseTime = distributedLock.leaseTime();
        TimeUnit timeUnit = distributedLock.timeUnit();

        String evaluatedKey = parseKey(joinPoint, keyExpression);
        String lockKey = prefix + ":" + evaluatedKey;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(waitTime, leaseTime, timeUnit)) {
                log.warn("락 획득 실패: {}", lockKey);
                throw new HandledException(ErrorCode.RESERVATION_LOCK_TIMEOUT);
            }
            log.info("락 획득 성공: {}", lockKey);
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("인터럽트 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("예약 생성 중 오류 발생", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        EvaluationContext context = new MethodBasedEvaluationContext(
                null, method, joinPoint.getArgs(), new DefaultParameterNameDiscoverer()
        );
        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
