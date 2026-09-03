package net.rajaonson.room_management_system.common.dto;

/**
 * Minimal acknowledgement body for endpoints that have nothing else to return.
 */
public record StatusResponseDto(String status) {

    public static StatusResponseDto ok() {
        return new StatusResponseDto("OK");
    }
}
