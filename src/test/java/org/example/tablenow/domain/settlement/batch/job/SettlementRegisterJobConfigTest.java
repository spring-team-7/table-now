package org.example.tablenow.domain.settlement.batch.job;

import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.settlement.entity.Settlement;
import org.example.tablenow.domain.settlement.enums.SettlementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

class SettlementRegisterJobConfigTest {

    private SettlementRegisterJobConfig config;

    @BeforeEach
    void setUp() {

        DataSource mockDataSource = mock(DataSource.class);
        config = new SettlementRegisterJobConfig(
                mock(JobRepository.class),
                mock(PlatformTransactionManager.class),
                mockDataSource
        );
    }

    @Test
    void 정산_등록_Job_생성_성공() {

        // when
        Job job = config.settlementRegisterJob();

        // then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("settlementRegisterJob");
    }

    @Test
    void 정산_등록_Step_생성_성공() {

        // when
        Step step = config.settlementRegisterStep();

        // then
        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("settlementRegisterStep");
    }

    @Test
    void 결제_읽기_Reader_생성_성공() {

        // when
        JdbcCursorItemReader<Payment> reader = config.paymentCursorReader();

        // then
        assertThat(reader).isNotNull();
    }

    @Test
    void 결제에서_정산으로_변환_Processor_생성_성공() throws Exception {

        // given
        ItemProcessor<Payment, Settlement> processor = config.PaymentProcessor();
        Payment payment = Payment.builder()
                .id(1L)
                .price(10000)
                .build();

        // when
        Settlement settlement = processor.process(payment);

        // then
        assertAll(
                () -> assertThat(settlement.getAmount()).isEqualTo(10000),
                () -> assertThat(settlement.getPayment().getId()).isEqualTo(1L),
                () -> assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.READY)
        );
    }

    @Test
    void 정산_등록_Writer_생성_성공() {

        // when
        JdbcBatchItemWriter<Settlement> writer = config.settlementRegisterWriter();

        // then
        assertThat(writer).isNotNull();
    }
}
