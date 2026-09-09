package net.rajaonson.room_management_system.room.repository;


import net.rajaonson.room_management_system.room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByName(String name);
}
