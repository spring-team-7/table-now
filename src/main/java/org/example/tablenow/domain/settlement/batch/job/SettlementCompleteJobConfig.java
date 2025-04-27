package org.example.tablenow.domain.settlement.batch.job;

import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.settlement.entity.Settlement;
import org.example.tablenow.domain.settlement.enums.SettlementStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
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

    @Qualifier("dataDBSource")
    private final DataSource dataDBSource;

    public SettlementCompleteJobConfig(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            @Qualifier("dataDBSource") DataSource dataDBSource
    ) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
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
            SELECT id, payment_id, amount, status
            FROM `table-now`.settlement
            WHERE status = 'READY'
        """);

        reader.setRowMapper((rs, rowNum) -> Settlement.builder()
                .id(rs.getLong("id"))
                .payment(Payment.builder().id(rs.getLong("payment_id")).build())
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
    public JdbcBatchItemWriter<Settlement> settlementCompleteWriter() {
        JdbcBatchItemWriter<Settlement> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataDBSource);
        writer.setSql("""
                UPDATE `table-now`.settlement
                SET status = :statusString,
                    updatedAt = NOW()
                WHERE id = :id
                """);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.afterPropertiesSet();

        return writer;
    }
}
