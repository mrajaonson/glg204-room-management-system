package net.rajaonson.room_management_system.room.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import net.rajaonson.room_management_system.common.model.BaseEntity;

@Entity
@Table(name = "room")
public class Room extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RoomType type;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "reservation_requires_approval", nullable = false)
    private boolean reservationRequiresApproval;

    protected Room() {}

    public Room(String name, String location, int capacity, RoomType type, String description,
                boolean reservationRequiresApproval) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.type = type;
        this.description = description;
        this.reservationRequiresApproval = reservationRequiresApproval;
    }

    public void update(String name, String location, int capacity, RoomType type, String description,
                       boolean reservationRequiresApproval) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.type = type;
        this.description = description;
        this.reservationRequiresApproval = reservationRequiresApproval;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public RoomType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isReservationRequiresApproval() {
        return reservationRequiresApproval;
    }

    @Override
    public String toString() {
        return "Room[id=%s, name=%s, location=%s, capacity=%d, type=%s]"
                .formatted(getId(), name, location, capacity, type);
    }
}
