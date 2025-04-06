package org.example.tablenow.domain.store.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.entity.Store;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.tablenow.domain.store.entity.QStore.store;

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
                        storeDeletedAtIsNotNull()
                )
                .fetchOne();
    }

    @Override
    public List<Store> findAllByUserId(Long userId) {
        return queryFactory.select(store)
                .from(store)
                .leftJoin(store.category).fetchJoin()
                .where(
                        storeUserIdEq(userId),
                        storeDeletedAtIsNotNull()
                )
                .fetch();
    }

    private BooleanExpression storeUserIdEq(Long userId) {
        return store.user.id.eq(userId);
    }

    private BooleanExpression storeDeletedAtIsNotNull() {
        return store.deletedAt.isNotNull();
    }
}
