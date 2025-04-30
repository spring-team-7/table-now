package org.example.tablenow.domain.payment.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.reservation.entity.Reservation;

@Getter
@NoArgsConstructor
public class CheckoutRequestDto {
    private String orderId;
    private Long userId;
    private int price;
    private String storeName;
    private String userEmail;
    private String userName;

    @Builder
    public CheckoutRequestDto(String orderId, Long userId, int price, String storeName, String userEmail, String userName) {
        this.orderId = orderId;
        this.userId = userId;
        this.price = price;
        this.storeName = storeName;
        this.userEmail = userEmail;
        this.userName = userName;
    }

    public static CheckoutRequestDto fromReservation(Reservation reservation) {
        return CheckoutRequestDto.builder()
                .orderId(String.valueOf(reservation.getId()))
                .userId(reservation.getUserId())
                .price(reservation.getStoreDeposit())
                .storeName(reservation.getStoreName())
                .userEmail(reservation.getUserEmail())
                .userName(reservation.getUserName())
                .build();

    }
}
