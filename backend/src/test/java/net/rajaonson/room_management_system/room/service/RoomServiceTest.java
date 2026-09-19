package net.rajaonson.room_management_system.room.service;

import net.rajaonson.room_management_system.TestEntities;
import net.rajaonson.room_management_system.room.model.AvailabilitySlot;
import net.rajaonson.room_management_system.room.model.Equipment;
import net.rajaonson.room_management_system.room.model.Room;
import net.rajaonson.room_management_system.room.model.RoomType;
import net.rajaonson.room_management_system.room.repository.AvailabilitySlotRepository;
import net.rajaonson.room_management_system.room.repository.EquipmentRepository;
import net.rajaonson.room_management_system.room.repository.RoomRepository;
import net.rajaonson.room_management_system.room.service.dto.AvailabilitySlotCreationRequestDto;
import net.rajaonson.room_management_system.room.service.dto.EquipmentCreationRequestDto;
import net.rajaonson.room_management_system.room.service.dto.RoomCreationRequestDto;
import net.rajaonson.room_management_system.room.service.dto.RoomResponseDto;
import net.rajaonson.room_management_system.room.service.dto.RoomUpdateRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final long ROOM_ID = 10L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 1, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 1, 11, 0);

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @InjectMocks
    private RoomService service;

    @Test
    void createsARoom() {
        when(roomRepository.existsByName("B101")).thenReturn(false);
        when(roomRepository.save(any(Room.class)))
                .thenAnswer(invocation -> TestEntities.withId(invocation.getArgument(0), ROOM_ID));

        RoomResponseDto response = service.createRoom(new RoomCreationRequestDto(
                "B101", "Building B", 30, RoomType.CLASSROOM, "a room", true));

        assertThat(response.id()).isEqualTo(ROOM_ID);
        assertThat(response.name()).isEqualTo("B101");
        assertThat(response.capacity()).isEqualTo(30);
    }

    @Test
    void refusesADuplicateRoomName() {
        when(roomRepository.existsByName("B101")).thenReturn(true);

        assertThatThrownBy(() -> service.createRoom(new RoomCreationRequestDto(
                "B101", "Building B", 30, RoomType.CLASSROOM, "a room", true)))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void refusesAnUnknownRoom() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRoom(ROOM_ID))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updatesARoom() {
        Room room = TestEntities.room(ROOM_ID, false);
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(roomRepository.existsByNameAndIdNot("B102", ROOM_ID)).thenReturn(false);

        RoomResponseDto response = service.updateRoom(ROOM_ID, new RoomUpdateRequestDto(
                "B102", "Building C", 40, RoomType.LAB, "a lab", true));

        assertThat(response.name()).isEqualTo("B102");
        assertThat(room.getCapacity()).isEqualTo(40);
        assertThat(room.isReservationRequiresApproval()).isTrue();
        verify(roomRepository).save(room);
    }

    @Test
    void refusesARoomNameAlreadyUsedByAnotherRoom() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(TestEntities.room(ROOM_ID, false)));
        when(roomRepository.existsByNameAndIdNot("B102", ROOM_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.updateRoom(ROOM_ID, new RoomUpdateRequestDto(
                "B102", "Building C", 40, RoomType.LAB, "a lab", true)))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(roomRepository, never()).save(any());
    }

    @Test
    void addsAnAvailabilitySlot() {
        Room room = TestEntities.room(ROOM_ID, false);
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(availabilitySlotRepository
                .existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(ROOM_ID, END, START))
                .thenReturn(false);
        when(availabilitySlotRepository.save(any(AvailabilitySlot.class)))
                .thenAnswer(invocation -> TestEntities.withId(invocation.getArgument(0), 7L));

        var response = service.addAvailabilitySlot(ROOM_ID,
                new AvailabilitySlotCreationRequestDto(START, END));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.startAt()).isEqualTo(START);
        assertThat(response.endAt()).isEqualTo(END);
    }

    @Test
    void refusesAnAvailabilitySlotOverlappingAnotherOne() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(TestEntities.room(ROOM_ID, false)));
        when(availabilitySlotRepository
                .existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(ROOM_ID, END, START))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addAvailabilitySlot(ROOM_ID,
                new AvailabilitySlotCreationRequestDto(START, END)))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(availabilitySlotRepository, never()).save(any());
    }

    @Test
    void createsAnEquipmentInItsRoom() {
        Room room = TestEntities.room(ROOM_ID, false);
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(equipmentRepository.save(any(Equipment.class)))
                .thenAnswer(invocation -> TestEntities.withId(invocation.getArgument(0), 3L));

        var response = service.createEquipment(ROOM_ID,
                new EquipmentCreationRequestDto("projector", "4K"));

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.name()).isEqualTo("projector");
    }

    @Test
    void listsEquipmentsOfAKnownRoomOnly() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listEquipments(ROOM_ID))
                .isInstanceOfSatisfying(ErrorResponseException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(equipmentRepository, never()).findByRoomIdOrderByName(any());
    }
}
