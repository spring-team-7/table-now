package org.example.tablenow.domain.settlement.batch.job;

import org.example.tablenow.domain.payment.entity.Payment;
import org.example.tablenow.domain.payment.enums.PaymentStatus;
import org.example.tablenow.domain.payment.service.PaymentService;
import org.example.tablenow.domain.settlement.entity.Settlement;
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
public class SettlementRegisterJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final PaymentService paymentService;
    private final SettlementRepository settlementRepository;

    @Qualifier("dataDBSource")
    private final DataSource dataDBSource;

    public SettlementRegisterJobConfig(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            PaymentService paymentService,
            SettlementRepository settlementRepository,
            @Qualifier("dataDBSource") DataSource dataDBSource
    ) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.paymentService = paymentService;
        this.settlementRepository = settlementRepository;
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
                .processor(persistentPaymentProcessor())
                .writer(settlementRegisterWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Payment> paymentCursorReader() {

        JdbcCursorItemReader<Payment> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataDBSource);
        reader.setSql("""
        SELECT
            p.id, p.paymentKey, p.method, p.price, p.status, p.user_id, p.reservation_id, p.createdAt, p.updatedAt
        FROM `table-now`.payment p
        LEFT JOIN `table-now`.settlement s ON s.payment_id = p.id
        WHERE p.status = 'DONE' AND s.id IS NULL
        """);

        reader.setRowMapper((rs, rowNum) -> Payment.builder()
                .id(rs.getLong("id"))
                .paymentKey(rs.getString("paymentKey"))
                .method(rs.getString("method")) // 그대로 string
                .price(rs.getInt("price"))
                .status(PaymentStatus.valueOf(rs.getString("status"))) // enum은 여전히 변환 필요
                .build());
        return reader;
    }

    @Bean
    public ItemProcessor<Payment, Settlement> persistentPaymentProcessor() {
        return payment -> {
            // paymentRepository를 통해 영속 객체 다시 조회
            Payment persistent = paymentService.getVerifiedPaymentById(payment.getId());

            return Settlement.fromPayment(persistent);
        };
    }

    @Bean
    public RepositoryItemWriter<Settlement> settlementRegisterWriter() {

        return new RepositoryItemWriterBuilder<Settlement>()
                .repository(settlementRepository)
                .methodName("save")
                .build();
    }
}
