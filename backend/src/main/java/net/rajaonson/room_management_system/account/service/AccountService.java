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
import net.rajaonson.room_management_system.common.error.ApiErrors;
import net.rajaonson.room_management_system.notification.service.NotificationService;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        if (accountRepository.existsByLogin(login) || requestRepository.existsByLogin(login)
                || accountRepository.existsByEmail(email) || requestRepository.existsByEmail(email)) {
            throw ApiErrors.conflict("an account or a pending request already exists for this login or email");
        }

        AccountCreationRequest request =
                new AccountCreationRequest(login, passwordEncoder.encode(dto.getPassword()), email);
        AccountCreationRequest saved = requestRepository.save(request);

        if (notificationService.sendConfirmationEmail(saved.getEmail(), saved.getValidationToken())) {
            saved.markEmailSent();
            requestRepository.save(saved);
        }
    }

    public void validateEmail(String token) {
        AccountCreationRequest request = requestRepository
                .findByValidationToken(token)
                .orElseThrow(() -> ApiErrors.notFound("no pending request matches this validation token"));

        request.markEmailValidated();

        requestRepository.save(request);
    }

    @Transactional
    public AccountResponseDto validateAccountCreationRequest(Long id) {
        AccountCreationRequest request = requestRepository.findById(id)
                .orElseThrow(() -> ApiErrors.notFound("no account creation request with id %d".formatted(id)));

        if (accountRepository.existsByLogin(request.getLogin())
                || accountRepository.existsByEmail(request.getEmail())) {
            throw ApiErrors.conflict("an account already exists with this login or email");
        }

        request.approve();
        requestRepository.save(request);

        Account account = accountRepository.save(new Account(
                request.getLogin(), request.getPasswordHash(), request.getEmail(), Role.USER));

        notificationService.sendApprovalEmail(account.getEmail(), account.getLogin());

        log.info("validated account creation request {} into account {}", id, account.getId());

        return AccountResponseDto.from(account);
    }

    @Transactional(readOnly = true)
    public List<AccountCreationRequestResponseDto> findAccountCreationRequests() {
        return requestRepository
                .findByStatusNotInOrderByCreatedAtDesc(
                        List.of(RequestStatus.VALIDATED, RequestStatus.REFUSED))
                .stream()
                .map(AccountCreationRequestResponseDto::from)
                .toList();
    }

    @Transactional
    public AccountCreationRequestResponseDto refuseAccountCreationRequest(Long id) {
        AccountCreationRequest request = requestRepository.findById(id)
                .orElseThrow(() -> ApiErrors.notFound("no account creation request with id %d".formatted(id)));

        request.refuse();
        requestRepository.save(request);

        notificationService.sendRefusalEmail(request.getEmail());

        log.info("refused account creation request {}", id);

        return AccountCreationRequestResponseDto.from(request);
    }
}
