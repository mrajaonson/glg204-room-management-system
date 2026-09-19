package net.rajaonson.room_management_system.room.repository;

import net.rajaonson.room_management_system.PostgresTest;
import net.rajaonson.room_management_system.room.model.AvailabilitySlot;
import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.model.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AvailabilitySlotRepositoryTest extends PostgresTest {

    private static final LocalDateTime NINE = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime ELEVEN = LocalDateTime.of(2026, 10, 1, 11, 0);
    private static final LocalDateTime NOON = LocalDateTime.of(2026, 10, 1, 12, 0);

    @Autowired
    private AvailabilitySlotRepository repository;
    @Autowired
    private TestEntityManager entityManager;

    private Long roomId;

    @BeforeEach
    void setUp() {
        Room room = entityManager.persistAndFlush(
                new Room("B101", "Building B", 30, RoomType.CLASSROOM, null, false));
        roomId = room.getId();
        // the room is open from 9:00 to 12:00
        entityManager.persistAndFlush(new AvailabilitySlot(NINE, NOON, room));
    }

    @Test
    void coversAPeriodInsideTheSlot() {
        assertThat(repository.existsByRoomIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                roomId, NINE.plusMinutes(30), ELEVEN)).isTrue();
    }

    @Test
    void coversAPeriodMatchingTheSlotExactly() {
        assertThat(repository.existsByRoomIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                roomId, NINE, NOON)).isTrue();
    }

    @Test
    void doesNotCoverAPeriodRunningPastTheSlot() {
        assertThat(repository.existsByRoomIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                roomId, ELEVEN, NOON.plusHours(1))).isFalse();
    }

    @Test
    void doesNotCoverAPeriodOfAnotherRoom() {
        Room other = entityManager.persistAndFlush(
                new Room("B102", "Building B", 20, RoomType.MEETING, null, false));

        assertThat(repository.existsByRoomIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                other.getId(), NINE, ELEVEN)).isFalse();
    }

    @Test
    void detectsAnOverlappingSlot() {
        assertThat(repository.existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(
                roomId, NOON.plusHours(1), ELEVEN)).isTrue();
    }

    @Test
    void doesNotDetectAnOverlapForAnAdjacentSlot() {
        // a slot starting exactly when the existing one ends must be allowed
        assertThat(repository.existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(
                roomId, NOON.plusHours(2), NOON)).isFalse();
    }

    @Test
    void listsTheSlotsOfARoomByStart() {
        Room room = entityManager.find(Room.class, roomId);
        entityManager.persistAndFlush(new AvailabilitySlot(NINE.minusDays(1), NOON.minusDays(1), room));

        assertThat(repository.findByRoomIdOrderByStartAt(roomId))
                .extracting(AvailabilitySlot::getStartAt)
                .containsExactly(NINE.minusDays(1), NINE);
    }
}
