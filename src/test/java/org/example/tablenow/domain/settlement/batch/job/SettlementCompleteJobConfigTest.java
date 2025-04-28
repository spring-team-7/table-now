package org.example.tablenow.domain.settlement.batch.job;

import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.settlement.entity.Settlement;
import org.example.tablenow.domain.settlement.enums.SettlementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SettlementCompleteJobConfigTest {

    private SettlementCompleteJobConfig config;

    @BeforeEach
    void setUp() {

        DataSource mockDataSource = mock(DataSource.class);
        config = new SettlementCompleteJobConfig(
                mock(JobRepository.class),
                mock(PlatformTransactionManager.class),
                mockDataSource
        );
    }

    @Test
    void 정산_완료_Job_생성_성공() {

        // when
        Job job = config.settlementCompleteJob();

        // then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("settlementCompleteJob");
    }

    @Test
    void 정산_완료_Step_생성_성공() {

        // when
        Step step = config.settlementCompleteStep();

        // then
        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("settlementCompleteStep");
    }

    @Test
    void 정산_완료_Reader_생성_성공() {
        // when
        JdbcCursorItemReader<Settlement> reader = config.readySettlementCursorReader();

        // then
        assertThat(reader).isNotNull();
    }

    @Test
    void 정산_완료_상태_업데이트_Processor_생성_성공() throws Exception {

        // given
        ItemProcessor<Settlement, Settlement> processor = config.settlementStatusUpdater();
        Settlement settlement = Settlement.builder()
                .id(1L)
                .amount(10000)
                .status(SettlementStatus.READY)
                .payment(Payment.builder().id(1L).build())
                .build();

        // when
        Settlement processed = processor.process(settlement);

        // then
        assertThat(processed.getStatus()).isEqualTo(SettlementStatus.DONE);
    }

    @Test
    void 정산_완료_Writer_생성_성공() {

        // when
        JdbcBatchItemWriter<Settlement> writer = config.settlementCompleteWriter();

        // then
        assertThat(writer).isNotNull();
    }
}
