package com.namudev.identity_service.repository;

import com.namudev.identity_service.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface InvalidatedTokenRepo extends JpaRepository<InvalidatedToken, String> {
    @Modifying
    @Query("DELETE FROM InvalidatedToken it WHERE it.expirationDate < :now")
    int deleteAllExpiredTokens(@Param("now") Instant now);
}
