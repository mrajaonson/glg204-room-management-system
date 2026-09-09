package net.rajaonson.room_management_system.reservation.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ReservationCreationRequestDto(
        @NotNull Long roomId,
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt,
        @NotBlank @Size(max = 500) String purpose) {

    @JsonIgnore
    @AssertTrue(message = "end must be after start")
    public boolean isPeriodValid() {
        return startAt != null && endAt != null && endAt.isAfter(startAt);
    }
}
