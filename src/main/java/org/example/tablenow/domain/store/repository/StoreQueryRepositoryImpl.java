package org.example.tablenow.domain.store.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.response.StoreResponseDto;
import org.example.tablenow.domain.store.dto.response.StoreSearchResponseDto;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.enums.StoreSortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.example.tablenow.domain.category.entity.QCategory.category;
import static org.example.tablenow.domain.store.entity.QStore.store;
import static org.example.tablenow.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class StoreQueryRepositoryImpl implements StoreQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Long countActiveStoresByUser(Long userId) {
        return queryFactory.select(Wildcard.count)
                .from(store)
                .where(
                        storeUserIdEq(userId),
                        storeDeletedAtIsNull()
                )
                .fetchOne();
    }

    @Override
    public List<StoreResponseDto> findAllByUserId(Long userId) {
        return queryFactory.select(Projections.constructor(
                        StoreResponseDto.class,
                        store.id,
                        store.name,
                        user.id,
                        category.id,
                        category.name,
                        store.description,
                        store.address,
                        store.imageUrl,
                        store.capacity,
                        store.startTime,
                        store.endTime,
                        store.deposit,
                        store.rating,
                        store.ratingCount
                ))
                .from(store)
                .join(store.user, user)
                .join(store.category, category)
                .where(
                        storeUserIdEq(userId),
                        storeDeletedAtIsNull()
                )
                .fetch();
    }

    @Override
    public Page<StoreSearchResponseDto> searchStores(Pageable pageable, Long categoryId, String keyword) {
        JPAQuery<StoreSearchResponseDto> query = queryFactory.select(Projections.constructor(
                        StoreSearchResponseDto.class,
                        store.id,
                        store.name,
                        category.id,
                        category.name,
                        store.imageUrl,
                        store.startTime,
                        store.endTime,
                        store.rating,
                        store.ratingCount
                ))
                .from(store)
                .join(store.category, category);

        // 검색 조건
        BooleanExpression[] conditions = {storeDeletedAtIsNull(), storeCategoryIdEq(categoryId), storeNameContains(keyword)};
        query.where(conditions);

        // 정렬 조건
        query.orderBy(toOrderSpecifiers(pageable.getSort()));
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        List<StoreSearchResponseDto> stores = query.fetch();

        Long totalSize = queryFactory.select(Wildcard.count)
                .from(store)
                .where(conditions)
                .fetchOne();

        return PageableExecutionUtils.getPage(stores, pageable, () -> totalSize);
    }

    private BooleanExpression storeUserIdEq(Long userId) {
        return store.user.id.eq(userId);
    }

    private BooleanExpression storeDeletedAtIsNull() {
        return store.deletedAt.isNull();
    }

    private BooleanExpression storeCategoryIdEq(Long categoryId) {
        return Objects.nonNull(categoryId) ? store.category.id.eq(categoryId) : null;
    }

    private BooleanExpression storeNameContains(String keyword) {
        return Objects.nonNull(keyword) ? store.name.contains(keyword) : null;
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        PathBuilder<Store> path = new PathBuilder<>(Store.class, "store");
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order order : sort) {
            StoreSortField sortField = StoreSortField.from(order.getProperty());
            orderSpecifiers.add(sortField.toOrderSpecifier(path, order.isAscending()));
        }
        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }

}
