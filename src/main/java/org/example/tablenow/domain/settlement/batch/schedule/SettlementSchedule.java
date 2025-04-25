package org.example.tablenow.domain.settlement.batch.schedule;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.settlement.batch.util.JobTimeUtil;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class SettlementSchedule {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    // 정산 등록: 1시간마다
    @Scheduled(cron = "0/30 * * * * *", zone = "Asia/Seoul")
    public void runRegisterJob() throws Exception {

        System.out.println("▶ 정산 등록 Job 시작");
        String date = JobTimeUtil.getNowFormatted();
        runJob("settlementRegisterJob", "register");
    }

    // 정산 완료: 매일 자정
    @Scheduled(cron = "0 0/1 * * * *", zone = "Asia/Seoul")
    public void runCompleteJob() throws Exception {

        System.out.println("▶ 정산 완료 Job 시작");
        String date = JobTimeUtil.getNowFormatted();
        runJob("settlementCompleteJob", "complete");
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