package org.example.tablenow.domain.waitlist.service;

import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.repository.StoreRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.domain.waitlist.dto.request.WaitlistRequestDto;
import org.example.tablenow.domain.waitlist.dto.response.WaitlistResponseDto;
import org.example.tablenow.domain.waitlist.entity.Waitlist;
import org.example.tablenow.domain.waitlist.repository.WaitlistRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

  @InjectMocks
  private WaitlistService waitlistService;

  @Mock
  private WaitlistRepository waitlistRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private StoreRepository storeRepository;

  @Mock
  private User user;

  @Mock
  private Store store;

  @Test
  void 대기등록_성공() {
    WaitlistRequestDto requestDto = new WaitlistRequestDto();
    ReflectionTestUtils.setField(requestDto, "storeId", 10L);

    when(store.getId()).thenReturn(10L);
    when(store.getName()).thenReturn("테스트 가게");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
    when(waitlistRepository.existsByUserAndStoreAndIsNotifiedFalse(user, store)).thenReturn(false);
    when(waitlistRepository.countByStoreAndIsNotifiedFalse(store)).thenReturn(3L);

    when(waitlistRepository.save(any(Waitlist.class)))
        .thenAnswer(invocation -> {
          Waitlist saved = invocation.getArgument(0);
          ReflectionTestUtils.setField(saved, "id", 1L);
          ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0));
          return saved;
        });

    WaitlistResponseDto result = waitlistService.registerWaitlist(1L, requestDto);

    assertEquals(Long.valueOf(1L), result.getWaitlistId());
    assertEquals(Long.valueOf(10L), result.getStoreId());
  }

  @Test
  void 유저를_찾지_못해_대기등록_실패() {
    WaitlistRequestDto requestDto = new WaitlistRequestDto();
    ReflectionTestUtils.setField(requestDto, "storeId", 10L);

    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    HandledException exception = assertThrows(HandledException.class, () ->
        waitlistService.registerWaitlist(1L, requestDto)
    );

    assertEquals(ErrorCode.USER_NOT_FOUND.getStatus(), exception.getHttpStatus());
  }

  @Test
  void 가게를_찾지_못해_대기등록_실패() {

    WaitlistRequestDto requestDto = new WaitlistRequestDto();
    ReflectionTestUtils.setField(requestDto, "storeId", 10L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(storeRepository.findById(10L)).thenReturn(Optional.empty());

    HandledException exception = assertThrows(HandledException.class, () ->
        waitlistService.registerWaitlist(1L, requestDto)
    );
    assertEquals(ErrorCode.STORE_NOT_FOUND.getStatus(), exception.getHttpStatus());
  }

  @Test
  void 이미_대기중이라_중복대기로_실패() {
    WaitlistRequestDto requestDto = new WaitlistRequestDto();
    ReflectionTestUtils.setField(requestDto, "storeId", 10L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

    when(waitlistRepository.existsByUserAndStoreAndIsNotifiedFalse(user, store)).thenReturn(true);

    HandledException exception = assertThrows(HandledException.class, () ->
        waitlistService.registerWaitlist(1L, requestDto)
    );

    assertEquals(ErrorCode.WAITLIST_ALREADY_REGISTERED.getStatus(), exception.getHttpStatus());
  }

  @Test
  void 대기인원_초과로_실패() {
    WaitlistRequestDto requestDto = new WaitlistRequestDto();
    ReflectionTestUtils.setField(requestDto, "storeId", 10L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(storeRepository.findById(10L)).thenReturn(Optional.of(store));

    when(waitlistRepository.existsByUserAndStoreAndIsNotifiedFalse(user, store)).thenReturn(false);

    when(waitlistRepository.countByStoreAndIsNotifiedFalse(store)).thenReturn(100L);

    HandledException exception = assertThrows(HandledException.class, () ->
        waitlistService.registerWaitlist(1L, requestDto)
    );

    assertEquals(ErrorCode.WAITLIST_FULL.getStatus(), exception.getHttpStatus());
  }
}