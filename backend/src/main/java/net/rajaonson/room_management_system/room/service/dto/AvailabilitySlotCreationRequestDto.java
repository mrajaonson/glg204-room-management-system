package net.rajaonson.room_management_system.room.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AvailabilitySlotCreationRequestDto(
        @NotNull LocalDateTime startAt,
        @NotNull LocalDateTime endAt) {

    @JsonIgnore
    @AssertTrue(message = "end must be after start")
    public boolean isPeriodValid() {
        return startAt != null && endAt != null && endAt.isAfter(startAt);
    }
}
