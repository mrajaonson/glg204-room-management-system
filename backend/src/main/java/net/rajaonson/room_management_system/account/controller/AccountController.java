package net.rajaonson.room_management_system.account.controller;

import jakarta.validation.Valid;
import net.rajaonson.room_management_system.account.service.AccountService;
import net.rajaonson.room_management_system.account.service.dto.AccountCreationRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public void createAccountCreationRequest(@Valid @RequestBody AccountCreationRequestDto request) {
        accountService.createAccountCreationRequest(request);
    }
}
