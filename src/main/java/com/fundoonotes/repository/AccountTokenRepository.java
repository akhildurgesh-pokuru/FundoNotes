
package com.fundoonotes.repository;

import com.fundoonotes.entity.AccountToken;
import com.fundoonotes.entity.AccountTokenType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface AccountTokenRepository
        extends JpaRepository<AccountToken, Long> {

    // Lock the token row while consuming it to prevent concurrent reuse.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AccountToken> findByTokenHashAndType(
            String tokenHash,
            AccountTokenType type
    );
}
