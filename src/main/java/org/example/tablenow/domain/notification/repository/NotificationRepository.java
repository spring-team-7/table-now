package org.example.tablenow.domain.notification.repository;

import org.example.tablenow.domain.notification.entity.Notification;
import org.example.tablenow.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  Page<Notification> findAllByUserAndIsRead(User user, Boolean isRead, Pageable pageable);

  List<Notification> findAllByUserAndIsReadFalse(User user);

  Page<Notification> findAllByUser(User user, Pageable pageable);
}
