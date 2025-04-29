package org.example.tablenow.domain.rating.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.entity.TimeStamped;

import java.util.Optional;

@Getter
@Entity
@Table(name = "rating")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // UserRole: ROLE_USER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(nullable = false)
    private Integer rating;

    @Builder
    private Rating(Long id, User user, Store store, Integer rating) {
        this.id = id;
        this.user = user;
        this.store = store;
        this.rating = rating;
    }

    public void updateRating(Integer rating) {
        this.rating = rating;
    }

    public Long getUserId() {
        return Optional.ofNullable(this.user)
                .map(User::getId)
                .orElse(null);
    }

    public Long getStoreId() {
        return Optional.ofNullable(this.store)
                .map(Store::getId)
                .orElse(null);
    }
}
