package org.example.tablenow.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.dto.request.CheckoutRequestDto;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "결제 뷰 API")
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentViewController {

    private final ReservationService reservationService;

    @Operation(summary = "결제 위젯 페이지 뷰 반환")
    @GetMapping("/v1/view/reservations/{reservationId}/payments/checkout")
    public String widget(
            Model model,
            @PathVariable Long reservationId
            ) {
        Reservation reservation = reservationService.getReservation(reservationId);
        CheckoutRequestDto checkoutRequestDto = CheckoutRequestDto.fromReservation(reservation);

        model.addAttribute("payRequest", checkoutRequestDto);

        return "widget/checkout"; // templates/widget/checkout.html
    }
}
