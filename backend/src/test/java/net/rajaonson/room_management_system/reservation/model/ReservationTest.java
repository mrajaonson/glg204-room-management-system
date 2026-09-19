package net.rajaonson.room_management_system.reservation.model;

import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.account.model.Role;
import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.model.RoomType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 1, 11, 0);

    private final Account requester = account(1L);

    @Test
    void isConfirmedWhenTheRoomNeedsNoApproval() {
        Reservation reservation = new Reservation(room(false), requester, START, END, "course");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void isPendingWhenTheRoomRequiresApproval() {
        Reservation reservation = new Reservation(room(true), requester, START, END, "course");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING_APPROVAL);
    }

    @Test
    void rejectsAnEndNotAfterTheStart() {
        assertThatThrownBy(() -> new Reservation(room(false), requester, END, START, "course"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Reservation(room(false), requester, START, START, "course"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changesStatusOnConfirmRejectAndCancel() {
        Reservation reservation = new Reservation(room(true), requester, START, END, "course");

        reservation.confirm();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        reservation.reject();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REJECTED);

        reservation.cancel();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void recognisesItsRequesterById() {
        Reservation reservation = new Reservation(room(false), requester, START, END, "course");

        assertThat(reservation.isRequestedBy(account(1L))).isTrue();
        assertThat(reservation.isRequestedBy(account(2L))).isFalse();
    }

    private static Room room(boolean requiresApproval) {
        Room room = new Room("B101", "Building B", 30, RoomType.CLASSROOM, "a room", requiresApproval);
        ReflectionTestUtils.setField(room, "id", 10L);
        return room;
    }

    private static Account account(Long id) {
        Account account = new Account("user" + id, "hash", "user" + id + "@example.org", Role.USER);
        ReflectionTestUtils.setField(account, "id", id);
        return account;
    }
}
