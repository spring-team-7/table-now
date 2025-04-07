package org.example.tablenow.domain.store.enums;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import org.apache.coyote.BadRequestException;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;

import java.time.LocalDateTime;

public enum StoreSortField {
    NAME("name") {
        @Override
        public OrderSpecifier<?> toOrderSpecifier(PathBuilder<?> path, boolean isAsc) {
            return new OrderSpecifier<>(
                    isAsc ? Order.ASC : Order.DESC,
                    path.getString(property)
            );
        }
    },
    CREATED_AT("createdAt") {
        @Override
        public OrderSpecifier<?> toOrderSpecifier(PathBuilder<?> path, boolean isAsc) {
            return new OrderSpecifier<>(
                    isAsc ? Order.ASC : Order.DESC,
                    path.getDateTime(property, LocalDateTime.class)
            );
        }
    };

    protected final String property;

    StoreSortField(String property) {
        this.property = property;
    }

    public abstract OrderSpecifier<?> toOrderSpecifier(PathBuilder<?> path, boolean isAsc);

    public static StoreSortField from(String property) {
        for (StoreSortField field : values()) {
            if (field.property.equals(property)) {
                return field;
            }
        }
        throw new HandledException(ErrorCode.BAD_REQUEST);
    }
}
