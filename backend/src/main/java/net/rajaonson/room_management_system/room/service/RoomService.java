package net.rajaonson.room_management_system.room.service;

import net.rajaonson.room_management_system.common.error.ApiErrors;
import net.rajaonson.room_management_system.room.model.AvailabilitySlot;
import net.rajaonson.room_management_system.room.model.Equipment;
import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.repository.AvailabilitySlotRepository;
import net.rajaonson.room_management_system.room.repository.EquipmentRepository;
import net.rajaonson.room_management_system.room.repository.RoomRepository;
import net.rajaonson.room_management_system.room.service.dto.AvailabilitySlotCreationRequestDto;
import net.rajaonson.room_management_system.room.service.dto.AvailabilitySlotResponseDto;
import net.rajaonson.room_management_system.room.service.dto.EquipmentCreationRequestDto;
import net.rajaonson.room_management_system.room.service.dto.EquipmentResponseDto;
import net.rajaonson.room_management_system.room.service.dto.RoomCreationRequestDto;
import net.rajaonson.room_management_system.room.service.dto.RoomSearchFilterDto;
import net.rajaonson.room_management_system.room.service.dto.RoomResponseDto;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@NullMarked
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    public RoomService(RoomRepository roomRepository,
                       EquipmentRepository equipmentRepository,
                       AvailabilitySlotRepository availabilitySlotRepository) {
        this.roomRepository = roomRepository;
        this.equipmentRepository = equipmentRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    public RoomResponseDto createRoom(RoomCreationRequestDto dto) {
        if (roomRepository.existsByName(dto.name())) {
            throw ApiErrors.conflict("a room named '%s' already exists".formatted(dto.name()));
        }

        Room room = roomRepository.save(new Room(
                dto.name(),
                dto.location(),
                dto.capacity(),
                dto.type(),
                dto.description(),
                dto.reservationRequiresApproval()));

        log.info("created room {}", room.getId());

        return RoomResponseDto.from(room);
    }

    public List<RoomResponseDto> listRooms() {
        return roomRepository.findAll()
                .stream()
                .map(RoomResponseDto::from)
                .toList();
    }

    public List<RoomResponseDto> searchRooms(RoomSearchFilterDto filter) {
        Specification<Room> spec = new RoomSpecification()
                .nameContains(filter.name())
                .locationContains(filter.location())
                .capacityAtLeast(filter.capacityMin())
                .capacityAtMost(filter.capacityMax())
                .type(filter.type())
                .hasAllEquipments(filter.equipments())
                .availableBetween(filter.startAt(), filter.endAt());

        return roomRepository.findAll(spec)
                .stream()
                .map(RoomResponseDto::from)
                .toList();
    }

    public EquipmentResponseDto createEquipment(Long roomId, EquipmentCreationRequestDto dto) {
        Room room = findRoom(roomId);

        Equipment equipment = equipmentRepository.save(
                new Equipment(dto.name(), dto.description(), room));

        log.info("created equipment {} in room {}", equipment.getId(), roomId);

        return EquipmentResponseDto.from(equipment);
    }

    public AvailabilitySlotResponseDto addAvailabilitySlot(Long roomId, AvailabilitySlotCreationRequestDto dto) {
        Room room = findRoom(roomId);

        if (availabilitySlotRepository.existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(
                roomId, dto.endAt(), dto.startAt())) {
            throw ApiErrors.conflict("this period overlaps an existing availability slot of the room");
        }

        AvailabilitySlot slot = availabilitySlotRepository.save(
                new AvailabilitySlot(dto.startAt(), dto.endAt(), room));

        log.info("added availability slot {} to room {}", slot.getId(), roomId);

        return AvailabilitySlotResponseDto.from(slot);
    }

    private Room findRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> ApiErrors.notFound("no room with id %d".formatted(roomId)));
    }
}
