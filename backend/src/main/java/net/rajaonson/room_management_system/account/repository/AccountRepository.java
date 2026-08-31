package net.rajaonson.room_management_system.account.repository;

import net.rajaonson.room_management_system.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    Optional<Account> findByLogin(String login);
}
