package org.example.tablenow.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.payment.dto.request.CheckoutRequestDto;
import org.example.tablenow.domain.reservation.entity.Reservation;
import org.example.tablenow.domain.reservation.repository.ReservationRepository;
import org.example.tablenow.domain.reservation.service.ReservationService;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentViewController {

    private final ReservationService reservationService;

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

