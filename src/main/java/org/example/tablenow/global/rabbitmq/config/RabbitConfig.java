package org.example.tablenow.global.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.example.tablenow.global.rabbitmq.constant.RabbitConstant.*;

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
