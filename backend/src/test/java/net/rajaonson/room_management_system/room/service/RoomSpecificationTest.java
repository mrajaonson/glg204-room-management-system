package net.rajaonson.room_management_system.room.service;

import net.rajaonson.room_management_system.PostgresTest;
import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.account.model.Role;
import net.rajaonson.room_management_system.reservation.model.Reservation;
import net.rajaonson.room_management_system.room.model.AvailabilitySlot;
import net.rajaonson.room_management_system.room.model.Equipment;
import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.model.RoomType;
import net.rajaonson.room_management_system.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoomSpecificationTest extends PostgresTest {

    private static final LocalDateTime NINE = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime TEN = LocalDateTime.of(2026, 10, 1, 10, 0);
    private static final LocalDateTime ELEVEN = LocalDateTime.of(2026, 10, 1, 11, 0);
    private static final LocalDateTime NOON = LocalDateTime.of(2026, 10, 1, 12, 0);

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Room classroom;
    private Room lab;

    @BeforeEach
    void setUp() {
        classroom = entityManager.persist(
                new Room("Amphi Grenat", "Building A", 120, RoomType.CLASSROOM, null, false));
        lab = entityManager.persist(
                new Room("Lab Reseau", "Building B", 20, RoomType.LAB, null, true));

        entityManager.persist(new Equipment("projector", null, classroom));
        entityManager.persist(new Equipment("whiteboard", null, classroom));
        entityManager.persist(new Equipment("projector", null, lab));

        entityManager.flush();
    }

    @Test
    void filtersByNameIgnoringCase() {
        List<Room> found = roomRepository.findAll(new RoomSpecification().nameContains("grenat"));

        assertThat(found).containsExactly(classroom);
    }

    @Test
    void filtersByLocation() {
        List<Room> found = roomRepository.findAll(new RoomSpecification().locationContains("Building B"));

        assertThat(found).containsExactly(lab);
    }

    @Test
    void filtersByCapacityRange() {
        assertThat(roomRepository.findAll(new RoomSpecification().capacityAtLeast(50)))
                .containsExactly(classroom);
        assertThat(roomRepository.findAll(new RoomSpecification().capacityAtMost(50)))
                .containsExactly(lab);
        assertThat(roomRepository.findAll(
                new RoomSpecification().capacityAtLeast(10).capacityAtMost(200)))
                .containsExactlyInAnyOrder(classroom, lab);
    }

    @Test
    void filtersByType() {
        List<Room> found = roomRepository.findAll(new RoomSpecification().type(RoomType.LAB));

        assertThat(found).containsExactly(lab);
    }

    @Test
    void keepsOnlyTheRoomsHavingEveryRequestedEquipment() {
        assertThat(roomRepository.findAll(
                new RoomSpecification().hasAllEquipments(List.of("projector"))))
                .containsExactlyInAnyOrder(classroom, lab);

        assertThat(roomRepository.findAll(
                new RoomSpecification().hasAllEquipments(List.of("projector", "whiteboard"))))
                .containsExactly(classroom);
    }

    @Test
    void combinesTheFilters() {
        List<Room> found = roomRepository.findAll(new RoomSpecification()
                .locationContains("Building")
                .capacityAtLeast(10)
                .type(RoomType.CLASSROOM)
                .hasAllEquipments(List.of("whiteboard")));

        assertThat(found).containsExactly(classroom);
    }

    @Test
    void keepsARoomOpenOverThePeriodAndNotYetBooked() {
        entityManager.persistAndFlush(new AvailabilitySlot(NINE, NOON, classroom));

        List<Room> found = roomRepository.findAll(new RoomSpecification().availableBetween(TEN, ELEVEN));

        assertThat(found).containsExactly(classroom);
    }

    @Test
    void dropsARoomWhoseSlotDoesNotCoverTheWholePeriod() {
        entityManager.persistAndFlush(new AvailabilitySlot(NINE, TEN, classroom));

        List<Room> found = roomRepository.findAll(new RoomSpecification().availableBetween(TEN, ELEVEN));

        assertThat(found).isEmpty();
    }

    @Test
    void dropsARoomAlreadyBookedOverThePeriod() {
        entityManager.persist(new AvailabilitySlot(NINE, NOON, classroom));
        // classroom needs no approval, so the reservation is CONFIRMED
        entityManager.persistAndFlush(
                new Reservation(classroom, requester(), TEN.minusMinutes(30), ELEVEN, "course"));

        List<Room> found = roomRepository.findAll(new RoomSpecification().availableBetween(TEN, ELEVEN));

        assertThat(found).isEmpty();
    }

    @Test
    void keepsARoomWhoseOverlappingReservationWasCancelled() {
        entityManager.persist(new AvailabilitySlot(NINE, NOON, classroom));
        Reservation reservation =
                new Reservation(classroom, requester(), TEN, ELEVEN, "course");
        reservation.cancel();
        entityManager.persistAndFlush(reservation);

        List<Room> found = roomRepository.findAll(new RoomSpecification().availableBetween(TEN, ELEVEN));

        assertThat(found).containsExactly(classroom);
    }

    @Test
    void keepsARoomWhoseReservationOnlyTouchesThePeriod() {
        entityManager.persist(new AvailabilitySlot(NINE, NOON, classroom));
        // ends exactly when the searched period starts
        entityManager.persistAndFlush(
                new Reservation(classroom, requester(), NINE, TEN, "course"));

        List<Room> found = roomRepository.findAll(new RoomSpecification().availableBetween(TEN, ELEVEN));

        assertThat(found).containsExactly(classroom);
    }

    @Test
    void returnsEveryRoomWhenNoFilterIsSet() {
        assertThat(roomRepository.findAll(new RoomSpecification()))
                .containsExactlyInAnyOrder(classroom, lab);
    }

    private Account requester() {
        return entityManager.persist(new Account("alice", "hash", "alice@example.org", Role.USER));
    }
}
