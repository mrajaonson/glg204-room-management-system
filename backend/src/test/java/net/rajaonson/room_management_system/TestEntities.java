package net.rajaonson.room_management_system;

import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.account.model.Role;
import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.model.RoomType;
import org.springframework.test.util.ReflectionTestUtils;

/** Builders for detached entities with an id, as they would come back from a repository. */
public final class TestEntities {

    private TestEntities() {}

    public static Room room(Long id, boolean reservationRequiresApproval) {
        Room room = new Room("B101", "Building B", 30, RoomType.CLASSROOM, "a room",
                reservationRequiresApproval);
        return withId(room, id);
    }

    public static Account account(Long id, String login, Role role) {
        Account account = new Account(login, "hash", login + "@example.org", role);
        return withId(account, id);
    }

    public static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
