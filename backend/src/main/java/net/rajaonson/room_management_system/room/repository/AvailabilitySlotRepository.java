package net.rajaonson.room_management_system.room.repository;

import net.rajaonson.room_management_system.room.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByRoomIdOrderByStartAt(Long roomId);

    boolean existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(
            Long roomId, LocalDateTime end, LocalDateTime start);

    boolean existsByRoomIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            Long roomId, LocalDateTime start, LocalDateTime end);
}
