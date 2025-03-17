package com.eloir.wallet.repository;

import com.eloir.wallet.entity.Transaction;
import com.eloir.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletAndCreatedAtBetween(Wallet wallet, LocalDateTime atStartOfDay, LocalDateTime atStartOfDay1);
}
