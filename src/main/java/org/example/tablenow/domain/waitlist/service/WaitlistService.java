package org.example.tablenow.domain.waitlist.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.service.StoreService;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.domain.waitlist.dto.request.WaitlistRequestDto;
import org.example.tablenow.domain.waitlist.dto.response.WaitlistResponseDto;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.example.tablenow.domain.waitlist.repository.WaitlistRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitlistService {
  private final WaitlistRepository waitlistRepository;
  private final UserRepository userRepository;
  private final StoreService storeService;

  private static final int MAX_WAITING = 100;

  @Transactional
  public WaitlistResponseDto registerWaitlist(Long userId, WaitlistRequestDto requestDto) {
    User findUser = userRepository.findById(userId)
        .orElseThrow(() -> new HandledException(ErrorCode.USER_NOT_FOUND));
    Store findStore = storeService.getStore(requestDto.getStoreId());

    // 해당 가게에 유저가 이미 대기 중인지 확인
    if(waitlistRepository.existsByUserAndStoreAndIsNotifiedFalse(findUser,findStore)){
      throw new HandledException(ErrorCode.WAITLIST_ALREADY_REGISTERED);
    }

    // 대기 등록 인원 제한(100명)
    long waitingCount = waitlistRepository.countByStoreAndIsNotifiedFalse(findStore);
    if(waitingCount >= MAX_WAITING){
      throw new HandledException(ErrorCode.WAITLIST_FULL);
    }

    Waitlist waitlist = new Waitlist(findUser, findStore);
    waitlistRepository.save(waitlist);

    return WaitlistResponseDto.fromWaitlist(waitlist);
  }
}
