package org.example.tablenow.domain.settlement.batch.job;

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
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class SettlementCompleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final SettlementRepository settlementRepository;

    @Qualifier("dataDBSource")
    private final DataSource dataDBSource;

    public SettlementCompleteJobConfig(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            SettlementRepository settlementRepository,
            @Qualifier("dataDBSource") DataSource dataDBSource
    ) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.settlementRepository = settlementRepository;
        this.dataDBSource = dataDBSource;
    }

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
                .reader(readySettlementCursorReader())
                .processor(settlementStatusUpdater())
                .writer(settlementCompleteWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Settlement> readySettlementCursorReader() {
        JdbcCursorItemReader<Settlement> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataDBSource);
        reader.setSql("""
            SELECT id, payment_id, amount, status, createdAt, updatedAt
            FROM `table-now`.settlement
            WHERE status = 'READY'
        """);

        reader.setRowMapper((rs, rowNum) -> Settlement.builder()
                .id(rs.getLong("id"))
                .payment(Payment.builder()
                        .id(rs.getLong("payment_id")) // 영속성 보장 위해 ID만 세팅
                        .build())
                .amount(rs.getInt("amount"))
                .status(SettlementStatus.valueOf(rs.getString("status")))
                .build());

        return reader;
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
