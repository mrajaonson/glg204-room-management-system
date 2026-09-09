package net.rajaonson.room_management_system.room.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import net.rajaonson.room_management_system.room.model.RoomType;

public record RoomCreationRequestDto(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 255) String location,
        @Positive int capacity,
        @NotNull RoomType type,
        @Size(max = 500) String description,
        boolean reservationRequiresApproval) {
}
