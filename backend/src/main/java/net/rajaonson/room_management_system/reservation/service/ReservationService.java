package net.rajaonson.room_management_system.reservation.service;

import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.account.repository.AccountRepository;
import net.rajaonson.room_management_system.common.error.ApiErrors;
import net.rajaonson.room_management_system.notification.service.NotificationService;
import net.rajaonson.room_management_system.reservation.model.Reservation;
import net.rajaonson.room_management_system.reservation.model.ReservationStatus;
import net.rajaonson.room_management_system.reservation.repository.ReservationRepository;
import net.rajaonson.room_management_system.reservation.service.dto.ReservationCreationRequestDto;
import net.rajaonson.room_management_system.reservation.service.dto.ReservationResponseDto;
import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.repository.AvailabilitySlotRepository;
import net.rajaonson.room_management_system.room.repository.RoomRepository;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
@NullMarked
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;

    public ReservationService(ReservationRepository reservationRepository,
                              RoomRepository roomRepository,
                              AvailabilitySlotRepository availabilitySlotRepository,
                              AccountRepository accountRepository,
                              NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.accountRepository = accountRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReservationResponseDto createReservation(ReservationCreationRequestDto dto, String login) {
        Account requester = findAccount(login);
        Room room = roomRepository.findById(dto.roomId())
                .orElseThrow(() -> ApiErrors.notFound("no room with id %d".formatted(dto.roomId())));

        boolean covered = availabilitySlotRepository
                .existsByRoomIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                        room.getId(), dto.startAt(), dto.endAt());

        if (!covered) {
            throw ApiErrors.badRequest(
                    "no availability slot of this room covers the requested period");
        }

        boolean taken = reservationRepository
                .existsByRoomIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        room.getId(),
                        EnumSet.of(ReservationStatus.PENDING_APPROVAL, ReservationStatus.CONFIRMED),
                        dto.endAt(), dto.startAt());

        if (taken) {
            throw ApiErrors.conflict("the room is already booked for this period");
        }

        Reservation reservation = reservationRepository.save(
                new Reservation(room, requester, dto.startAt(), dto.endAt(), dto.purpose()));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            notificationService.sendReservationConfirmationEmail(requester.getEmail(), reservation);
        } else {
            notificationService.sendReservationPendingEmail(requester.getEmail(), reservation);
        }

        log.info("created reservation {} for room {} in status {}",
                reservation.getId(), room.getId(), reservation.getStatus());

        return ReservationResponseDto.from(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponseDto getReservation(Long id) {
        return ReservationResponseDto.from(findReservation(id));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> listReservations(@Nullable ReservationStatus status,
                                                        @Nullable Long roomId) {
        Specification<Reservation> spec = new ReservationSpecification()
                .status(status)
                .room(roomId);

        return reservationRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "startAt"))
                .stream()
                .map(ReservationResponseDto::from)
                .toList();
    }

    @Transactional
    public ReservationResponseDto cancelReservation(Long id, String login) {
        Reservation reservation = findReservation(id);
        Account currentAccount = findAccount(login);

        if (!reservation.isRequestedBy(currentAccount) && !currentAccount.isManager()) {
            throw ApiErrors.forbidden("only the requester or a manager may cancel this reservation");
        }

        reservation.cancel();
        reservationRepository.save(reservation);

        notificationService.sendReservationCancellationEmail(
                reservation.getRequester().getEmail(), reservation);

        log.info("cancelled reservation {}", id);

        return ReservationResponseDto.from(reservation);
    }

    @Transactional
    public ReservationResponseDto approveReservation(Long id) {
        Reservation reservation = findReservation(id);

        reservation.confirm();
        reservationRepository.save(reservation);

        notificationService.sendReservationConfirmationEmail(
                reservation.getRequester().getEmail(), reservation);

        log.info("approved reservation {}", id);

        return ReservationResponseDto.from(reservation);
    }

    @Transactional
    public ReservationResponseDto rejectReservation(Long id, String reason) {
        Reservation reservation = findReservation(id);

        reservation.reject();
        reservationRepository.save(reservation);

        notificationService.sendReservationRejectionEmail(
                reservation.getRequester().getEmail(), reservation, reason);

        log.info("rejected reservation {}", id);

        return ReservationResponseDto.from(reservation);
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> ApiErrors.notFound("no reservation with id %d".formatted(id)));
    }

    private Account findAccount(String login) {
        return accountRepository.findByLogin(login)
                .orElseThrow(() -> ApiErrors.unauthorized("the authenticated account no longer exists"));
    }
}
