package org.example.tablenow.domain.notification.repository;

import org.example.tablenow.domain.notification.entity.Notification;
import org.example.tablenow.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findAllByUserOrderByCreatedAtDesc(User user);
}
