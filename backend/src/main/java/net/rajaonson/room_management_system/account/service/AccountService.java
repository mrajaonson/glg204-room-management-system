package net.rajaonson.room_management_system.account.service;

import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.account.model.AccountCreationRequest;
import net.rajaonson.room_management_system.account.model.RequestStatus;
import net.rajaonson.room_management_system.account.model.Role;
import net.rajaonson.room_management_system.account.repository.AccountCreationRequestRepository;
import net.rajaonson.room_management_system.account.repository.AccountRepository;
import net.rajaonson.room_management_system.account.service.dto.AccountCreationRequestDto;
import net.rajaonson.room_management_system.account.service.dto.AccountCreationRequestResponseDto;
import net.rajaonson.room_management_system.account.service.dto.AccountResponseDto;
import net.rajaonson.room_management_system.notification.service.NotificationService;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
@NullMarked
public class AccountService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final AccountCreationRequestRepository requestRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public AccountService(AccountRepository accountRepository, AccountCreationRequestRepository requestRepository, PasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.accountRepository = accountRepository;
        this.requestRepository = requestRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByLogin(username)
                .map(account -> User.withUsername(account.getLogin())
                        .password(account.getPasswordHash())
                        .roles(account.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("account not found"));
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
        AccountCreationRequest saved = requestRepository.save(request);

        try {
            notificationService.sendConfirmationEmail(saved.getEmail(), saved.getValidationToken());
            saved.markEmailSent();
            requestRepository.save(saved);
        } catch (MailException e) {
            log.error("failed to send confirmation email for request {}", saved.getId(), e);
        }
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

    @Transactional
    public AccountResponseDto validateAccountCreationRequest(Long id) {
        AccountCreationRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ErrorResponseException(HttpStatus.NOT_FOUND));

        if (accountRepository.existsByLogin(request.getLogin())
                || accountRepository.existsByEmail(request.getEmail())) {
            throw new ErrorResponseException(HttpStatus.CONFLICT);
        }

        request.approve();
        requestRepository.save(request);

        Account account = accountRepository.save(new Account(
                request.getLogin(), request.getPasswordHash(), request.getEmail(), Role.USER));

        log.info("validated account creation request {} into account {}", id, account.getId());

        return AccountResponseDto.from(account);
    }

    public List<AccountCreationRequestResponseDto> findEmailValidatedRequests() {
        return requestRepository.findByStatus(RequestStatus.EMAIL_VALIDATED)
                .stream()
                .map(AccountCreationRequestResponseDto::from)
                .toList();
    }
}
