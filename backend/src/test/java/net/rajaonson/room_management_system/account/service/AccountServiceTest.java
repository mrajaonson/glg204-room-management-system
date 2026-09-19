package net.rajaonson.room_management_system.account.service;

import net.rajaonson.room_management_system.TestEntities;
import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.account.model.AccountCreationRequest;
import net.rajaonson.room_management_system.account.model.RequestStatus;
import net.rajaonson.room_management_system.account.model.Role;
import net.rajaonson.room_management_system.account.repository.AccountCreationRequestRepository;
import net.rajaonson.room_management_system.account.repository.AccountRepository;
import net.rajaonson.room_management_system.account.service.dto.AccountCreationRequestDto;
import net.rajaonson.room_management_system.account.service.dto.AccountResponseDto;
import net.rajaonson.room_management_system.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.ErrorResponseException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountCreationRequestRepository requestRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AccountService service;

    @Test
    void storesTheCreationRequestWithAHashedPassword() {
        when(accountRepository.existsByLogin("alice")).thenReturn(false);
        when(requestRepository.existsByLogin("alice")).thenReturn(false);
        when(accountRepository.existsByEmail("alice@example.org")).thenReturn(false);
        when(requestRepository.existsByEmail("alice@example.org")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("hashed");
        when(requestRepository.save(any(AccountCreationRequest.class)))
                .thenAnswer(invocation -> TestEntities.withId(invocation.getArgument(0), 1L));
        when(notificationService.sendConfirmationEmail(anyString(), anyString())).thenReturn(true);

        service.createAccountCreationRequest(
                new AccountCreationRequestDto("alice", "password1", "password1", "alice@example.org"));

        ArgumentCaptor<AccountCreationRequest> saved =
                ArgumentCaptor.forClass(AccountCreationRequest.class);
        verify(requestRepository, times(2)).save(saved.capture());
        AccountCreationRequest request = saved.getValue();
        assertThat(request.getPasswordHash()).isEqualTo("hashed");
        assertThat(request.getLogin()).isEqualTo("alice");
        assertThat(request.getStatus()).isEqualTo(RequestStatus.EMAIL_SENT);
    }

    @Test
    void keepsTheRequestUnsentWhenTheEmailFails() {
        when(accountRepository.existsByLogin("alice")).thenReturn(false);
        when(requestRepository.existsByLogin("alice")).thenReturn(false);
        when(accountRepository.existsByEmail("alice@example.org")).thenReturn(false);
        when(requestRepository.existsByEmail("alice@example.org")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("hashed");
        when(requestRepository.save(any(AccountCreationRequest.class)))
                .thenAnswer(invocation -> TestEntities.withId(invocation.getArgument(0), 1L));
        when(notificationService.sendConfirmationEmail(anyString(), anyString())).thenReturn(false);

        service.createAccountCreationRequest(
                new AccountCreationRequestDto("alice", "password1", "password1", "alice@example.org"));

        ArgumentCaptor<AccountCreationRequest> saved =
                ArgumentCaptor.forClass(AccountCreationRequest.class);
        verify(requestRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(RequestStatus.CREATED);
    }

    @Test
    void refusesALoginAlreadyTaken() {
        when(accountRepository.existsByLogin("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.createAccountCreationRequest(
                new AccountCreationRequestDto("alice", "password1", "password1", "alice@example.org")))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(requestRepository, never()).save(any());
    }

    @Test
    void validatesTheEmailOfAPendingRequest() {
        AccountCreationRequest request = pendingRequest();
        when(requestRepository.findByValidationToken("token")).thenReturn(Optional.of(request));

        service.validateEmail("token");

        assertThat(request.getStatus()).isEqualTo(RequestStatus.EMAIL_VALIDATED);
        verify(requestRepository).save(request);
    }

    @Test
    void refusesAnUnknownValidationToken() {
        when(requestRepository.findByValidationToken("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateEmail("nope"))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void turnsAnApprovedRequestIntoAUserAccount() {
        AccountCreationRequest request = pendingRequest();
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(accountRepository.existsByLogin("alice")).thenReturn(false);
        when(accountRepository.existsByEmail("alice@example.org")).thenReturn(false);
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> TestEntities.withId(invocation.getArgument(0), 2L));

        AccountResponseDto response = service.validateAccountCreationRequest(1L);

        assertThat(response.login()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.VALIDATED);
        verify(notificationService).sendApprovalEmail("alice@example.org", "alice");
    }

    @Test
    void refusesToValidateARequestWhoseLoginIsNowTaken() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest()));
        when(accountRepository.existsByLogin("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.validateAccountCreationRequest(1L))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void refusesARequestAndNotifiesTheApplicant() {
        AccountCreationRequest request = pendingRequest();
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        service.refuseAccountCreationRequest(1L);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.REFUSED);
        verify(notificationService).sendRefusalEmail("alice@example.org");
    }

    @Test
    void loadsAUserByLogin() {
        when(accountRepository.findByLogin("alice"))
                .thenReturn(Optional.of(TestEntities.account(1L, "alice", Role.MANAGER)));

        UserDetails user = service.loadUserByUsername("alice");

        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getAuthorities()).extracting(Object::toString).contains("ROLE_MANAGER");
    }

    @Test
    void refusesToLoadAnUnknownLogin() {
        when(accountRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private static AccountCreationRequest pendingRequest() {
        return TestEntities.withId(
                new AccountCreationRequest("alice", "hashed", "alice@example.org"), 1L);
    }
}
