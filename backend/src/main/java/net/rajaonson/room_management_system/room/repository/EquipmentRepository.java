package net.rajaonson.room_management_system.room.repository;

import net.rajaonson.room_management_system.room.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
}
