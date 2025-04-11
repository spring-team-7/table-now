package org.example.tablenow.domain.waitlist.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.entity.TimeStamped;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "waitlist")
public class Waitlist extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "is_notified")
    private Boolean isNotified = false;

    public Waitlist(User user, Store store) {
        this.user = user;
        this.store = store;
        this.isNotified = false;
    }

    public void updateNotified() {
        this.isNotified = true;
    }
}
