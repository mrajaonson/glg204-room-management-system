package net.rajaonson.room_management_system.reservation.controller;

import jakarta.validation.Valid;
import net.rajaonson.room_management_system.reservation.model.ReservationStatus;
import net.rajaonson.room_management_system.reservation.service.ReservationService;
import net.rajaonson.room_management_system.reservation.service.dto.ReservationCreationRequestDto;
import net.rajaonson.room_management_system.reservation.service.dto.ReservationRejectionDto;
import net.rajaonson.room_management_system.reservation.service.dto.ReservationResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDto createReservation(
            @Valid @RequestBody ReservationCreationRequestDto requestDto,
            @AuthenticationPrincipal Jwt jwt) {
        return reservationService.createReservation(requestDto, jwt.getSubject());
    }

    @GetMapping("/{id}")
    public ReservationResponseDto getReservation(@PathVariable Long id,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return reservationService.getReservation(id, jwt.getSubject());
    }

    @GetMapping
    public List<ReservationResponseDto> listReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) Long roomId,
            @RequestParam(defaultValue = "false") boolean currentUserOnly,
            @AuthenticationPrincipal Jwt jwt) {
        return reservationService.listReservations(status, roomId, currentUserOnly, jwt.getSubject());
    }

    @PutMapping("/{id}/cancel")
    public ReservationResponseDto cancelReservation(@PathVariable Long id,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return reservationService.cancelReservation(id, jwt.getSubject());
    }

    @PutMapping("/{id}/approve")
    public ReservationResponseDto approveReservation(@PathVariable Long id) {
        return reservationService.approveReservation(id);
    }

    @PutMapping("/{id}/reject")
    public ReservationResponseDto rejectReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRejectionDto requestDto) {
        return reservationService.rejectReservation(id, requestDto.reason());
    }
}
