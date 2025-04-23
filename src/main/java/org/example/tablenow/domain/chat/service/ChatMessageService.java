package org.example.tablenow.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.chat.entity.ChatMessage;
import org.example.tablenow.domain.chat.repository.ChatMessageRepository;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public ChatMessage saveMessage(Long reservationId, Long senderId, String content, String imageUrl) {

        // 1. 기존 메시지가 존재하는지 확인
        return chatMessageRepository.findTop1ByReservationIdOrderByCreatedAtDesc(reservationId)
                .map(lastMessage -> {
                    Long ownerId = lastMessage.getOwnerId();
                    Long reservationUserId = lastMessage.getReservationUserId();
                    validateSender(senderId, ownerId, reservationUserId);
                    return chatMessageRepository.save(buildChatMessage(reservationId, senderId, ownerId, reservationUserId, content, imageUrl));
                })
                .orElseGet(() -> {
                    // 최초 메시지: reservation 기준으로 권한 확인 및 역정규화 값 추출
                    Reservation reservation = reservationService.getReservation(reservationId);
                    Long ownerId = reservation.getStore().getUser().getId();
                    Long reservationUserId = reservation.getUser().getId();
                    validateSender(senderId, ownerId, reservationUserId);
                    return chatMessageRepository.save(buildChatMessage(reservationId, senderId, ownerId, reservationUserId, content, imageUrl));
                });
    }

    private void validateSender(Long senderId, Long ownerId, Long reservationUserId) {
        if (!senderId.equals(ownerId) && !senderId.equals(reservationUserId)) {
            throw new HandledException(ErrorCode.UNAUTHORIZED_CHAT_SENDER);
        }
    }

    private ChatMessage buildChatMessage(Long reservationId, Long senderId, Long ownerId, Long reservationUserId,
                                         String content, String imageUrl) {
        User sender = userRepository.getReferenceById(senderId);
        return ChatMessage.builder()
                .reservationId(reservationId)
                .sender(sender)
                .ownerId(ownerId)
                .reservationUserId(reservationUserId)
                .content(content)
                .imageUrl(imageUrl)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
