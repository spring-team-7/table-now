package org.example.tablenow.domain.settlement.batch.job;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.payment.enums.PaymentStatus;
import org.example.tablenow.domain.payment.repository.PaymentRepository;
import org.example.tablenow.domain.settlement.entity.Settlement;
import org.example.tablenow.domain.settlement.repository.SettlementRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SettlementRegisterJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;

    @Bean
    public Job settlementRegisterJob() {

        return new JobBuilder("settlementRegisterJob", jobRepository)
                .start(settlementRegisterStep())
                .build();
    }

    @Bean
    public Step settlementRegisterStep() {

        return new StepBuilder("settlementRegisterStep", jobRepository)
                .<Payment, Settlement>chunk(100, platformTransactionManager)
                .reader(unsettledPaymentReader())
                .processor(settlementProcessor())
                .writer(settlementRegisterWriter())
                .build();
    }

    // RepositoryItemReader 방식
    @Bean
    public RepositoryItemReader<Payment> unsettledPaymentReader() {

        return new RepositoryItemReaderBuilder<Payment>()
                .name("unsettledPaymentReader")
                .repository(paymentRepository)
                .methodName("findUnsettledDonePayments")
                .sorts(Map.of("id", Sort.Direction.ASC))
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<Payment, Settlement> settlementProcessor() {

        return Settlement::fromPayment;
    }

    @Bean
    public RepositoryItemWriter<Settlement> settlementRegisterWriter() {

        return new RepositoryItemWriterBuilder<Settlement>()
                .repository(settlementRepository)
                .methodName("save")
                .build();
    }
}
