package net.rajaonson.room_management_system.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import net.rajaonson.room_management_system.common.model.BaseEntity;

@Entity
@Table(name = "account")
public class Account extends BaseEntity {

    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    protected Account() {}

    public Account(String login, String passwordHash, String email, Role role) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
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

    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "Account[id=%s, login=%s, email=%s, role=%s]".formatted(getId(), login, email, role);
    }
}
