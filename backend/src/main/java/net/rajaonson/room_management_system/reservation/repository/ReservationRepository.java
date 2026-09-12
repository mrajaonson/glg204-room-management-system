package net.rajaonson.room_management_system.reservation.repository;

import net.rajaonson.room_management_system.reservation.model.Reservation;
import net.rajaonson.room_management_system.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    boolean existsByRoomIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Long roomId, Collection<ReservationStatus> statuses, LocalDateTime end, LocalDateTime start);
}
