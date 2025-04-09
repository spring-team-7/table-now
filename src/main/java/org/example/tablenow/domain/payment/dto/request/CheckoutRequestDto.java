package org.example.tablenow.domain.payment.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.reservation.entity.Reservation;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckoutRequestDto {
    private String orderNum;
    private Long customerId;
    private int price;
    private String itemName;
    private String customerEmail;
    private String customerName;

    public CheckoutRequestDto(String orderNum, Long customerId, int price,
                              String itemName, String customerEmail, String customerName) {
        this.orderNum = orderNum;
        this.customerId = customerId;
        this.price = price;
        this.itemName = itemName;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
    }

    public static CheckoutRequestDto fromReservation(Reservation reservation) {
        return new CheckoutRequestDto(
                String.valueOf(reservation.getId()),
                reservation.getUser().getId(),
                reservation.getStore().getDeposit(),
                reservation.getStore().getName(),
                reservation.getUser().getEmail(),
                reservation.getUser().getName()
        );
    }
}
