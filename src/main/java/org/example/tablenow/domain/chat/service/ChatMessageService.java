package org.example.tablenow.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.chat.dto.request.ChatMessageRequest;
import org.example.tablenow.domain.chat.dto.response.ChatAvailabilityResponse;
import org.example.tablenow.domain.chat.dto.response.ChatMessageResponse;
import org.example.tablenow.domain.chat.entity.ChatMessage;
import org.example.tablenow.domain.chat.repository.ChatMessageRepository;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.entity.ReservationStatus;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.service.UserService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ReservationService reservationService;
    private final UserService userService;

    // 참여자 정보 전달용 내부 record
    private record ChatParticipants(Long ownerId, Long reservationUserId) {}

    @Transactional
    public ChatMessage saveMessage(ChatMessageRequest request, Long senderId) {
        ChatParticipants participants = loadChatParticipants(request.getReservationId());
        validateChatParticipant(senderId, participants);

        return chatMessageRepository.save(buildChatMessage(
                request,
                senderId,
                participants.ownerId(),
                participants.reservationUserId()
        ));
    }

    @Transactional(readOnly = true)
    public ChatAvailabilityResponse isChatAvailable(Long reservationId, Long userId) {
        ChatParticipants participants = loadChatParticipants(reservationId);
        validateChatParticipant(userId, participants);

        Reservation reservation = reservationService.getReservation(reservationId);
        boolean isAvailable = reservation.getStatus() == ReservationStatus.RESERVED;

        return ChatAvailabilityResponse.builder()
                .reservationId(reservationId)
                .userId(userId)
                .available(isAvailable)
                .reason(reservation.getStatus().name())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(Long reservationId, Long userId, Pageable pageable) {
        ChatParticipants participants = loadChatParticipants(reservationId);
        validateChatParticipant(userId, participants);

        Page<ChatMessage> messages = chatMessageRepository.findByReservationId(reservationId, pageable);

        return messages.map(ChatMessageResponse::fromChatMessage);
    }

    private ChatParticipants loadChatParticipants(Long reservationId) {
        Optional<ChatMessage> lastMessageOpt =
                chatMessageRepository.findTop1ByReservationIdOrderByCreatedAtDesc(reservationId);

        if (lastMessageOpt.isPresent()) {
            // 채팅한 내역이 있을 경우 chatMessage 정보로 참여자 정보 추출
            ChatMessage lastMessage = lastMessageOpt.get();
            return new ChatParticipants(
                    lastMessage.getOwnerId(),
                    lastMessage.getReservationUserId()
            );
        } else {
            // 최초 채팅일 경우 reservation 정보로 참여자 정보 추출
            Reservation reservation = reservationService.getReservation(reservationId);
            return new ChatParticipants(
                    reservation.getStore().getUser().getId(),
                    reservation.getUser().getId()
            );
        }
    }

    private void validateChatParticipant(Long userId, ChatParticipants participants) {
        if (!userId.equals(participants.ownerId()) && !userId.equals(participants.reservationUserId())) {
            throw new HandledException(ErrorCode.INVALID_CHAT_PARTICIPANT);
        }
    }

    private ChatMessage buildChatMessage(ChatMessageRequest request, Long senderId, Long ownerId, Long reservationUserId) {
        User sender = userService.getUser(senderId);
        return ChatMessage.builder()
                .reservationId(request.getReservationId())
                .sender(sender)
                .ownerId(ownerId)
                .reservationUserId(reservationUserId)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}