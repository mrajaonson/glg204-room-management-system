package net.rajaonson.room_management_system.room.repository;


import net.rajaonson.room_management_system.room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
