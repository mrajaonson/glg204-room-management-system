package net.rajaonson.room_management_system.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import net.rajaonson.room_management_system.common.model.BaseEntity;
import org.apache.commons.lang3.NotImplementedException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "account_creation_request")
public class AccountCreationRequest extends BaseEntity {

    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "validation_token", nullable = false, unique = true, length = 36)
    private String validationToken;

    protected AccountCreationRequest() {}

    public AccountCreationRequest(String login, String passwordHash, String email) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.email = email;
        this.status = RequestStatus.CREATED;
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
        this.validationToken = UUID.randomUUID().toString();
    }

    public void markEmailSent() {
        throw new NotImplementedException("Not yet implemented");
    }

    public void markEmailValidated() {
        throw new NotImplementedException("Not yet implemented");
    }

    public void approve() {
        throw new NotImplementedException("Not yet implemented");
    }

    public void refuse() {
        throw new NotImplementedException("Not yet implemented");
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getValidationToken() {
        return validationToken;
    }

    @Override
    public String toString() {
        return "AccountCreationRequest[id=%s, login=%s, email=%s, status=%s, createdAt=%s]"
                .formatted(getId(), login, email, status, createdAt);
    }
}
