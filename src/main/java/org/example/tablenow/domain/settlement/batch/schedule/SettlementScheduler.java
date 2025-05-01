package org.example.tablenow.domain.settlement.batch.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.settlement.batch.util.JobTimeUtil;
import org.redisson.api.RedissonClient;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

import static org.example.tablenow.global.constant.RedisKeyConstants.SETTLEMENT_SCHEDULE_LOCK_KEY;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final RedissonClient redissonClient;
// git commit -m "feat(settlement): SettlementScheduler에 Redisson Lock 적용 #"
    // 정산 등록: 1시간마다
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void runRegisterJob() throws Exception {

        log.info("정산 등록 Job 시작");

        RLock lock = redissonClient.getLock(SETTLEMENT_SCHEDULE_LOCK_KEY);
        lock.lock();

        try {
            runJob("settlementRegisterJob", "register");
        } finally {
            lock.unlock();
        }
    }

    // 정산 완료: 매일 00:00:01
    @Scheduled(cron = "1 0 0 * * *", zone = "Asia/Seoul")
    public void runCompleteJob() throws Exception {

        log.info("정산 완료 Job 대기 중");

        RLock lock = redissonClient.getLock(SETTLEMENT_SCHEDULE_LOCK_KEY);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(1, 10, TimeUnit.MINUTES);

            if (!isLocked) {
                log.warn("정산 완료 Job 락 획득 실패, 선행 Job이 아직 완료되지 않음(Job Skipped)");
                return;
            }

            log.info("정산 완료 Job 시작");
            runJob("settlementCompleteJob", "complete");
        } finally {
            lock.unlock();
        }
    }

    private void runJob(String jobName, String type) throws Exception {
        JobParameters parameters = buildJobParameters(type);
        jobLauncher.run(jobRegistry.getJob(jobName), parameters);
    }

    private JobParameters buildJobParameters(String type) {
        return new JobParametersBuilder()
                .addString("date", JobTimeUtil.getNowFormatted())
                .addString("type", type)
                .toJobParameters();
    }
}