package net.rajaonson.room_management_system.account.repository;

import net.rajaonson.room_management_system.account.model.AccountCreationRequest;
import net.rajaonson.room_management_system.account.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountCreationRequestRepository extends JpaRepository<AccountCreationRequest, Long> {

    Optional<AccountCreationRequest> findByValidationToken(String validationToken);

    List<AccountCreationRequest> findByStatusNotInOrderByCreatedAtDesc(Collection<RequestStatus> statuses);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);
}
