package org.example.tablenow.domain.event;

import org.example.tablenow.domain.event.dto.request.EventRequestDto;
import org.example.tablenow.domain.event.dto.response.EventJoinResponseDto;
import org.example.tablenow.domain.event.entity.Event;
import org.example.tablenow.domain.event.enums.EventStatus;
import org.example.tablenow.domain.event.repository.EventJoinRepository;
import org.example.tablenow.domain.event.repository.EventRepository;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EventJoinExecutorTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventJoinRepository eventJoinRepository;

    @InjectMocks
    private EventJoinExecutor eventJoinExecutor;

    Long eventId = 1L;
    Long userId = 1L;
    String userIdStr = String.valueOf(userId);
    String zsetKey = "event:join:" + eventId;

    AuthUser authUser = new AuthUser(userId, "test@test.com", UserRole.ROLE_USER, "일반회원");
    User user = User.fromAuthUser(authUser);

    Store store;
    Event event;

    @BeforeEach
    void setUp() {
        store = Store.builder()
                .id(100L)
                .capacity(10)
                .build();

        EventRequestDto dto = EventRequestDto.builder()
                .storeId(store.getId())
                .openAt(LocalDateTime.now().minusDays(1))
                .eventTime(LocalDateTime.now().plusDays(1))
                .limitPeople(5)
                .build();

        event = Event.create(store, dto);
        ReflectionTestUtils.setField(event, "id", eventId);
        ReflectionTestUtils.setField(event, "status", EventStatus.OPENED);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Nested
    class 이벤트_참여 {

        @Test
        void 이벤트가_존재하지_않으면_예외() {
            // given
            given(eventRepository.findById(eventId)).willReturn(Optional.empty());

            // when & then
            HandledException ex = assertThrows(HandledException.class, () ->
                    eventJoinExecutor.execute(eventId, authUser)
            );

            assertEquals(ErrorCode.EVENT_NOT_FOUND.getDefaultMessage(), ex.getMessage());
        }

        @Test
        void 이벤트가_오픈되지_않았으면_예외() {
            // given
            ReflectionTestUtils.setField(event, "status", EventStatus.READY);
            given(eventRepository.findById(eventId)).willReturn(Optional.of(event));

            // when & then
            HandledException ex = assertThrows(HandledException.class, () ->
                    eventJoinExecutor.execute(eventId, authUser)
            );

            assertEquals(ErrorCode.EVENT_NOT_OPENED.getDefaultMessage(), ex.getMessage());
        }

        @Test
        void Redis_ZADD_실패시_예외() {
            // given
            given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
            given(zSetOperations.add(anyString(), anyString(), anyDouble())).willReturn(false);

            // when & then
            HandledException ex = assertThrows(HandledException.class, () ->
                    eventJoinExecutor.execute(eventId, authUser)
            );

            assertEquals(ErrorCode.EVENT_ALREADY_JOINED.getDefaultMessage(), ex.getMessage());
        }

        @Test
        void 정원_초과시_자리삭제_후_예외() {
            // given
            given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
            given(zSetOperations.add(anyString(), anyString(), anyDouble())).willReturn(true);
            given(zSetOperations.rank(zsetKey, userIdStr)).willReturn(10L);

            // when & then
            HandledException ex = assertThrows(HandledException.class, () ->
                    eventJoinExecutor.execute(eventId, authUser)
            );

            verify(zSetOperations).remove(zsetKey, userIdStr);
            assertEquals(ErrorCode.EVENT_FULL.getDefaultMessage(), ex.getMessage());
        }

        @Test
        void DB_저장_실패시_자리삭제_및_예외_전파() {
            // given
            given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
            given(zSetOperations.add(anyString(), anyString(), anyDouble())).willReturn(true);
            given(zSetOperations.rank(zsetKey, userIdStr)).willReturn(1L);
            given(eventJoinRepository.save(any())).willThrow(new RuntimeException("DB 오류"));

            // when & then
            assertThrows(RuntimeException.class, () ->
                    eventJoinExecutor.execute(eventId, authUser)
            );

            verify(redisTemplate.opsForZSet()).remove(zsetKey, userIdStr);
        }

        @Test
        void 성공_참여시_응답_정상반환() {
            // given
            given(eventRepository.findById(eventId)).willReturn(Optional.of(event));
            given(redisTemplate.opsForZSet().add(anyString(), anyString(), anyDouble())).willReturn(true);
            given(redisTemplate.opsForZSet().rank(zsetKey, userIdStr)).willReturn(1L);
            given(eventJoinRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            // when
            EventJoinResponseDto response = eventJoinExecutor.execute(eventId, authUser);

            // then
            assertNotNull(response);
            assertEquals(eventId, response.getEventId());
        }
    }
}
