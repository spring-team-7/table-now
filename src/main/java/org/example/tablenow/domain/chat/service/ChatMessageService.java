package org.example.tablenow.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.chat.dto.request.ChatMessageRequest;
import org.example.tablenow.domain.chat.entity.ChatMessage;
import org.example.tablenow.domain.chat.repository.ChatMessageRepository;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.service.UserService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ReservationService reservationService;
    private final UserService userService;

    @Transactional
    public ChatMessage saveMessage(ChatMessageRequest request, Long senderId) {

        // 1. 기존 메시지가 존재하는지 확인
        return chatMessageRepository.findTop1ByReservationIdOrderByCreatedAtDesc(request.getReservationId())
                .map(lastMessage -> {
                    Long ownerId = lastMessage.getOwnerId();
                    Long reservationUserId = lastMessage.getReservationUserId();
                    validateSender(senderId, ownerId, reservationUserId);
                    return chatMessageRepository.save(buildChatMessage(request, senderId, ownerId, reservationUserId));
                })
                .orElseGet(() -> {
                    // 최초 메시지: reservation 기준으로 권한 확인 및 역정규화 값 추출
                    Reservation reservation = reservationService.getReservation(request.getReservationId());
                    Long ownerId = reservation.getStore().getUser().getId();
                    Long reservationUserId = reservation.getUser().getId();
                    validateSender(senderId, ownerId, reservationUserId);
                    return chatMessageRepository.save(buildChatMessage(request, senderId, ownerId, reservationUserId));
                });
    }

    private void validateSender(Long senderId, Long ownerId, Long reservationUserId) {
        if (!senderId.equals(ownerId) && !senderId.equals(reservationUserId)) {
            throw new HandledException(ErrorCode.UNAUTHORIZED_CHAT_SENDER);
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
