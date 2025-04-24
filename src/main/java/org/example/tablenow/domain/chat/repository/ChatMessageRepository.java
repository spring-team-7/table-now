package org.example.tablenow.domain.chat.repository;

import org.example.tablenow.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // reservationId 기준 가장 최근 메시지 1개 조회
    Optional<ChatMessage> findTop1ByReservationIdOrderByCreatedAtDesc(Long reservationId);

    Page<ChatMessage> findByReservationId(Long reservationId, Pageable pageable);
}
