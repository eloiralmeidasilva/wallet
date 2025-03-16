package com.eloir.wallet.repository;

import com.eloir.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByUserId(String userId);

    @Query(value = "SELECT w FROM Wallet w WHERE w.codAccount = :codAccount", nativeQuery = false)
    Optional<Wallet> findByCodWallet(@Param("codAccount") String codAccount);

    @Query("SELECT MAX(w.numAccount) FROM Wallet w")
    Integer findMaxNumAccount();
}

