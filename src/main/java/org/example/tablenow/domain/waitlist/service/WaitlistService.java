package org.example.tablenow.domain.waitlist.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.domain.waitlist.dto.request.WaitlistRequestDto;
import org.example.tablenow.domain.waitlist.dto.response.WaitlistFindResponseDto;
import org.example.tablenow.domain.waitlist.dto.response.WaitlistResponseDto;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.example.tablenow.domain.waitlist.repository.WaitlistRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WaitlistService {
    private final WaitlistRepository waitlistRepository;
    private final UserRepository userRepository;
    private final StoreService storeService;
    private final ReservationService reservationService;
    private final RedissonClient redissonClient;

    private static final int MAX_WAITING = 100;
    private static final int LOCK_WAIT_TIME = 2;
    private static final int LOCK_LEASE_TIME = 1;

    @Transactional
    public WaitlistResponseDto registerWaitlist(Long userId, WaitlistRequestDto requestDto) {
        User findUser = userRepository.findById(userId)
            .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
        Store findStore = storeService.getStore(requestDto.getStoreId());

        // 빈자리 있는 경우 대기 등록 안됨
        validateNoVacancy(findStore, requestDto.getWaitDate());

        // 해당 가게에 유저가 이미 대기 중인지 확인
        if (waitlistRepository.existsByUserAndStoreAndIsNotifiedFalse(findUser, findStore)) {
            throw new HandledException(ErrorCode.WAITLIST_ALREADY_REGISTERED);
        }

        // 대기 등록 인원 제한 (100명)
        long waitingCount = waitlistRepository.countByStoreAndWaitDateAndIsNotifiedFalse(findStore, requestDto.getWaitDate());
        if (waitingCount >= MAX_WAITING) {
            throw new HandledException(ErrorCode.WAITLIST_FULL);
        }

        Waitlist waitlist = new Waitlist(findUser, findStore, requestDto.getWaitDate());
        Waitlist savedWaitlist = waitlistRepository.save(waitlist);

        return WaitlistResponseDto.fromWaitlist(savedWaitlist);
    }

    // 빈자리 대기 등록 - Redisson 분산 락 적용
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WaitlistResponseDto registerLockWaitlist(Long userId, WaitlistRequestDto requestDto) {
        // 가게 ID별,날짜 별로 락 설정
        String lockKey = String.format("lock:store:%d:date:%s",
            requestDto.getStoreId(),
            requestDto.getWaitDate().format(DateTimeFormatter.ISO_DATE)
        );
        RLock lock = redissonClient.getLock(lockKey);

        boolean available = false;
        try {
            available = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!available) {
                throw new HandledException(ErrorCode.WAITLIST_REQUEST_TIMEOUT);
            }

            User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
            Store findStore = storeService.getStore(requestDto.getStoreId());

            // 빈자리 있는 경우 대기 등록 안됨
            validateNoVacancy(findStore, requestDto.getWaitDate());

            // 해당 가게에 유저가 이미 대기 중인지 확인
            if (waitlistRepository.existsByUserAndStoreAndIsNotifiedFalse(findUser, findStore)) {
                throw new HandledException(ErrorCode.WAITLIST_ALREADY_REGISTERED);
            }

            // 대기 등록 인원 제한(100명)
            long waitingCount = waitlistRepository.countByStoreAndWaitDateAndIsNotifiedFalse(findStore, requestDto.getWaitDate());
            if (waitingCount >= MAX_WAITING) {
                throw new HandledException(ErrorCode.WAITLIST_FULL);
            }

            Waitlist waitlist = new Waitlist(findUser, findStore, requestDto.getWaitDate());
            Waitlist savedWaitlist = waitlistRepository.save(waitlist);

            return WaitlistResponseDto.fromWaitlist(savedWaitlist);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HandledException(ErrorCode.WAITLIST_REQUEST_INTERRUPTED);
        } finally {
            if (available && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 내 대기 목록 조회
    @Transactional(readOnly = true)
    public List<WaitlistFindResponseDto> findMyWaitlist(Long userId) {
        User findUser = userRepository.findById(userId)
            .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));

        List<Waitlist> waitlists = waitlistRepository.findAllByUserAndIsNotifiedFalse(findUser);
        return waitlists.stream()
            .map(WaitlistFindResponseDto::fromWaitlist)
            .toList();
    }

    private void validateNoVacancy(Store store, LocalDate waitDate) {
        if (reservationService.hasVacancyDate(store, waitDate)) {
            throw new HandledException(ErrorCode.WAITLIST_NOT_ALLOWED);
        }
    }

}
