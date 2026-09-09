package net.rajaonson.room_management_system.reservation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import net.rajaonson.room_management_system.account.model.Account;
import net.rajaonson.room_management_system.common.model.BaseEntity;
import net.rajaonson.room_management_system.room.model.Room;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
public class Reservation extends BaseEntity {

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "purpose", nullable = false, length = 500)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private Account requester;

    protected Reservation() {}

    public Reservation(Room room, Account requester, LocalDateTime startAt, LocalDateTime endAt,
                       String purpose) {
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException(
                    "end %s must be after start %s".formatted(endAt, startAt));
        }
        this.room = room;
        this.requester = requester;
        this.startAt = startAt;
        this.endAt = endAt;
        this.purpose = purpose;
        this.status = room.isReservationRequiresApproval()
                ? ReservationStatus.PENDING_APPROVAL
                : ReservationStatus.CONFIRMED;
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void reject() {
        this.status = ReservationStatus.REJECTED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPurpose() {
        return purpose;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Room getRoom() {
        return room;
    }

    public Account getRequester() {
        return requester;
    }

    public boolean isRequestedBy(Account account) {
        return requester.getId().equals(account.getId());
    }

    @Override
    public String toString() {
        return "Reservation[id=%s, startAt=%s, endAt=%s, status=%s, roomId=%s]"
                .formatted(getId(), startAt, endAt, status, room.getId());
    }
}
