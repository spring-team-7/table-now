package org.example.tablenow.domain.waitlist.repository;

import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    // 한 유저가 동일한 가게 중복 등록 방지
    boolean existsByUserAndStoreAndIsNotifiedFalse(User user, Store store);

    // 해당 가게에서 대기 인원 수 조회 (알림 미수신만)
    long countByStoreAndIsNotifiedFalse(Store store);

    List<Waitlist> user(User user);

    // 특정 유저의 대기 목록 중에서, 아직 알림을 받지 않은 모든 대기 목록
    List<Waitlist> findAllByUserAndIsNotifiedFalse(User findUser);

    // 빈자리 알림 보낼때 사용
    Optional<Waitlist> findByUserAndStoreAndIsNotifiedFalse(User user, Store store);

    // 특정 가게 대기자 목록 조회
    List<Waitlist> findAllByStoreAndIsNotifiedFalse(Store store);

}