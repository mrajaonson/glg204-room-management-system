package net.rajaonson.room_management_system.room.service.dto;

import net.rajaonson.room_management_system.room.model.Equipment;

public record EquipmentResponseDto(Long id, String name, String description, Long roomId) {

    public static EquipmentResponseDto from(Equipment equipment) {
        return new EquipmentResponseDto(
                equipment.getId(),
                equipment.getName(),
                equipment.getDescription(),
                equipment.getRoom().getId());
    }
}
