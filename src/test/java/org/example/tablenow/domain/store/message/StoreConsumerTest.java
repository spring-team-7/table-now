package org.example.tablenow.domain.store.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.domain.store.message.consumer.StoreConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreConsumerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private StoreConsumer storeConsumer;

    @Nested
    class DLQ_테스트 {
        private MessageProperties props = new MessageProperties();
        private Message message;

        @Test
        void 가게_생성_최대_재시도_초과시_재전송_안함() throws JsonProcessingException {
            // given
            StoreDocument storeDocument = mock(StoreDocument.class);
            byte[] body = new ObjectMapper().writeValueAsBytes(storeDocument);
            message = MessageBuilder
                    .withBody(body)
                    .andProperties(props)
                    .build();

            props.setHeader("x-retry-count", 3);

            // when
            storeConsumer.handleCreateDlq(message);

            // then
            verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Message.class));
        }

        @Test
        void 가게_생성_메시지_재전송() throws JsonProcessingException {
            // given
            StoreDocument storeDocument = mock(StoreDocument.class);
            byte[] body = new ObjectMapper().writeValueAsBytes(storeDocument);
            message = MessageBuilder
                    .withBody(body)
                    .andProperties(props)
                    .build();

            props.setHeader("x-retry-count", 2);

            // when
            storeConsumer.handleCreateDlq(message);

            // then
            verify(rabbitTemplate).convertAndSend(anyString(), any(Message.class));
        }

        @Test
        void 가게_수정_최대_재시도_초과시_재전송_안함() throws JsonProcessingException {
            // given
            StoreDocument storeDocument = mock(StoreDocument.class);
            byte[] body = new ObjectMapper().writeValueAsBytes(storeDocument);
            message = MessageBuilder
                    .withBody(body)
                    .andProperties(props)
                    .build();

            props.setHeader("x-retry-count", 3);

            // when
            storeConsumer.handleUpdateDlq(message);

            // then
            verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Message.class));
        }

        @Test
        void 가게_수정_메시지_재전송() throws JsonProcessingException {
            // given
            StoreDocument storeDocument = mock(StoreDocument.class);
            byte[] body = new ObjectMapper().writeValueAsBytes(storeDocument);
            message = MessageBuilder
                    .withBody(body)
                    .andProperties(props)
                    .build();

            props.setHeader("x-retry-count", 2);

            // when
            storeConsumer.handleUpdateDlq(message);

            // then
            verify(rabbitTemplate).convertAndSend(anyString(), any(Message.class));
        }

        @Test
        void 가게_삭제_최대_재시도_초과시_재전송_안함() throws JsonProcessingException {
            // given
            Long storeId = 1L;
            byte[] body = new ObjectMapper().writeValueAsBytes(storeId);
            message = MessageBuilder
                    .withBody(body)
                    .andProperties(props)
                    .build();

            props.setHeader("x-retry-count", 3);

            // when
            storeConsumer.handleDeleteDlq(message);

            // then
            verify(rabbitTemplate, never()).convertAndSend(anyString(), any(Message.class));
        }

        @Test
        void 가게_삭제_메시지_재전송() throws JsonProcessingException {
            // given
            Long storeId = 1L;
            byte[] body = new ObjectMapper().writeValueAsBytes(storeId);
            message = MessageBuilder
                    .withBody(body)
                    .andProperties(props)
                    .build();

            props.setHeader("x-retry-count", 2);

            // when
            storeConsumer.handleDeleteDlq(message);

            // then
            verify(rabbitTemplate).convertAndSend(anyString(), any(Message.class));
        }
    }
}
