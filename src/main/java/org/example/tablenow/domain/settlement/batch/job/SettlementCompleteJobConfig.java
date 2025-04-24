package org.example.tablenow.domain.settlement.batch.job;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.settlement.entity.Settlement;
import org.example.tablenow.domain.settlement.enums.SettlementStatus;
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
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SettlementCompleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final SettlementRepository settlementRepository;

    @Bean
    public Job settlementCompleteJob() {

        return new JobBuilder("settlementCompleteJob", jobRepository)
                .start(settlementCompleteStep())
                .build();
    }

    @Bean
    public Step settlementCompleteStep() {

        return new StepBuilder("settlementCompleteStep", jobRepository)
                .<Settlement, Settlement>chunk(100, platformTransactionManager)
                .reader(readySettlementReader())
                .processor(settlementStatusUpdater())
                .writer(settlementCompleteWriter())
                .build();
    }

    // RepositoryItemReader 방식
    @Bean
    public RepositoryItemReader<Settlement> readySettlementReader() {

        return new RepositoryItemReaderBuilder<Settlement>()
                .name("readySettlementReader")
                .repository(settlementRepository)
                .methodName("findAllByStatus")
                .arguments(SettlementStatus.READY)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<Settlement, Settlement> settlementStatusUpdater() {

        return settlement -> {
            settlement.done();
            return settlement;
        };
    }

    @Bean
    public RepositoryItemWriter<Settlement> settlementCompleteWriter() {

        return new RepositoryItemWriterBuilder<Settlement>()
                .repository(settlementRepository)
                .methodName("save")
                .build();
    }
}
