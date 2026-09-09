package net.rajaonson.room_management_system.room.repository;

import net.rajaonson.room_management_system.room.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    boolean existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(
            Long roomId, LocalDateTime end, LocalDateTime start);
}
