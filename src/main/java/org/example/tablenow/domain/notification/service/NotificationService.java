package org.example.tablenow.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.notification.dto.request.NotificationRequestDto;
import org.example.tablenow.domain.notification.dto.response.NotificationResponseDto;
import org.example.tablenow.domain.notification.dto.response.NotificationUpdateReadResponseDto;
import org.example.tablenow.domain.notification.entity.Notification;
import org.example.tablenow.domain.notification.repository.NotificationRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.repository.UserRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  // 알림 생성
  public NotificationResponseDto createNotification(NotificationRequestDto requestDto) {
    User findUser = userRepository.findById(requestDto.getUserId())
        .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND));

    //알람 수신 여부 확인
    if(!findUser.getIsAlarmEnabled()){
      throw new HandledException(ErrorCode.FORBIDDEN, "알림 수신을 거부한 사용자입니다.");
    }

    Notification notification = new Notification(findUser, requestDto.getType(), requestDto.getContent());
    notificationRepository.save(notification);

    return new NotificationResponseDto(notification);
  }

  // 알림 조회
  public List<NotificationResponseDto> findNotifications(User user) {
    List<Notification> notificationList = notificationRepository.findAllByUserOrderByCreatedAtDesc(user);
    ArrayList<NotificationResponseDto> responseList = new ArrayList<>();

    for (Notification notification : notificationList){
      responseList.add(new NotificationResponseDto(notification));
    }

    return responseList;
  }

  // 알림 읽음 처리
  @Transactional
  public NotificationUpdateReadResponseDto updateNotificationRead(Long notificationId, User user) {
    Notification findNotification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new HandledException(ErrorCode.NOT_FOUND, "조회한 알람이 존재하지 않습니다."));

    if (!findNotification.getUser().getId().equals(user.getId())){
      throw new HandledException(ErrorCode.FORBIDDEN, "알람을 받은 본인만 읽음 처리를 할 수 있습니다.");
    }

    findNotification.updateRead();
    return new NotificationUpdateReadResponseDto(findNotification.getId(),findNotification.getIsRead());
  }
}
