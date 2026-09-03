package net.rajaonson.room_management_system.account.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import net.rajaonson.room_management_system.account.service.AccountService;
import net.rajaonson.room_management_system.account.service.dto.AccountCreationRequestDto;
import net.rajaonson.room_management_system.account.service.dto.AccountCreationRequestResponseDto;
import net.rajaonson.room_management_system.common.dto.StatusResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @SecurityRequirements
    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public void createAccountCreationRequest(@Valid @RequestBody AccountCreationRequestDto request) {
        accountService.createAccountCreationRequest(request);
    }

    @SecurityRequirements
    @GetMapping("/requests/validate")
    public StatusResponseDto validateEmail(@RequestParam String token) {
        accountService.validateEmail(token);
        return StatusResponseDto.ok();
    }

    @GetMapping("/requests")
    public List<AccountCreationRequestResponseDto> getEmailValidatedRequests() {
        return accountService.findEmailValidatedRequests();
    }
}
