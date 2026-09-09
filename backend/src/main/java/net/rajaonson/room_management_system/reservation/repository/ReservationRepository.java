package net.rajaonson.room_management_system.reservation.repository;

import net.rajaonson.room_management_system.reservation.model.Reservation;
import net.rajaonson.room_management_system.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByRoomIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Long roomId, Collection<ReservationStatus> statuses, LocalDateTime end, LocalDateTime start);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findAllByOrderByStartAtDesc();
}
