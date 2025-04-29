package org.example.tablenow.domain.store.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.entity.TimeStamped;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Getter
@Entity
@Table(name = "store")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    private String address;
    private String imageUrl;
    private Integer capacity;
    private Integer deposit;
    private Double rating = 0.0;
    private Integer ratingCount = 0;
    @Column(nullable = false)
    private LocalTime startTime;
    @Column(nullable = false)
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // UserRole: ROLE_OWNER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        rating = 0.0;
        ratingCount = 0;
    }

    @Builder
    private Store(Long id, String name, String description, String address, String imageUrl, Integer capacity, Integer deposit, Double rating, Integer ratingCount, LocalTime startTime, LocalTime endTime, User user, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.imageUrl = imageUrl;
        this.capacity = capacity;
        this.deposit = deposit;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
        this.category = category;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateAddress(String address) {
        this.address = address;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void updateDeposit(Integer deposit) {
        this.deposit = deposit;
    }

    public void updateStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void updateEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void deleteStore() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isOpenAt(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(this.startTime) && !time.isAfter(this.endTime);
    }

    public boolean hasVacancy(long reservedCount) {
        return reservedCount < this.capacity;
    }

    public void applyRating(Integer rating) {
        Integer newRatingCount = this.ratingCount + 1;
        Double newRating = ((this.rating * this.ratingCount) + rating) / newRatingCount;

        this.ratingCount = newRatingCount;
        this.rating = newRating;
    }

    public void removeRating(Integer rating) {
        Integer newRatingCount = this.ratingCount - 1;

        if (newRatingCount <= 0) {
            this.ratingCount = 0;
            this.rating = 0.0;
            return;
        }
        Double newRating = ((this.rating * this.ratingCount) - rating) / newRatingCount;

        this.ratingCount = newRatingCount;
        this.rating = newRating;
    }

    public void updateRating(Integer oldRating, Integer newRating) {
        if (this.ratingCount == 0) {
            throw new HandledException(ErrorCode.CONFLICT);
        }

        Double resultRating = ((this.rating * this.ratingCount) - oldRating + newRating) / this.ratingCount;
        this.rating = resultRating;
    }

    public Long getUserId() {
        return Optional.ofNullable(this.user)
                .map(User::getId)
                .orElse(null);
    }

    public String getUserName() {
        return Optional.ofNullable(this.user)
                .map(User::getName)
                .orElse(null);
    }

    public Long getCategoryId() {
        return Optional.ofNullable(this.category)
                .map(Category::getId)
                .orElse(null);
    }

    public String getCategoryName() {
        return Optional.ofNullable(this.category)
                .map(Category::getName)
                .orElse(null);
    }
}
