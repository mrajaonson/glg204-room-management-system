package net.rajaonson.room_management_system.room.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import net.rajaonson.room_management_system.common.model.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "availability_slot")
public class AvailabilitySlot extends BaseEntity {

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    protected AvailabilitySlot() {}

    public AvailabilitySlot(LocalDateTime startAt, LocalDateTime endAt, Room room) {
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("end %s must be after start %s".formatted(endAt, startAt));
        }
        this.startAt = startAt;
        this.endAt = endAt;
        this.room = room;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public Room getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return "AvailabilitySlot[id=%s, startAt=%s, endAt=%s, roomId=%s]"
                .formatted(getId(), startAt, endAt, room.getId());
    }
}
