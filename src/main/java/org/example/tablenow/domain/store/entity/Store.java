package org.example.tablenow.domain.store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.entity.TimeStamped;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "store")
@NoArgsConstructor
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
    @Min(0)
    private int capacity;
    @Min(0)
    private int deposit;
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

    @Builder
    private Store(Long id, String name, String description, String address, String imageUrl, int capacity, int deposit, LocalTime startTime, LocalTime endTime, User user, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.imageUrl = imageUrl;
        this.capacity = capacity;
        this.deposit = deposit;
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

    public void updateCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void updateDeposit(int deposit) {
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

    public List<LocalTime> getStoreTimeSlots() {
        List<LocalTime> timeSlots = new ArrayList<>();

        LocalTime currentTime = this.startTime;
        while (currentTime.isBefore(endTime)) {
            timeSlots.add(currentTime);
            currentTime = currentTime.plusHours(1);
        }
        return timeSlots;
    }

    public boolean hasVacancy(long reservedCount) {
        return reservedCount < this.capacity;
    }
}
