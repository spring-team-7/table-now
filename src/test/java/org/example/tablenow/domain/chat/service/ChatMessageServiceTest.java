package org.example.tablenow.domain.chat.service;

import org.example.tablenow.domain.chat.dto.request.ChatMessageRequest;
import org.example.tablenow.domain.chat.dto.response.ChatAvailabilityResponse;
import org.example.tablenow.domain.chat.dto.response.ChatMessageResponse;
import org.example.tablenow.domain.chat.dto.response.ChatReadStatusResponse;
import org.example.tablenow.domain.chat.entity.ChatMessage;
import org.example.tablenow.domain.chat.repository.ChatMessageRepository;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.service.UserService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ReservationService reservationService;
    @Mock
    private UserService userService;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private static final Long OWNER_ID = 1L;
    private static final Long RESERVATION_USER_ID = 2L;
    private static final Long RESERVATION_ID = 10L;

    @Nested
    class 채팅_저장_및_알림_발송 {

        Reservation reservation;

        @BeforeEach
        void setUp() {
            reservation = mockReservation(OWNER_ID, RESERVATION_USER_ID);
        }

        @Test
        void 채팅_참여자가_아니면_예외처리() {
            // given
            Long invalidSenderId = 99L;
            ChatMessageRequest request = new ChatMessageRequest(RESERVATION_ID, "Hello", null);

            given(chatMessageRepository.findTop1ByReservationIdOrderByCreatedAtDesc(RESERVATION_ID)).willReturn(Optional.empty());
            given(reservationService.getReservationWithStore(RESERVATION_ID)).willReturn(reservation);

            // when & then
            assertThatThrownBy(() -> chatMessageService.saveMessageAndNotify(request, invalidSenderId))
                    .isInstanceOf(HandledException.class)
                    .hasMessageContaining(ErrorCode.INVALID_CHAT_PARTICIPANT.getDefaultMessage());
        }

        @Test
        void 채팅_저장_및_알림_발송_성공() {
            // given
            Long senderId = OWNER_ID;
            ChatMessageRequest request = new ChatMessageRequest(RESERVATION_ID, "Hello", null);

            User sender = User.builder().id(senderId).build();

            given(chatMessageRepository.findTop1ByReservationIdOrderByCreatedAtDesc(RESERVATION_ID)).willReturn(Optional.empty());
            given(reservationService.getReservationWithStore(RESERVATION_ID)).willReturn(reservation);
            given(userService.getUser(senderId)).willReturn(sender);
            given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(buildChatMessage(request, senderId, reservation));

            // when
            ChatMessageResponse response = chatMessageService.saveMessageAndNotify(request, senderId);

            // then
            assertThat(response.getReservationId()).isEqualTo(RESERVATION_ID);
            verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(ChatMessageResponse.class));
        }
    }

    @Nested
    class 채팅_가능여부_조회 {

        Reservation reservation;

        @BeforeEach
        void setUp() {
            reservation = mockReservation(OWNER_ID, RESERVATION_USER_ID);
        }

        @Test
        void 채팅_참여자가_아니면_예외처리() {
            // given
            Long invalidUserId = 99L;

            given(chatMessageRepository.findTop1ByReservationIdOrderByCreatedAtDesc(RESERVATION_ID)).willReturn(Optional.empty());
            given(reservationService.getReservationWithStore(RESERVATION_ID)).willReturn(reservation);

            // when & then
            assertThatThrownBy(() -> chatMessageService.isChatAvailable(RESERVATION_ID, invalidUserId))
                    .isInstanceOf(HandledException.class)
                    .hasMessageContaining(ErrorCode.INVALID_CHAT_PARTICIPANT.getDefaultMessage());
        }

        @Test
        void 채팅_가능여부_조회_성공() {
            // given
            Long userId = RESERVATION_USER_ID;

            given(chatMessageRepository.findTop1ByReservationIdOrderByCreatedAtDesc(RESERVATION_ID)).willReturn(Optional.empty());
            given(reservationService.getReservationWithStore(RESERVATION_ID)).willReturn(reservation);

            // when
            ChatAvailabilityResponse response = chatMessageService.isChatAvailable(RESERVATION_ID, userId);

            // then
            assertThat(response.isAvailable()).isTrue();
            assertThat(response.getReason()).isEqualTo(reservation.getStatus().name());
        }
    }

    @Nested
    class 채팅_메시지_목록_조회 {

        @Test
        void 채팅_메시지_목록_조회_성공() {
            // given
            Long userId = OWNER_ID;
            PageRequest pageable = PageRequest.of(0, 10);

            ChatMessage chatMessage = buildChatMessage("Hi", RESERVATION_ID, userId, OWNER_ID, RESERVATION_USER_ID);
            Page<ChatMessage> page = new PageImpl<>(List.of(chatMessage));

            given(chatMessageRepository.findTop1ByReservationIdOrderByCreatedAtDesc(RESERVATION_ID)).willReturn(Optional.of(chatMessage));
            given(chatMessageRepository.findByReservationId(RESERVATION_ID, pageable)).willReturn(page);

            // when
            Page<ChatMessageResponse> responses = chatMessageService.getMessages(RESERVATION_ID, userId, pageable);

            // then
            assertAll(
                    () -> assertThat(responses.getContent()).hasSize(1),
                    () -> assertEquals(responses.getContent().get(0).getContent(), chatMessage.getContent())
            );
        }
    }

    @Nested
    class 읽음상태_변경 {

        @Test
        void 채팅_참여자가_아니면_예외처리() {
            // given
            Long invalidUserId = 99L;
            Long correctUserId = OWNER_ID;

            ChatMessage chatMessage = buildChatMessage("Hi", RESERVATION_ID, correctUserId, OWNER_ID, RESERVATION_USER_ID);

            given(chatMessageRepository.findTop1ByReservationIdOrderByCreatedAtDesc(RESERVATION_ID)).willReturn(Optional.of(chatMessage));

            // when & then
            assertThatThrownBy(() -> chatMessageService.changeReadStatus(RESERVATION_ID, invalidUserId))
                    .isInstanceOf(HandledException.class)
                    .hasMessageContaining(ErrorCode.INVALID_CHAT_PARTICIPANT.getDefaultMessage());
        }

        @Test
        void 읽음상태_변경_성공() {
            // given
            Long userId = OWNER_ID;

            ChatMessage chatMessage = buildChatMessage("Hi", RESERVATION_ID, userId, OWNER_ID, RESERVATION_USER_ID);

            given(chatMessageRepository.findTop1ByReservationIdOrderByCreatedAtDesc(RESERVATION_ID)).willReturn(Optional.of(chatMessage));
            given(chatMessageRepository.updateUnreadMessagesAsRead(RESERVATION_ID, userId)).willReturn(5);

            // when
            ChatReadStatusResponse response = chatMessageService.changeReadStatus(RESERVATION_ID, userId);

            // then
            assertThat(response.getMessage()).isEqualTo("5개의 메시지 읽음 처리가 완료되었습니다.");
        }
    }

    private Reservation mockReservation(Long ownerId, Long userId) {
        User owner = User.builder().id(ownerId).build();
        User user = User.builder().id(userId).build();
        Store store = Store.builder().user(owner).build();
        return Reservation.builder()
                .store(store)
                .user(user)
                .reservedAt(LocalDateTime.now())
                .build();
    }

    private ChatMessage buildChatMessage(ChatMessageRequest request, Long senderId, Reservation reservation) {
        return ChatMessage.builder()
                .reservationId(request.getReservationId())
                .sender(User.builder().id(senderId).build())
                .ownerId(reservation.getStore().getUser().getId())
                .reservationUserId(reservation.getUser().getId())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }

    private ChatMessage buildChatMessage(String content, Long reservationId, Long senderId, Long ownerId, Long reservationUserId) {
        return ChatMessage.builder()
                .reservationId(reservationId)
                .sender(User.builder().id(senderId).build())
                .ownerId(ownerId)
                .reservationUserId(reservationUserId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
    }
}