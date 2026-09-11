package net.rajaonson.room_management_system.room.controller;

import jakarta.validation.Valid;
import net.rajaonson.room_management_system.room.service.RoomService;
import net.rajaonson.room_management_system.room.service.dto.AvailabilitySlotCreationRequestDto;
import net.rajaonson.room_management_system.room.service.dto.AvailabilitySlotResponseDto;
import net.rajaonson.room_management_system.room.service.dto.EquipmentCreationRequestDto;
import net.rajaonson.room_management_system.room.service.dto.EquipmentResponseDto;
import net.rajaonson.room_management_system.room.service.dto.RoomCreationRequestDto;
import net.rajaonson.room_management_system.room.service.dto.RoomResponseDto;
import net.rajaonson.room_management_system.room.service.dto.RoomSearchFilterDto;
import net.rajaonson.room_management_system.room.service.dto.RoomUpdateRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponseDto createRoom(@Valid @RequestBody RoomCreationRequestDto requestDto) {
        return roomService.createRoom(requestDto);
    }

    @GetMapping
    public List<RoomResponseDto> listRooms() {
        return roomService.listRooms();
    }

    @GetMapping("/{roomId}")
    public RoomResponseDto getRoom(@PathVariable Long roomId) {
        return roomService.getRoom(roomId);
    }

    @PutMapping("/{roomId}")
    public RoomResponseDto updateRoom(@PathVariable Long roomId, @Valid @RequestBody RoomUpdateRequestDto requestDto) {
        return roomService.updateRoom(roomId, requestDto);
    }

    @PostMapping("/search")
    public List<RoomResponseDto> searchRooms(@Valid @RequestBody RoomSearchFilterDto filterDto) {
        return roomService.searchRooms(filterDto);
    }

    @PostMapping("/{roomId}/equipments")
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentResponseDto createEquipment(@PathVariable Long roomId, @Valid @RequestBody EquipmentCreationRequestDto requestDto) {
        return roomService.createEquipment(roomId, requestDto);
    }

    @PostMapping("/{roomId}/availabilities")
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilitySlotResponseDto addAvailabilitySlot(
            @PathVariable Long roomId,
            @Valid @RequestBody AvailabilitySlotCreationRequestDto requestDto) {
        return roomService.addAvailabilitySlot(roomId, requestDto);
    }

    @GetMapping("/{roomId}/availabilities")
    public List<AvailabilitySlotResponseDto> listAvailabilitySlots(@PathVariable Long roomId) {
        return roomService.listAvailabilitySlots(roomId);
    }
}
