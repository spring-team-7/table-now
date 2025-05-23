package org.example.tablenow.domain.store.service;

import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.category.service.CategoryService;
import org.example.tablenow.domain.image.service.ImageService;
import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.example.tablenow.domain.store.dto.request.StoreUpdateRequestDto;
import org.example.tablenow.domain.store.dto.response.*;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.message.producer.StoreProducer;
import org.example.tablenow.domain.store.repository.StoreRepository;
import org.example.tablenow.domain.store.util.StoreConstant;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ImageService imageService;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ZSetOperations<String, String> zSetOperations;
    @Mock
    private StoreProducer storeProducer;
    @InjectMocks
    private StoreService storeService;

    private final Long STORE_ID = 1L;
    private final Long USER_ID = 1L;
    private final Long OWNER_ID = 2L;
    private final Long CATEGORY_ID = 1L;

    @Nested
    class 가게_등록 {
        StoreCreateRequestDto dto = new StoreCreateRequestDto();

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(dto, "name", "맛있는 가게");
            ReflectionTestUtils.setField(dto, "description", "가게 설명입니다.");
            ReflectionTestUtils.setField(dto, "address", "서울특별시 강남구 테헤란로11길 1 1층");
            ReflectionTestUtils.setField(dto, "capacity", 100);
            ReflectionTestUtils.setField(dto, "startTime", LocalTime.of(9, 00));
            ReflectionTestUtils.setField(dto, "endTime", LocalTime.of(21, 00));
            ReflectionTestUtils.setField(dto, "deposit", 10000);
            ReflectionTestUtils.setField(dto, "categoryId", CATEGORY_ID);
        }

        @Test
        void 존재하지_않는_카테고리_조회_시_예외_발생() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            given(categoryService.findCategory(anyLong()))
                    .willThrow(new HandledException(ErrorCode.CATEGORY_NOT_FOUND));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.createStore(authOwner, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.CATEGORY_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 가게_시작_시간이_종료_시간_이후인_경우_예외_발생() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");
            Category category = Category.builder().id(CATEGORY_ID).name("한식").build();

            given(categoryService.findCategory(anyLong())).willReturn(category);
            ReflectionTestUtils.setField(dto, "startTime", LocalTime.of(22, 00));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.createStore(authOwner, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.STORE_BAD_REQUEST_TIME.getDefaultMessage());
        }

        @Test
        void 가게_최대_등록_수를_초과하는_경우_예외_발생() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");
            Category category = Category.builder().id(CATEGORY_ID).name("한식").build();

            given(categoryService.findCategory(anyLong())).willReturn(category);
            given(storeRepository.countActiveStoresByUser(anyLong())).willReturn(99L);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.createStore(authOwner, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.STORE_EXCEED_MAX.getDefaultMessage());
        }

        @Test
        void 등록_성공_시_MQ_메시지_발송() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");
            Category category = Category.builder().id(CATEGORY_ID).name("한식").build();

            given(categoryService.findCategory(anyLong())).willReturn(category);
            given(storeRepository.countActiveStoresByUser(anyLong())).willReturn(0L);
            given(storeRepository.save(any(Store.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            StoreCreateResponseDto response = storeService.createStore(authOwner, dto);

            // then
            assertNotNull(response);
            assertEquals(response.getName(), dto.getName());
            assertEquals(response.getUserId(), authOwner.getId());
            assertEquals(response.getCategoryId(), category.getId());
            verify(storeProducer, times(1)).publishStoreCreate(any(Store.class));
        }
    }

    @Nested
    class 내_가게_목록_조회 {

        @Test
        void 조회_성공() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            Long storeId1 = 1L;
            Store store1 = mockStore(storeId1, OWNER_ID, CATEGORY_ID);

            Long storeId2 = 2L;
            Store store2 = mockStore(storeId2, OWNER_ID, CATEGORY_ID);

            List<Store> stores = List.of(store1, store2);
            List<StoreResponseDto> findResult = stores.stream().map(StoreResponseDto::fromStore).toList();
            given(storeRepository.findAllByUserId(anyLong())).willReturn(findResult);

            // when
            List<StoreResponseDto> response = storeService.findMyStores(authOwner);
            StoreResponseDto firstResult = response.get(0);

            // then
            assertNotNull(response);
            assertEquals(response.size(), 2);
            assertAll(
                    () -> assertEquals(firstResult.getStoreId(), storeId1),
                    () -> assertEquals(firstResult.getUserId(), OWNER_ID),
                    () -> assertEquals(firstResult.getCategoryId(), CATEGORY_ID)
            );
        }
    }

    @Nested
    class 가게_수정 {
        StoreUpdateRequestDto dto = new StoreUpdateRequestDto();

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(dto, "name", "더 맛있는 가게");
            ReflectionTestUtils.setField(dto, "description", "더 맛있는 가게 설명입니다.");
            ReflectionTestUtils.setField(dto, "address", "서울특별시 강남구 테헤란로22길 2 2층");
            ReflectionTestUtils.setField(dto, "capacity", 200);
            ReflectionTestUtils.setField(dto, "startTime", LocalTime.of(8, 00));
            ReflectionTestUtils.setField(dto, "endTime", LocalTime.of(22, 00));
            ReflectionTestUtils.setField(dto, "deposit", 20000);
            ReflectionTestUtils.setField(dto, "categoryId", 2L);
        }

        @Test
        void 존재하지_않는_가게_조회_시_예외_발생() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.updateStore(STORE_ID, authOwner, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.STORE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 가게_주인이_아닌_경우_예외_발생() {
            // given
            AuthUser authUser = new AuthUser(USER_ID, "user@a.com", UserRole.ROLE_USER, "일반회원");
            User user = User.fromAuthUser(authUser);

            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            ReflectionTestUtils.setField(store, "user", user);
            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.updateStore(STORE_ID, authOwner, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.STORE_FORBIDDEN.getDefaultMessage());
        }

        @Test
        void 존재하지_않는_카테고리_조회_시_예외_발생() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));
            given(categoryService.findCategory(anyLong()))
                    .willThrow(new HandledException(ErrorCode.CATEGORY_NOT_FOUND));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.updateStore(STORE_ID, authOwner, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.CATEGORY_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 가게_시작_시간이_종료_시간_이후인_경우_예외_발생() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            ReflectionTestUtils.setField(dto, "endTime", LocalTime.of(7, 30));
            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.updateStore(STORE_ID, authOwner, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.STORE_BAD_REQUEST_TIME.getDefaultMessage());
        }

        @Nested
        class 가게_이미지_수정 {

            @Test
            void 기존_이미지와_요청_이미지가_없는_경우_삭제_없이_수정_성공() {
                // given
                AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

                Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

                given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));
                given(categoryService.findCategory(anyLong())).willReturn(Category.builder().id(1L).name("한식").build());

                // when
                StoreUpdateResponseDto response = storeService.updateStore(STORE_ID, authOwner, dto);

                // then
                assertNotNull(response);
                assertEquals(response.getImageUrl(), dto.getImageUrl());
                verify(imageService, never()).delete(anyString());
            }

            @Test
            void 기존_이미지가_없으면_삭제_없이_수정_성공() {
                // given
                AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

                Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

                String request = "https://url.com/store/1/update.jpg";
                ReflectionTestUtils.setField(dto, "imageUrl", request);

                given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));
                given(categoryService.findCategory(anyLong())).willReturn(Category.builder().id(1L).name("한식").build());

                // when
                StoreUpdateResponseDto response = storeService.updateStore(STORE_ID, authOwner, dto);

                // then
                assertNotNull(response);
                assertEquals(response.getImageUrl(), dto.getImageUrl());
                verify(imageService, never()).delete(anyString());
            }

            @Test
            void 요청_이미지가_없으면_삭제_없이_수정_성공() {
                // given
                AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

                Category category = Category.builder().id(2L).name("중식").build();

                Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

                String origin = "https://url.com/store/1/origin.jpg";
                ReflectionTestUtils.setField(store, "imageUrl", origin);
                given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));
                given(categoryService.findCategory(anyLong())).willReturn(category);

                // when
                StoreUpdateResponseDto response = storeService.updateStore(STORE_ID, authOwner, dto);

                // then
                assertNotNull(response);
                assertEquals(response.getImageUrl(), store.getImageUrl());
                verify(imageService, never()).delete(anyString());
            }

            @Test
            void 동일한_이미지_요청_시_삭제_없이_수정_성공() {
                // given
                AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

                Category category = Category.builder().id(CATEGORY_ID).name("한식").build();

                Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

                String origin = "https://url.com/store/1/origin.jpg";
                ReflectionTestUtils.setField(store, "imageUrl", origin);
                ReflectionTestUtils.setField(dto, "imageUrl", origin);

                given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));
                given(categoryService.findCategory(anyLong())).willReturn(category);

                // when
                StoreUpdateResponseDto response = storeService.updateStore(STORE_ID, authOwner, dto);

                // then
                assertNotNull(response);
                assertEquals(response.getImageUrl(), dto.getImageUrl());
                verify(imageService, never()).delete(anyString());
            }

            @Test
            void 기존_이미지_변경_시_이미지_삭제_및_수정_성공() {
                // given
                AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

                Category category = Category.builder().id(CATEGORY_ID).name("한식").build();

                Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

                String origin = "https://url.com/store/1/origin.jpg";
                String request = "https://url.com/store/1/update.jpg";
                ReflectionTestUtils.setField(store, "imageUrl", origin);
                ReflectionTestUtils.setField(dto, "imageUrl", request);

                given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));
                given(categoryService.findCategory(anyLong())).willReturn(category);

                // when
                StoreUpdateResponseDto response = storeService.updateStore(STORE_ID, authOwner, dto);

                // then
                assertNotNull(response);
                assertEquals(response.getImageUrl(), dto.getImageUrl());
                verify(imageService).delete(anyString());
            }
        }

        @Test
        void 요청_데이터가_비어있을_경우_수정_성공() {
            StoreUpdateRequestDto emptyDto = new StoreUpdateRequestDto();

            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            // given
            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));

            // when
            StoreUpdateResponseDto response = storeService.updateStore(STORE_ID, authOwner, emptyDto);

            // then
            assertNotNull(response);
            assertAll(
                    () -> assertEquals(response.getName(), store.getName()),
                    () -> assertEquals(response.getImageUrl(), store.getImageUrl()),
                    () -> assertEquals(response.getStartTime(), store.getStartTime()),
                    () -> assertEquals(response.getEndTime(), store.getEndTime()),
                    () -> assertEquals(response.getCategoryId(), store.getCategoryId())
            );
        }

        @Test
        void 수정_성공_시_MQ_메시지_발송() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            Long categoryId2 = 2L;
            Category category2 = Category.builder().id(categoryId2).name("분식").build();

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            ReflectionTestUtils.setField(dto, "categoryId", categoryId2);
            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));
            given(categoryService.findCategory(anyLong())).willReturn(category2);

            // when
            StoreUpdateResponseDto response = storeService.updateStore(STORE_ID, authOwner, dto);

            // then
            assertNotNull(response);
            assertAll(
                    () -> assertEquals(response.getName(), dto.getName()),
                    () -> assertEquals(response.getImageUrl(), dto.getImageUrl()),
                    () -> assertEquals(response.getStartTime(), dto.getStartTime()),
                    () -> assertEquals(response.getEndTime(), dto.getEndTime()),
                    () -> assertEquals(response.getCategoryId(), dto.getCategoryId())
            );
            verify(storeProducer, times(1)).publishStoreUpdate(any(Store.class));
        }
    }

    @Nested
    class 가게_삭제 {

        @Test
        void 존재하지_않는_가게_조회_시_예외_발생() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.deleteStore(STORE_ID, authOwner)
            );
            assertEquals(exception.getMessage(), ErrorCode.STORE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 가게_주인이_아닌_경우_예외_발생() {
            // given
            AuthUser authUser = new AuthUser(USER_ID, "user@a.com", UserRole.ROLE_USER, "일반회원");
            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.deleteStore(STORE_ID, authUser)
            );
            assertEquals(exception.getMessage(), ErrorCode.STORE_FORBIDDEN.getDefaultMessage());
        }

        @Test
        void 가게_이미지가_없는_경우_삭제_처리_없이_성공() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));

            // when
            StoreDeleteResponseDto response = storeService.deleteStore(STORE_ID, authOwner);

            // then
            assertNotNull(response);
            assertEquals(response.getStoreId(), STORE_ID);
            verify(imageService, never()).delete(anyString());
        }

        @Test
        void 가게_이미지_삭제_처리_후_삭제_성공() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);
            String origin = "https://url.com/store/1/origin.jpg";

            ReflectionTestUtils.setField(store, "imageUrl", origin);
            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));

            // when
            StoreDeleteResponseDto response = storeService.deleteStore(STORE_ID, authOwner);

            // then
            assertNotNull(response);
            assertEquals(response.getStoreId(), STORE_ID);
            verify(imageService).delete(anyString());
        }

        @Test
        void 가게_삭제_시_MQ_메세지_발송() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));

            // when
            StoreDeleteResponseDto response = storeService.deleteStore(STORE_ID, authOwner);

            // then
            assertNotNull(response);
            assertEquals(response.getStoreId(), STORE_ID);
            verify(storeProducer, times(1)).publishStoreDelete(store.getId());
        }
    }

    @Nested
    class 가게_검색 {

        @Nested
        class 정렬_기준_예외 {
            @Test
            void asc_desc_외_정렬_입력_시_예외_발생() {
                // given
                AuthUser authUser = new AuthUser(USER_ID, "user@a.com", UserRole.ROLE_USER, "일반회원");

                String sortField = "name";
                String sortOrder = "average";

                // when & then
                HandledException exception = assertThrows(HandledException.class, () ->
                        storeService.getStoresV2(authUser, 1, 10, sortField, sortOrder, null, null)
                );
                assertEquals(exception.getMessage(), ErrorCode.INVALID_ORDER_VALUE.getDefaultMessage());
            }

            @Test
            void 존재하지_않는_정렬_기준_입력_시_예외_발생() {
                // given
                AuthUser authUser = new AuthUser(USER_ID, "user@a.com", UserRole.ROLE_USER, "일반회원");

                String sortField = "description";
                String sortOrder = "asc";

                // when & then
                HandledException exception = assertThrows(HandledException.class, () ->
                        storeService.getStoresV2(authUser, 1, 10, sortField, sortOrder, null, null)
                );
                assertEquals(exception.getMessage(), ErrorCode.INVALID_SORT_FIELD.getDefaultMessage());
            }
        }

        @Test
        void 검색어_제외_조회_성공() {
            // given
            int page = 1;
            int size = 10;
            String sortField = "createdAt";
            String sortOrder = "desc";

            AuthUser authUser = new AuthUser(USER_ID, "user@a.com", UserRole.ROLE_USER, "일반회원");
            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            Page<StoreSearchResponseDto> result = new PageImpl<>(List.of(StoreSearchResponseDto.fromStore(store)));
            given(storeRepository.searchStores(any(Pageable.class), anyLong(), anyString())).willReturn(result);

            // when
            Page<StoreSearchResponseDto> response = storeService.getStoresV2(authUser, page, size, sortField, sortOrder, CATEGORY_ID, "");

            // then
            StoreSearchResponseDto dto = response.getContent().get(0);
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getTotalElements(), 1),
                    () -> assertEquals(dto.getRating(), store.getRating()),
                    () -> assertEquals(dto.getRatingCount(), store.getRatingCount())
            );
        }

        @Test
        void 가게_검색_DB_조회_성공() {
            // given
            int page = 1;
            int size = 10;
            String sortField = "name";
            String sortOrder = "desc";
            String search = "맛있는";

            AuthUser authUser = new AuthUser(USER_ID, "user@a.com", UserRole.ROLE_USER, "일반회원");
            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            given(redisTemplate.hasKey(anyString())).willReturn(true);

            Page<StoreSearchResponseDto> result = new PageImpl<>(List.of(StoreSearchResponseDto.fromStore(store)));
            given(storeRepository.searchStores(any(Pageable.class), anyLong(), anyString())).willReturn(result);

            // when
            Page<StoreSearchResponseDto> response = storeService.getStoresV1(authUser, page, size, sortField, sortOrder, CATEGORY_ID, search);

            // then
            StoreSearchResponseDto dto = response.getContent().get(0);
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getTotalElements(), 1),
                    () -> assertEquals(dto.getRating(), store.getRating()),
                    () -> assertEquals(dto.getRatingCount(), store.getRatingCount())
            );
            verify(storeRepository).searchStores(any(Pageable.class), anyLong(), anyString());
        }

        @Test
        void 가게_검색_redis_조회_성공() {
            // given
            int page = 1;
            int size = 10;
            String sortField = "name";
            String sortOrder = "desc";
            String search = "맛있는";

            AuthUser authUser = new AuthUser(USER_ID, "user@a.com", UserRole.ROLE_USER, "일반회원");
            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            given(redisTemplate.hasKey(anyString())).willReturn(true);
            Page<StoreSearchResponseDto> result = new PageImpl<>(List.of(StoreSearchResponseDto.fromStore(store)));
            given(storeRepository.searchStores(any(Pageable.class), anyLong(), anyString())).willReturn(result);

            // when
            Page<StoreSearchResponseDto> response = storeService.getStoresV2(authUser, page, size, sortField, sortOrder, CATEGORY_ID, search);

            // then
            StoreSearchResponseDto dto = response.getContent().get(0);
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getTotalElements(), 1),
                    () -> assertEquals(dto.getRating(), store.getRating()),
                    () -> assertEquals(dto.getRatingCount(), store.getRatingCount())
            );
        }

        @Test
        void 가게_검색_비로그인_조회_성공() {
            // given
            int page = 1;
            int size = 10;
            String sortField = "name";
            String sortOrder = "desc";
            String search = "맛있는";

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            Page<StoreSearchResponseDto> result = new PageImpl<>(List.of(StoreSearchResponseDto.fromStore(store)));
            given(storeRepository.searchStores(any(Pageable.class), anyLong(), anyString())).willReturn(result);

            // when
            Page<StoreSearchResponseDto> response = storeService.getStoresV2(null, page, size, sortField, sortOrder, CATEGORY_ID, search);

            // then
            StoreSearchResponseDto dto = response.getContent().get(0);
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getTotalElements(), 1),
                    () -> assertEquals(dto.getRating(), store.getRating()),
                    () -> assertEquals(dto.getRatingCount(), store.getRatingCount())
            );
        }

        @Test
        void 검색어_캐시_등록_및_조회_성공() {
            // given
            int page = 1;
            int size = 10;
            String sortField = "rating";
            String sortOrder = "desc";
            String search = "맛있는";

            AuthUser authUser = new AuthUser(USER_ID, "user@a.com", UserRole.ROLE_USER, "일반회원");

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            String hourKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
            String rankKey = StoreConstant.STORE_KEYWORD_RANK_KEY + ":" + hourKey;

            given(redisTemplate.hasKey(anyString())).willReturn(false);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.incrementScore(rankKey, search, 1L)).willReturn(1.0);

            Page<StoreSearchResponseDto> result = new PageImpl<>(List.of(StoreSearchResponseDto.fromStore(store)));
            given(storeRepository.searchStores(any(Pageable.class), anyLong(), anyString())).willReturn(result);

            // when
            Page<StoreSearchResponseDto> response = storeService.getStoresV2(authUser, page, size, sortField, sortOrder, CATEGORY_ID, search);

            // then

            StoreSearchResponseDto dto = response.getContent().get(0);
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getTotalElements(), 1),
                    () -> assertEquals(dto.getRating(), store.getRating()),
                    () -> assertEquals(dto.getRatingCount(), store.getRatingCount())
            );
        }
    }

    @Nested
    class 가게_상세_조회 {

        @Test
        void 존재하지_않는_가게_조회_시_예외_발생() {
            // given
            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.findStore(STORE_ID)
            );
            assertEquals(exception.getMessage(), ErrorCode.STORE_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 조회_성공() {
            // given
            AuthUser authOwner = new AuthUser(OWNER_ID, "owner@a.com", UserRole.ROLE_OWNER, "가게");

            Store store = mockStore(STORE_ID, OWNER_ID, CATEGORY_ID);

            given(storeRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(store));

            // when
            StoreResponseDto response = storeService.findStore(STORE_ID);

            // then
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getStoreId(), STORE_ID),
                    () -> assertEquals(response.getName(), store.getName()),
                    () -> assertEquals(response.getRatingCount(), store.getRatingCount()),
                    () -> assertEquals(response.getRating(), store.getRating())
            );
        }
    }

    @Nested
    class 가게_인기_검색_랭킹_조회 {

        @Test
        void 시간_집계_키_형식이_yyyyMMddHH_또는_yyyyMMdd_가_아닌_경우_예외_발생() {
            // given
            int limit = 10;
            String timeKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    storeService.getStoreRanking(limit, timeKey)
            );
            assertEquals(exception.getMessage(), ErrorCode.STORE_RANKING_TIME_KEY_ERROR.getDefaultMessage());

        }

        @Test
        void 인기_검색어_캐시가_없을_경우_빈_배열_조회_성공() {
            // given
            int limit = 10;
            String timeKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

            String rankKey = StoreConstant.STORE_KEYWORD_RANK_KEY + ":" + timeKey;
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.reverseRangeWithScores(rankKey, 0L, -1))
                    .willReturn(Collections.emptySet());

            // when
            List<StoreRankingResponseDto> response = storeService.getStoreRanking(limit, timeKey);

            // then
            assertNotNull(response);
            assertEquals(response.size(), 0);
        }

        @Test
        void 시간_집계_키가_없을_경우_현재_시간_기준_조회_성공() {
            // given
            int limit = 10;
            String timeKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

            String rankKey = StoreConstant.STORE_KEYWORD_RANK_KEY + ":" + timeKey;
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.reverseRangeWithScores(rankKey, 0L, -1))
                    .willReturn(Collections.emptySet());

            // when
            List<StoreRankingResponseDto> response = storeService.getStoreRanking(limit, null);

            // then
            assertNotNull(response);
            assertEquals(response.size(), 0);
        }

        @Test
        void 시간별_단위_집계_인기_검색어_조회_성공() {
            // given
            int limit = 10;
            String timeKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

            Set<ZSetOperations.TypedTuple<String>> mockResult = new HashSet<>();
            ZSetOperations.TypedTuple<String> tuple1 = new DefaultTypedTuple<>("김치찌개", 100.0);
            ZSetOperations.TypedTuple<String> tuple2 = new DefaultTypedTuple<>("제육볶음", 90.0);
            mockResult.add(tuple1);
            mockResult.add(tuple2);

            String rankKey = StoreConstant.STORE_KEYWORD_RANK_KEY + ":" + timeKey;
            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.reverseRangeWithScores(rankKey, 0L, -1))
                    .willReturn(mockResult);

            // when
            List<StoreRankingResponseDto> response = storeService.getStoreRanking(limit, timeKey);

            // then
            assertNotNull(response);
            assertEquals(response.size(), 2);
        }

        @Test
        void 일자별_단위_집계_인기_검색어_조회_성공() {
            // given
            int limit = 10;

            Set<ZSetOperations.TypedTuple<String>> mockResult = new HashSet<>();
            ZSetOperations.TypedTuple<String> tuple1 = new DefaultTypedTuple<>("김치찌개", 100.0);
            ZSetOperations.TypedTuple<String> tuple2 = new DefaultTypedTuple<>("제육볶음", 90.0);

            String dayKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String rankKey = StoreConstant.STORE_KEYWORD_RANK_KEY + ":" + dayKey + "00";
            mockResult.add(tuple1);
            mockResult.add(tuple2);

            given(redisTemplate.opsForZSet()).willReturn(zSetOperations);
            given(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
                    .willReturn(mockResult);
            given(zSetOperations.reverseRangeWithScores(rankKey, 0L, -1))
                    .willReturn(Collections.emptySet());

            // when
            List<StoreRankingResponseDto> response = storeService.getStoreRanking(limit, dayKey);

            // then
            assertNotNull(response);
            assertEquals(response.size(), 2);
        }
    }

    private static Store mockStore(Long storeId, Long ownerId, Long categoryId) {
        AuthUser authOwner = new AuthUser(ownerId, "owner@a.com", UserRole.ROLE_OWNER, "가게");
        User owner = User.fromAuthUser(authOwner);

        Category category = Category.builder().id(categoryId).build();

        return Store.builder()
                .id(storeId)
                .name("맛있는 가게")
                .description("가게 설명입니다.")
                .address("서울특별시 강남구 테헤란로11길 1 1층")
                .imageUrl(null)
                .capacity(100)
                .startTime(LocalTime.of(9, 00))
                .endTime(LocalTime.of(21, 00))
                .deposit(10000)
                .user(owner)
                .category(category)
                .rating(4.5)
                .ratingCount(100)
                .build();
    }
}
