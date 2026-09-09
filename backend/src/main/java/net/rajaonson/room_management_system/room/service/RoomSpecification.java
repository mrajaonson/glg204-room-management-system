package net.rajaonson.room_management_system.room.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import net.rajaonson.room_management_system.reservation.model.Reservation;
import net.rajaonson.room_management_system.reservation.model.ReservationStatus;
import net.rajaonson.room_management_system.room.model.AvailabilitySlot;
import net.rajaonson.room_management_system.room.model.Equipment;
import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.model.RoomType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class RoomSpecification implements Specification<Room> {

    private final transient List<Specification<Room>> specifications = new ArrayList<>();

    public RoomSpecification nameContains(@Nullable String name) {
        return add(name, v -> (root, query, cb) -> cb.like(cb.lower(root.get("name")), contains(v)));
    }

    public RoomSpecification locationContains(@Nullable String location) {
        return add(location, v -> (root, query, cb) -> cb.like(cb.lower(root.get("location")), contains(v)));
    }

    public RoomSpecification capacityAtLeast(@Nullable Integer min) {
        return add(min, v -> (root, query, cb) -> cb.ge(root.<Integer>get("capacity"), v));
    }

    public RoomSpecification capacityAtMost(@Nullable Integer max) {
        return add(max, v -> (root, query, cb) -> cb.le(root.<Integer>get("capacity"), v));
    }

    public RoomSpecification type(@Nullable RoomType type) {
        return add(type, v -> (root, query, cb) -> cb.equal(root.get("type"), v));
    }

    public RoomSpecification hasAllEquipments(@Nullable List<String> names) {
        return add(names, v -> (root, query, cb) ->
                cb.ge(matchingEquipmentCount(root, query, cb, v), v.stream().distinct().count()));
    }

    public RoomSpecification availableBetween(@Nullable LocalDateTime start, @Nullable LocalDateTime end) {
        if (start == null || end == null) {
            return this;
        }
        specifications.add((root, query, cb) -> cb.and(
                cb.exists(coveringSlot(root, query, cb, start, end)),
                cb.not(cb.exists(activeReservationOverlapping(root, query, cb, start, end)))));
        return this;
    }

    @Override
    public @Nullable Predicate toPredicate(@NonNull Root<Room> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder cb) {
        return Specification.allOf(specifications).toPredicate(root, query, cb);
    }

    private <V> RoomSpecification add(@Nullable V value, Function<V, Specification<Room>> specification) {
        if (value != null) {
            specifications.add(specification.apply(value));
        }
        return this;
    }

    private static String contains(String value) {
        return "%" + value.toLowerCase(Locale.ROOT) + "%";
    }

    private static Subquery<Long> matchingEquipmentCount(
            Root<Room> room, CriteriaQuery<?> query, CriteriaBuilder cb, List<String> names) {
        Subquery<Long> count = query.subquery(Long.class);
        Root<Equipment> equipment = count.from(Equipment.class);
        return count.select(cb.countDistinct(equipment.get("name")))
                .where(cb.equal(equipment.get("room"), room),
                       equipment.get("name").in(names));
    }

    private static Subquery<Long> coveringSlot(
            Root<Room> room, CriteriaQuery<?> query, CriteriaBuilder cb,
            LocalDateTime start, LocalDateTime end) {
        Subquery<Long> covering = query.subquery(Long.class);
        Root<AvailabilitySlot> slot = covering.from(AvailabilitySlot.class);
        return covering.select(cb.literal(1L))
                .where(cb.equal(slot.get("room"), room),
                       cb.lessThanOrEqualTo(slot.get("startAt"), start),
                       cb.greaterThanOrEqualTo(slot.get("endAt"), end));
    }

    private static Subquery<Long> activeReservationOverlapping(
            Root<Room> room, CriteriaQuery<?> query, CriteriaBuilder cb,
            LocalDateTime start, LocalDateTime end) {
        Subquery<Long> conflicting = query.subquery(Long.class);
        Root<Reservation> reservation = conflicting.from(Reservation.class);
        return conflicting.select(cb.literal(1L))
                .where(cb.equal(reservation.get("room"), room),
                       reservation.get("status").in(EnumSet.of(
                               ReservationStatus.PENDING_APPROVAL, ReservationStatus.CONFIRMED)),
                       cb.lessThan(reservation.get("startAt"), end),
                       cb.greaterThan(reservation.get("endAt"), start));
    }
}
