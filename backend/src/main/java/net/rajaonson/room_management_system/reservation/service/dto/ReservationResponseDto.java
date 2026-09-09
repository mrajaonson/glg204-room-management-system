package net.rajaonson.room_management_system.reservation.service.dto;

import net.rajaonson.room_management_system.reservation.model.Reservation;
import net.rajaonson.room_management_system.reservation.model.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponseDto(
        Long id,
        Long roomId,
        Long requesterId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String purpose,
        ReservationStatus status,
        LocalDateTime createdAt) {

    public static ReservationResponseDto from(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getId(),
                reservation.getRoom().getId(),
                reservation.getRequester().getId(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getPurpose(),
                reservation.getStatus(),
                reservation.getCreatedAt());
    }
}
