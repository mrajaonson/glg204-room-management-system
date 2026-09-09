package net.rajaonson.room_management_system.room.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import net.rajaonson.room_management_system.common.model.BaseEntity;

@Entity
@Table(name = "equipment")
public class Equipment extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    protected Equipment() {}

    public Equipment(String name, String description, Room room) {
        this.name = name;
        this.description = description;
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Room getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return "Equipment[id=%s, name=%s, roomId=%s]".formatted(getId(), name, room.getId());
    }
}
