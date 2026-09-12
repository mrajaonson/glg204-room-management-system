package net.rajaonson.room_management_system.reservation.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import net.rajaonson.room_management_system.reservation.model.Reservation;
import net.rajaonson.room_management_system.reservation.model.ReservationStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ReservationSpecification implements Specification<Reservation> {

    private final transient List<Specification<Reservation>> specifications = new ArrayList<>();

    public ReservationSpecification status(@Nullable ReservationStatus status) {
        return add(status, v -> (root, query, cb) -> cb.equal(root.get("status"), v));
    }

    public ReservationSpecification room(@Nullable Long roomId) {
        return add(roomId, v -> (root, query, cb) -> cb.equal(root.get("room").get("id"), v));
    }

    public ReservationSpecification requester(@Nullable Long accountId) {
        return add(accountId, v -> (root, query, cb) -> cb.equal(root.get("requester").get("id"), v));
    }

    @Override
    public @Nullable Predicate toPredicate(@NonNull Root<Reservation> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder cb) {
        return Specification.allOf(specifications).toPredicate(root, query, cb);
    }

    private <V> ReservationSpecification add(@Nullable V value, Function<V, Specification<Reservation>> specification) {
        if (value != null) {
            specifications.add(specification.apply(value));
        }
        return this;
    }
}
