package net.rajaonson.room_management_system.account.service.dto;

import net.rajaonson.room_management_system.account.model.AccountCreationRequest;
import net.rajaonson.room_management_system.account.model.RequestStatus;

import java.time.LocalDateTime;

/**
 * Outbound view of an {@link AccountCreationRequest}. Deliberately omits the password hash and the
 * validation token, which must never leave the server.
 */
public record AccountCreationRequestResponseDto(
        Long id,
        String login,
        String email,
        RequestStatus status,
        LocalDateTime createdAt) {

    public static AccountCreationRequestResponseDto from(AccountCreationRequest request) {
        return new AccountCreationRequestResponseDto(
                request.getId(),
                request.getLogin(),
                request.getEmail(),
                request.getStatus(),
                request.getCreatedAt());
    }
}
