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
        return QueueBuilder.durable(EVENT_OPEN_QUEUE)
                .withArgument("x-dead-letter-exchange", EVENT_OPEN_DLX)
                .withArgument("x-dead-letter-routing-key", EVENT_OPEN_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public FanoutExchange eventOpenExchange() {
        return new FanoutExchange(EVENT_OPEN_EXCHANGE, true, false);
    }

    @Bean
    public Binding eventOpenBinding() {
        return bindFanout(eventOpenQueue(), eventOpenExchange());
    }

    // 이벤트 오픈 DLX 및 DLQ 설정
    @Bean
    public DirectExchange eventOpenDlx() {
        return new DirectExchange(EVENT_OPEN_DLX);
    }

    @Bean
    public Queue eventOpenDlq() {
        return buildDlqQueue(EVENT_OPEN_DLQ);
    }

    @Bean
    public Binding eventOpenDlqBinding() {
        return bind(eventOpenDlq(), eventOpenDlx(), EVENT_OPEN_DLQ_ROUTING_KEY);
    }

    // 이벤트 오픈 RetryQueue
    @Bean
    public Queue eventOpenRetryQueue() {
        return QueueBuilder.durable(EVENT_OPEN_RETRY_QUEUE)
                .withArgument("x-message-ttl", TTL_MILLIS)
                .withArgument("x-dead-letter-exchange", EVENT_OPEN_EXCHANGE)
                .build();
    }

    @Bean
    public DirectExchange eventOpenRetryExchange() {
        return new DirectExchange(EVENT_OPEN_RETRY_EXCHANGE);
    }

    @Bean
    public Binding eventOpenRetryBinding() {
        return BindingBuilder.bind(eventOpenRetryQueue())
                .to(eventOpenRetryExchange())
                .with(EVENT_OPEN_RETRY_ROUTING_KEY);
    }

    // 예약 리마인드 등록 Queue, Exchange, Binding
    @Bean
    public Queue reminderRegisterQueue() {
        return new Queue(RESERVATION_REMINDER_REGISTER_QUEUE, true);
    }

    @Bean
    public FanoutExchange reminderRegisterExchange() {
        return new FanoutExchange(RESERVATION_REMINDER_REGISTER_EXCHANGE, true, false);
    }

    @Bean
    public Binding reminderRegisterBinding() {
        return bindFanout(reminderRegisterQueue(), reminderRegisterExchange());
    }

    // 예약 리마인드 발송 Queue, Exchange, Binding
    @Bean
    public Queue reminderSendQueue() {
        return QueueBuilder.durable(RESERVATION_REMINDER_SEND_QUEUE)
                .withArgument("x-dead-letter-exchange", RESERVATION_REMINDER_SEND_DLX)
                .withArgument("x-dead-letter-routing-key", RESERVATION_REMINDER_SEND_DLQ)
                .build();
    }

    @Bean
    public FanoutExchange reminderSendExchange() {
        return new FanoutExchange(RESERVATION_REMINDER_SEND_EXCHANGE, true, false);
    }

    @Bean
    public Binding reminderSendBinding() {
        return bindFanout(reminderSendQueue(), reminderSendExchange());
    }

    // 예약 리마인드 DLX 및 DLQ 설정
    @Bean
    public DirectExchange reminderSendDlx() {
        return new DirectExchange(RESERVATION_REMINDER_SEND_DLX);
    }

    @Bean
    public Queue reminderSendDlq() {
        return buildDlqQueue(RESERVATION_REMINDER_SEND_DLQ);
    }

    @Bean
    public Binding reminderSendDlqBinding() {
        return bind(reminderSendDlq(), reminderSendDlx(), RESERVATION_REMINDER_SEND_DLQ);
    }

    // 예약 리마인드 RetryQueue
    @Bean
    public Queue reminderSendRetryQueue() {
        return QueueBuilder.durable(RESERVATION_REMINDER_SEND_RETRY_QUEUE)
                .withArgument("x-message-ttl", TTL_MILLIS)
                .withArgument("x-dead-letter-exchange", RESERVATION_REMINDER_SEND_EXCHANGE)
                .build();
    }

    @Bean
    public FanoutExchange reminderSendRetryExchange() {
        return new FanoutExchange(RESERVATION_REMINDER_SEND_RETRY_EXCHANGE, true, false);
    }

    @Bean
    public Binding reminderSendRetryBinding() {
        return bindFanout(reminderSendRetryQueue(), reminderSendRetryExchange());
    }

    // 가게 데이터 변경 (Create/Update/Delete) Queue, Exchange, Binding
    @Bean
    public Queue storeCreateQueue() {
        return buildMainQueue(STORE_CREATE_QUEUE, STORE_CREATE_DLQ);
    }

    @Bean
    public Queue storeUpdateQueue() {
        return buildMainQueue(STORE_UPDATE_QUEUE, STORE_UPDATE_DLQ);
    }

    @Bean
    public Queue storeDeleteQueue() {
        return buildMainQueue(STORE_DELETE_QUEUE, STORE_DELETE_DLQ);
    }

    @Bean
    public Queue storeCreateDlq() {
        return buildDlqQueue(STORE_CREATE_DLQ);
    }

    @Bean
    public Queue storeUpdateDlq() {
        return buildDlqQueue(STORE_UPDATE_DLQ);
    }

    @Bean
    public Queue storeDeleteDlq() {
        return buildDlqQueue(STORE_DELETE_DLQ);
    }

    @Bean
    public DirectExchange storeExchange() {
        return new DirectExchange(STORE_EXCHANGE);
    }

    @Bean
    public DirectExchange storeDlx() {
        return new DirectExchange(STORE_DLX);
    }

    @Bean
    public Binding storeCreateBinding(Queue storeCreateQueue, DirectExchange storeExchange) {
        return bind(storeCreateQueue, storeExchange, STORE_CREATE);
    }

    @Bean
    public Binding storeUpdateBinding(Queue storeUpdateQueue, DirectExchange storeExchange) {
        return bind(storeUpdateQueue, storeExchange, STORE_UPDATE);
    }

    @Bean
    public Binding storeDeleteBinding(Queue storeDeleteQueue, DirectExchange storeExchange) {
        return bind(storeDeleteQueue, storeExchange, STORE_DELETE);
    }

    @Bean
    public Binding storeCreateDlqBinding(Queue storeCreateDlq, DirectExchange storeDlx) {
        return bind(storeCreateDlq, storeDlx, STORE_CREATE_DLQ);
    }

    @Bean
    public Binding storeUpdateDlqBinding(Queue storeUpdateDlq, DirectExchange storeDlx) {
        return bind(storeUpdateDlq, storeDlx, STORE_UPDATE_DLQ);
    }

    @Bean
    public Binding storeDeleteDlqBinding(Queue storeDeleteDlq, DirectExchange storeDlx) {
        return bind(storeDeleteDlq, storeDlx, STORE_DELETE_DLQ);
    }

    // 채팅 알림 Queue, Exchange, Binding
    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE, true);
    }

    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(CHAT_EXCHANGE);
    }

    @Bean
    public Binding chatBinding() {
        return BindingBuilder
                .bind(chatQueue())
                .to(chatExchange())
                .with(CHAT_ROUTING_KEY);
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

    private Queue buildMainQueue(String queueName, String dlqRoutingKey) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", STORE_DLX)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .withArgument("x-message-ttl", TTL_MILLIS)
                .build();
    }

    private Queue buildDlqQueue(String dlqName) {
        return QueueBuilder.durable(dlqName).build();
    }

    private Binding bind(Queue queue, DirectExchange exchange, String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    private Binding bindFanout(Queue queue, FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }
}
