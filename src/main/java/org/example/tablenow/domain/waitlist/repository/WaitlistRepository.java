package org.example.tablenow.domain.waitlist.repository;

import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
  // 아직 알림을 받지 않은 대기 등록 존재 유무 판별
  boolean existsByUserAndStoreAndIsNotifiedFalse(User user, Store store);
  // 대기 인원 체크용(알림 안받은 사람만)
  long countByStoreAndIsNotifiedFalse(Store store);
}