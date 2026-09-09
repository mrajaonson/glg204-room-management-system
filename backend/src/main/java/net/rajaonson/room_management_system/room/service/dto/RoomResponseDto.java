package net.rajaonson.room_management_system.room.service.dto;

import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.model.RoomType;

public record RoomResponseDto(
        Long id,
        String name,
        String location,
        int capacity,
        RoomType type,
        String description,
        boolean reservationRequiresApproval) {

    public static RoomResponseDto from(Room room) {
        return new RoomResponseDto(
                room.getId(),
                room.getName(),
                room.getLocation(),
                room.getCapacity(),
                room.getType(),
                room.getDescription(),
                room.isReservationRequiresApproval());
    }
}
