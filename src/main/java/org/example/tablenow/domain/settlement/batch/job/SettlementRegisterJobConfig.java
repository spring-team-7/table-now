package org.example.tablenow.domain.settlement.batch.job;

import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.settlement.entity.Settlement;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class SettlementRegisterJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Qualifier("dataDBSource")
    private final DataSource dataDBSource;

    public SettlementRegisterJobConfig(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            @Qualifier("dataDBSource") DataSource dataDBSource
    ) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.dataDBSource = dataDBSource;
    }

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
                .reader(paymentCursorReader())
                .processor(PaymentProcessor())
                .writer(settlementRegisterWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Payment> paymentCursorReader() {

        JdbcCursorItemReader<Payment> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataDBSource);

        reader.setSql("""
        SELECT
            p.id, p.price
        FROM `table-now`.payment p
        LEFT JOIN `table-now`.settlement s ON s.payment_id = p.id
        WHERE p.status = 'DONE' AND s.id IS NULL
        """);

        reader.setRowMapper((rs, rowNum) -> Payment.builder()
                .id(rs.getLong("id"))
                .price(rs.getInt("price"))
                .build());

        return reader;
    }

    @Bean
    public ItemProcessor<Payment, Settlement> PaymentProcessor() {

        return Settlement::fromPayment;
    }

    @Bean
    public JdbcBatchItemWriter<Settlement> settlementRegisterWriter() {
        return new JdbcBatchItemWriterBuilder<Settlement>()
                .dataSource(dataDBSource)
                .sql("""
                        INSERT INTO settlement (payment_id, amount, status, createdAt, updatedAt)
                        VALUES (:payment.id, :amount, :statusName, now(), now())
                        """)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }
}
