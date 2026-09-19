package net.rajaonson.room_management_system.reservation.service;

import net.rajaonson.room_management_system.TestEntities;
import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.account.model.Role;
import net.rajaonson.room_management_system.account.repository.AccountRepository;
import net.rajaonson.room_management_system.notification.service.NotificationService;
import net.rajaonson.room_management_system.reservation.model.Reservation;
import net.rajaonson.room_management_system.reservation.model.ReservationStatus;
import net.rajaonson.room_management_system.reservation.repository.ReservationRepository;
import net.rajaonson.room_management_system.reservation.service.dto.ReservationCreationRequestDto;
import net.rajaonson.room_management_system.reservation.service.dto.ReservationResponseDto;
import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.repository.AvailabilitySlotRepository;
import net.rajaonson.room_management_system.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final long ROOM_ID = 10L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 1, 11, 0);

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReservationService service;

    private Account requester;

    @BeforeEach
    void setUp() {
        requester = TestEntities.account(1L, "alice", Role.USER);
    }

    @Test
    void createsAConfirmedReservationWhenTheRoomNeedsNoApproval() {
        Room room = givenRoom(false);
        givenSlotCovers(true);
        givenPeriodTaken(false);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        givenAccount(requester);

        ReservationResponseDto response = service.createReservation(creationDto(), "alice");

        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(response.roomId()).isEqualTo(room.getId());
        assertThat(response.requesterLogin()).isEqualTo("alice");
        verify(notificationService)
                .sendReservationConfirmationEmail(eq(requester.getEmail()), any(Reservation.class));
    }

    @Test
    void createsAPendingReservationWhenTheRoomRequiresApproval() {
        givenRoom(true);
        givenSlotCovers(true);
        givenPeriodTaken(false);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        givenAccount(requester);

        ReservationResponseDto response = service.createReservation(creationDto(), "alice");

        assertThat(response.status()).isEqualTo(ReservationStatus.PENDING_APPROVAL);
        verify(notificationService)
                .sendReservationPendingEmail(eq(requester.getEmail()), any(Reservation.class));
    }

    @Test
    void refusesAPeriodNoAvailabilitySlotCovers() {
        givenRoom(false);
        givenSlotCovers(false);
        givenAccount(requester);

        assertThatThrownBy(() -> service.createReservation(creationDto(), "alice"))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void refusesAPeriodAlreadyBooked() {
        givenRoom(false);
        givenSlotCovers(true);
        givenPeriodTaken(true);
        givenAccount(requester);

        assertThatThrownBy(() -> service.createReservation(creationDto(), "alice"))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void refusesAnUnknownRoom() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());
        givenAccount(requester);

        assertThatThrownBy(() -> service.createReservation(creationDto(), "alice"))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void refusesAnAuthenticatedAccountThatNoLongerExists() {
        when(accountRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createReservation(creationDto(), "ghost"))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void letsTheRequesterCancelTheirReservation() {
        givenReservation(reservation(false));
        givenAccount(requester);

        ReservationResponseDto response = service.cancelReservation(5L, "alice");

        assertThat(response.status()).isEqualTo(ReservationStatus.CANCELLED);
        verify(notificationService)
                .sendReservationCancellationEmail(eq(requester.getEmail()), any(Reservation.class));
    }

    @Test
    void letsAManagerCancelSomeoneElsesReservation() {
        givenReservation(reservation(false));
        givenAccount(TestEntities.account(2L, "boss", Role.MANAGER));

        ReservationResponseDto response = service.cancelReservation(5L, "boss");

        assertThat(response.status()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void refusesACancellationByAnotherUser() {
        givenReservation(reservation(false));
        givenAccount(TestEntities.account(2L, "bob", Role.USER));

        assertThatThrownBy(() -> service.cancelReservation(5L, "bob"))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void approvesAPendingReservation() {
        Reservation reservation = reservation(true);
        givenReservation(reservation);

        ReservationResponseDto response = service.approveReservation(5L);

        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationRepository).save(reservation);
        verify(notificationService)
                .sendReservationConfirmationEmail(requester.getEmail(), reservation);
    }

    @Test
    void rejectsAPendingReservationWithItsReason() {
        Reservation reservation = reservation(true);
        givenReservation(reservation);

        ReservationResponseDto response = service.rejectReservation(5L, "room under maintenance");

        assertThat(response.status()).isEqualTo(ReservationStatus.REJECTED);
        verify(notificationService)
                .sendReservationRejectionEmail(requester.getEmail(), reservation, "room under maintenance");
    }

    @Test
    void refusesAnUnknownReservation() {
        when(reservationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReservation(404L))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private ReservationCreationRequestDto creationDto() {
        return new ReservationCreationRequestDto(ROOM_ID, START, END, "course");
    }

    private Reservation reservation(boolean requiresApproval) {
        return TestEntities.withId(
                new Reservation(TestEntities.room(ROOM_ID, requiresApproval), requester, START, END, "course"),
                5L);
    }

    private Room givenRoom(boolean requiresApproval) {
        Room room = TestEntities.room(ROOM_ID, requiresApproval);
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        return room;
    }

    private void givenAccount(Account account) {
        when(accountRepository.findByLogin(account.getLogin())).thenReturn(Optional.of(account));
    }

    private void givenReservation(Reservation reservation) {
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
    }

    private void givenSlotCovers(boolean covered) {
        when(availabilitySlotRepository
                .existsByRoomIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(ROOM_ID, START, END))
                .thenReturn(covered);
    }

    private void givenPeriodTaken(boolean taken) {
        when(reservationRepository.existsByRoomIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                anyLong(), ArgumentMatchers.<Collection<ReservationStatus>>any(),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(taken);
    }
}
