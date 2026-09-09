package net.rajaonson.room_management_system.room.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import net.rajaonson.room_management_system.room.model.RoomType;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public record RoomSearchFilterDto(
        @Nullable @Size(max = 100) String name,
        @Nullable @Size(max = 255) String location,
        @Nullable @Positive Integer capacityMin,
        @Nullable @Positive Integer capacityMax,
        @Nullable RoomType type,
        @Nullable List<@NotBlank @Size(max = 100) String> equipments,
        @Nullable LocalDateTime startAt,
        @Nullable LocalDateTime endAt) {

    public RoomSearchFilterDto {
        name = blankToNull(name);
        location = blankToNull(location);
        equipments = equipments == null || equipments.isEmpty() ? null : equipments.stream().toList();
    }

    @JsonIgnore
    @AssertTrue(message = "capacityMin must not exceed capacityMax")
    public boolean isCapacityRangeValid() {
        return capacityMin == null || capacityMax == null || capacityMin <= capacityMax;
    }

    @JsonIgnore
    @AssertTrue(message = "startAt and endAt must be given together, with endAt after startAt")
    public boolean isPeriodValid() {
        if (startAt == null && endAt == null) {
            return true;
        }
        return startAt != null && endAt != null && endAt.isAfter(startAt);
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
