package net.rajaonson.room_management_system.account.service;

import net.rajaonson.room_management_system.account.model.AccountCreationRequest;
import net.rajaonson.room_management_system.account.repository.AccountCreationRequestRepository;
import net.rajaonson.room_management_system.account.repository.AccountRepository;
import net.rajaonson.room_management_system.account.service.dto.AccountCreationRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountCreationRequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, AccountCreationRequestRepository requestRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createAccountCreationRequest(AccountCreationRequestDto dto) {
        String login = dto.getLogin();
        String email = dto.getEmail();

        if (accountRepository.existsByLogin(login) || requestRepository.existsByLogin(login)) {
            throw new ErrorResponseException(HttpStatus.CONFLICT);
        }
        if (accountRepository.existsByEmail(email) || requestRepository.existsByEmail(email)) {
            throw new ErrorResponseException(HttpStatus.CONFLICT);
        }

        AccountCreationRequest request =
                new AccountCreationRequest(login, passwordEncoder.encode(dto.getPassword()), email);
        requestRepository.save(request);
    }

    public void validateEmail(String token) {
        AccountCreationRequest request = requestRepository
                .findByValidationToken(token)
                .orElseThrow(() -> new ErrorResponseException(HttpStatus.NOT_FOUND));

        try {
            request.markEmailValidated();
        } catch (IllegalStateException e) {
            throw new ErrorResponseException(HttpStatus.CONFLICT);
        }

        requestRepository.save(request);
    }
}
