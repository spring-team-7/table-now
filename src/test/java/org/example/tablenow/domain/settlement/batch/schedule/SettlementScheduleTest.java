package org.example.tablenow.domain.settlement.batch.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettlementScheduleTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private JobRegistry jobRegistry;

    @Mock
    private Job job;

    @Mock
    private JobExecution jobExecution;

    @InjectMocks
    private SettlementSchedule settlementSchedule;

    @BeforeEach
    void setUp() throws Exception {
        given(jobLauncher.run(any(Job.class), any(JobParameters.class))).willReturn(jobExecution);
    }

    @Test
    void 정산_등록_Job_실행_성공() throws Exception {
        // given
        given(jobRegistry.getJob("settlementRegisterJob")).willReturn(job);

        // when
        settlementSchedule.runRegisterJob();

        // then
        verify(jobRegistry).getJob("settlementRegisterJob");
        verify(jobLauncher).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    void 정산_완료_Job_실행_성공() throws Exception {
        // given
        given(jobRegistry.getJob("settlementCompleteJob")).willReturn(job);

        // when
        settlementSchedule.runCompleteJob();

        // then
        verify(jobRegistry).getJob("settlementCompleteJob");
        verify(jobLauncher).run(any(Job.class), any(JobParameters.class));
    }
}