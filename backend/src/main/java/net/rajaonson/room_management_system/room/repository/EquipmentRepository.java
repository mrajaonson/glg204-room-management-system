package net.rajaonson.room_management_system.room.repository;

import net.rajaonson.room_management_system.room.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByRoomIdOrderByName(Long roomId);
}
