package net.rajaonson.room_management_system.account.service.dto;

import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.account.model.Role;

public record AccountResponseDto(Long id, String login, String email, Role role) {

    public static AccountResponseDto from(Account account) {
        return new AccountResponseDto(
                account.getId(),
                account.getLogin(),
                account.getEmail(),
                account.getRole());
    }
}
