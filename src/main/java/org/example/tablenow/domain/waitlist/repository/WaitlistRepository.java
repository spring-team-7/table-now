package org.example.tablenow.domain.waitlist.repository;

import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
  // 한 유저가 동일한 가게 중복 등록 방지
  boolean existsByUserAndStoreAndIsNotifiedFalse(User user, Store store);
  // 해당 가게에서 대기 인원 수 조회 (알림 미수신만)
  long countByStoreAndIsNotifiedFalse(Store store);
}