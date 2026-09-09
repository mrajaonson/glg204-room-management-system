package net.rajaonson.room_management_system.room.service.dto;

import net.rajaonson.room_management_system.room.model.AvailabilitySlot;

import java.time.LocalDateTime;

public record AvailabilitySlotResponseDto(
        Long id,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Long roomId) {

    public static AvailabilitySlotResponseDto from(AvailabilitySlot slot) {
        return new AvailabilitySlotResponseDto(
                slot.getId(),
                slot.getStartAt(),
                slot.getEndAt(),
                slot.getRoom().getId());
    }
}
