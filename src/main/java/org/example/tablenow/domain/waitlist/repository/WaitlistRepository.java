package org.example.tablenow.domain.waitlist.repository;

import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    // 한 유저가 동일한 가게 중복 등록 방지
    boolean existsByUserAndStoreAndIsNotifiedFalse(User user, Store store);

    // 해당 가게에서 대기 인원 수 조회 (알림 미수신만)
    long countByStoreAndWaitDateAndIsNotifiedFalse(Store store, LocalDate waitDate);

    List<Waitlist> user(User user);

    // 특정 유저의 대기 목록 중에서, 아직 알림을 받지 않은 모든 대기 목록
    List<Waitlist> findAllByUserAndIsNotifiedFalse(User findUser);

    // 빈자리 알림 보낼때 사용
    Optional<Waitlist> findByUserAndStoreAndIsNotifiedFalse(User user, Store store);

    //알림 받지 않은 대기자의 대기 날짜 목록 조회(중복 제거)
    @Query("SELECT DISTINCT w.waitDate FROM Waitlist w WHERE w.isNotified = false")
    List<LocalDate> findDistinctWaitDates();

    // 특정 가게의 해당 날짜의 대기자 목록 조회
    @Query("""
            SELECT w FROM Waitlist w
            JOIN FETCH w.user
            WHERE w.store = :store
              AND w.waitDate = :waitDate
              AND w.isNotified = false
        """)
    List<Waitlist> findAllWithUserByStoreAndWaitDateAndIsNotifiedFalse(@Param("store") Store store,
                                                                       @Param("waitDate") LocalDate waitDate);
}