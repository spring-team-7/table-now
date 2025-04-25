package org.example.tablenow.domain.chat.repository;

import org.example.tablenow.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // reservationId 기준 가장 최근 메시지 1개 조회
    Optional<ChatMessage> findTop1ByReservationIdOrderByCreatedAtDesc(Long reservationId);

    Page<ChatMessage> findByReservationId(Long reservationId, Pageable pageable);

    /**
     * reservationId 채팅에 대해 보낸 사람이 나(userId)가 아니며, 아직 읽지 않은 메시지들을 조회
     * 미사용메서드 - bulk update 방식(updateUnreadMessagesAsRead)으로 변경
     * 성능 비교 테스트를 위해 유지
     */
    List<ChatMessage> findByReservationIdAndSenderIdNotAndIsReadFalse(Long reservationId, Long senderId);

    // 채팅방 내에서 내가 읽지 않은 메시지들을 bulk update로 읽음 처리
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE ChatMessage m 
        SET m.isRead = true 
        WHERE m.reservationId = :reservationId 
        AND m.sender.id != :userId 
        AND m.isRead = false
        """)
    int updateUnreadMessagesAsRead(@Param("reservationId") Long reservationId, @Param("userId") Long userId);
}
