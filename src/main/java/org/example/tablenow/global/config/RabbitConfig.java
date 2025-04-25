package org.example.tablenow.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.example.tablenow.global.constant.RabbitConstant.*;

@Configuration
public class RabbitConfig {

    // vacancy Queue 등록(durable=true : 서버 재시작 후에도 큐가 사라지지 않게 함)
    @Bean
    public Queue vacancyQueue() {
        return QueueBuilder.durable(VACANCY_QUEUE)
            .withArgument("x-dead-letter-exchange", VACANCY_DLX) //DLX 설정
            .withArgument("x-dead-letter-routing-key", VACANCY_DLQ)
            .build();
    }

    // vacancy Exchange 등록(Direct: routing key가 정확히 일치할 때만 메시지를 보냄)
    @Bean
    public DirectExchange vacancyExchange(){
        return new DirectExchange(VACANCY_EXCHANGE);
    }
    // vacancy Exchange와 Queue 바인딩
    @Bean
    public Binding vacancyBinding(){
        return BindingBuilder.bind(vacancyQueue()).to(vacancyExchange()).with(VACANCY_ROUTING_KEY);
    }

    // DLX 등록
    @Bean
    public DirectExchange vacancyDlx(){
        return new DirectExchange(VACANCY_DLX);
    }

    // DLQ 등록 ( 실패 메시지 저장할 큐)
    @Bean
    public Queue vacancyDlq(){
        return new Queue(VACANCY_DLQ, true);
    }

    // DLX와 DLQ를 라우팅 키로 바인딩
    @Bean
    public Binding vacancyDlqBinding(){
        return BindingBuilder.bind(vacancyDlq())
            .to(vacancyDlx())
            .with(VACANCY_DLQ);
    }

    // 이벤트 오픈 Queue, Exchange, Binding
    @Bean
    public Queue eventOpenQueue() {
        return new Queue(EVENT_OPEN_QUEUE, true);
    }

    @Bean
    public FanoutExchange eventOpenExchange() {
        return new FanoutExchange(EVENT_OPEN_EXCHANGE, true, false);
    }

    @Bean
    public Binding eventOpenBinding() {
        return BindingBuilder.bind(eventOpenQueue())
                .to(eventOpenExchange());
    }

    // 예약 리마인드 등록 Queue, Exchange, Binding
    @Bean
    public Queue reminderRegisterQueue() {
        return new Queue(RESERVATION_REMINDER_REGISTER_QUEUE, true);
    }

    @Bean
    public DirectExchange reminderRegisterExchange() {
        return new DirectExchange(RESERVATION_REMINDER_REGISTER_EXCHANGE);
    }

    @Bean
    public Binding reminderRegisterBinding() {
        return BindingBuilder
                .bind(reminderRegisterQueue())
                .to(reminderRegisterExchange())
                .with(RESERVATION_REMINDER_REGISTER_ROUTING_KEY);
    }

    // 예약 리마인드 발송 Queue, Exchange, Binding
    @Bean
    public DirectExchange reminderSendExchange() {
        return new DirectExchange(RESERVATION_REMINDER_SEND_EXCHANGE);
    }

    @Bean
    public Queue reminderSendQueue() {
        return new Queue(RESERVATION_REMINDER_SEND_QUEUE, true);
    }

    @Bean
    public Binding reminderSendBinding() {
        return BindingBuilder
                .bind(reminderSendQueue())
                .to(reminderSendExchange())
                .with(RESERVATION_REMINDER_SEND_ROUTING_KEY);
    }

    // 가게 데이터 변경 (Create/Update/Delete) Queue, Exchange, Binding
    @Bean
    public Queue storeCreateQueue() {
        return new Queue(STORE_CREATE_QUEUE, true);
    }
    @Bean
    public Queue storeUpdateQueue() {
        return new Queue(STORE_UPDATE_QUEUE, true);
    }
    @Bean
    public Queue storeDeleteQueue() {
        return new Queue(STORE_DELETE_QUEUE, true);
    }
    @Bean
    public DirectExchange storeExchange(){
        return new DirectExchange(STORE_EXCHANGE);
    }
    @Bean
    public Binding storeCreateBinding(){
        return BindingBuilder.bind(storeCreateQueue()).to(storeExchange()).with(STORE_CREATE);
    }
    @Bean
    public Binding storeUpdateBinding(){
        return BindingBuilder.bind(storeUpdateQueue()).to(storeExchange()).with(STORE_UPDATE);
    }
    @Bean
    public Binding storeDeleteBinding(){
        return BindingBuilder.bind(storeDeleteQueue()).to(storeExchange()).with(STORE_DELETE);
    }

    // 공통 설정
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
