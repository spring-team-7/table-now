package org.example.tablenow.domain.notification.message.vacancy.service;

import org.example.tablenow.domain.notification.message.vacancy.dto.VacancyEventDto;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyRetryServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private VacancyRetryService vacancyRetryService;

    @Nested
    class DLQ_재처리 {

        MessageProperties props = new MessageProperties();
        Message message = mock(Message.class);

        VacancyEventDto dto = VacancyEventDto.builder()
            .storeId(1L)
            .waitDate(LocalDate.of(2025, 4, 25))
            .build();

        @Test
        void 최대_재시도_초과시_메시지_재전송_안함() {
            // given
            props.setHeader("x-retry-count", 3);
            given(message.getMessageProperties()).willReturn(props);

            MessageConverter mockConverter = mock(MessageConverter.class);
            given(rabbitTemplate.getMessageConverter()).willReturn(mockConverter);
            given(mockConverter.fromMessage(message)).willReturn(dto);

            // when
            vacancyRetryService.process(message);

            // then
            verify(rabbitTemplate).send(
                eq("vacancy.exchange"),
                eq("vacancy.routing.key"),
                any(Message.class)
            );
        }

        @Test
        void 메시지_재전송() {
            // given
            props.setHeader("x-retry-count", 2);
            given(message.getMessageProperties()).willReturn(props);

            MessageConverter mockConverter = mock(MessageConverter.class);
            given(rabbitTemplate.getMessageConverter()).willReturn(mockConverter);
            given(mockConverter.fromMessage(message)).willReturn(dto);
            given(mockConverter.toMessage(any(), any())).willReturn(mock(Message.class));

            // when
            vacancyRetryService.process(message);

            // then
            verify(rabbitTemplate).send(eq("vacancy.exchange"), eq("vacancy.routing.key"), any());
        }
    }
}