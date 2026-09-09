package net.rajaonson.room_management_system.reservation.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReservationRejectionDto(@NotBlank @Size(max = 500) String reason) {
}
