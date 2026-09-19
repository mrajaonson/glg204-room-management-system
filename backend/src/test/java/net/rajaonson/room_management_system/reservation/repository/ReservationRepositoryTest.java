package net.rajaonson.room_management_system.reservation.repository;

import net.rajaonson.room_management_system.PostgresTest;
import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.account.model.Role;
import net.rajaonson.room_management_system.reservation.model.Reservation;
import net.rajaonson.room_management_system.reservation.model.ReservationStatus;
import net.rajaonson.room_management_system.reservation.service.ReservationSpecification;
import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.model.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationRepositoryTest extends PostgresTest {

    private static final EnumSet<ReservationStatus> ACTIVE =
            EnumSet.of(ReservationStatus.PENDING_APPROVAL, ReservationStatus.CONFIRMED);

    private static final LocalDateTime NINE = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime TEN = LocalDateTime.of(2026, 10, 1, 10, 0);
    private static final LocalDateTime ELEVEN = LocalDateTime.of(2026, 10, 1, 11, 0);
    private static final LocalDateTime NOON = LocalDateTime.of(2026, 10, 1, 12, 0);

    @Autowired
    private ReservationRepository repository;
    @Autowired
    private TestEntityManager entityManager;

    private Room room;
    private Account requester;

    @BeforeEach
    void setUp() {
        room = entityManager.persist(
                new Room("B101", "Building B", 30, RoomType.CLASSROOM, null, false));
        requester = entityManager.persist(
                new Account("alice", "hash", "alice@example.org", Role.USER));
        // booked from 10:00 to 11:00, CONFIRMED since the room needs no approval
        entityManager.persistAndFlush(new Reservation(room, requester, TEN, ELEVEN, "course"));
    }

    @Test
    void findsAnOverlappingReservation() {
        assertThat(overlaps(NINE, ELEVEN)).isTrue();
        assertThat(overlaps(TEN.plusMinutes(15), TEN.plusMinutes(30))).isTrue();
        assertThat(overlaps(TEN.plusMinutes(30), NOON)).isTrue();
    }

    @Test
    void ignoresAnAdjacentPeriod() {
        assertThat(overlaps(NINE, TEN)).isFalse();
        assertThat(overlaps(ELEVEN, NOON)).isFalse();
    }

    @Test
    void ignoresACancelledOrRejectedReservation() {
        Reservation cancelled = new Reservation(room, requester, ELEVEN, NOON, "meeting");
        cancelled.cancel();
        Reservation rejected = new Reservation(room, requester, NOON, NOON.plusHours(1), "meeting");
        rejected.reject();
        entityManager.persist(cancelled);
        entityManager.persistAndFlush(rejected);

        assertThat(overlaps(ELEVEN, NOON.plusHours(1))).isFalse();
    }

    @Test
    void ignoresTheReservationsOfAnotherRoom() {
        Room other = entityManager.persistAndFlush(
                new Room("B102", "Building B", 20, RoomType.MEETING, null, false));

        assertThat(repository.existsByRoomIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                other.getId(), ACTIVE, ELEVEN, TEN)).isFalse();
    }

    @Test
    void filtersByStatus() {
        Reservation cancelled = new Reservation(room, requester, ELEVEN, NOON, "meeting");
        cancelled.cancel();
        entityManager.persistAndFlush(cancelled);

        List<Reservation> found = repository.findAll(
                new ReservationSpecification().status(ReservationStatus.CANCELLED),
                Sort.by(Sort.Direction.DESC, "startAt"));

        assertThat(found).extracting(Reservation::getStatus)
                .containsExactly(ReservationStatus.CANCELLED);
    }

    @Test
    void filtersByRoomAndSortsByStartDescending() {
        Room other = entityManager.persist(
                new Room("B102", "Building B", 20, RoomType.MEETING, null, false));
        entityManager.persist(new Reservation(other, requester, TEN, ELEVEN, "meeting"));
        entityManager.persistAndFlush(new Reservation(room, requester, ELEVEN, NOON, "course"));

        List<Reservation> found = repository.findAll(
                new ReservationSpecification().room(room.getId()),
                Sort.by(Sort.Direction.DESC, "startAt"));

        assertThat(found).extracting(Reservation::getStartAt).containsExactly(ELEVEN, TEN);
        assertThat(found).allSatisfy(
                reservation -> assertThat(reservation.getRoom().getId()).isEqualTo(room.getId()));
    }

    @Test
    void appliesNoFilterWhenNoneIsSet() {
        List<Reservation> found = repository.findAll(
                new ReservationSpecification().status(null).room(null),
                Sort.by(Sort.Direction.DESC, "startAt"));

        assertThat(found).hasSize(1);
    }

    private boolean overlaps(LocalDateTime start, LocalDateTime end) {
        return repository.existsByRoomIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                room.getId(), ACTIVE, end, start);
    }
}
