package com.eloir.wallet.strategy.impl;

import com.eloir.wallet.dto.WalletOperationRequest;
import com.eloir.wallet.entity.Transaction;
import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.enums.TransactionType;
import com.eloir.wallet.exception.WalletLockedException;
import com.eloir.wallet.repository.TransactionRepository;
import com.eloir.wallet.repository.WalletRepository;
import com.eloir.wallet.strategy.WalletOperationStrategy;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class DepositStrategy implements WalletOperationStrategy {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public DepositStrategy(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.DEPOSIT;
    }

    @Override
    @CircuitBreaker(name = "walletService", fallbackMethod = "fallbackDeposit")
    @Retry(name = "walletService", fallbackMethod = "retryFallback")
    @Transactional
    public void execute(WalletOperationRequest request) {
        log.info("Deposit operation started for userId: {} with amount: {}", request.userId(), request.amount());

        try {
            Wallet wallet = walletRepository.findByUserId(request.userId())
                    .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

            BigDecimal newBalance = wallet.getBalance().add(request.amount());
            wallet.setBalance(newBalance);

            Transaction transaction = new Transaction(wallet, TransactionType.DEPOSIT, request.amount(), newBalance);
            transactionRepository.save(transaction);

            walletRepository.save(wallet);

            log.info("Deposit operation successful for userId: {} with amount: {}", request.userId(), request.amount());
        } catch (EntityNotFoundException ex) {
            log.error("Wallet not found: {}", ex.getMessage());
            throw ex;
        } catch (PessimisticLockException | LockTimeoutException ex) {
            log.error("Wallet is locked for userId: {} - Error: {}", request.userId(), ex.getMessage());
            throw new WalletLockedException("The wallet is temporarily locked due to another operation. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error during deposit for userId: {} - Error: {}", request.userId(), e.getMessage(), e);
            throw e;
        }
    }

    public void fallbackDeposit(WalletOperationRequest request, Throwable t) {
        log.error("Fallback method invoked due to error: {}", t.getMessage());
        log.error("userId {} and amount: {} ", request.userId(), request.amount());
        throw new RuntimeException("Deposit service is temporarily unavailable. Please try again later.");
    }

    public void retryFallback(WalletOperationRequest request, Throwable t) {
        log.error("Retry fallback method invoked due to error: {}", t.getMessage());
        log.error("userId {} and amount: {} ", request.userId(), request.amount());
        throw new RuntimeException("The deposit operation failed after multiple retries. Please try again later.");
    }
}

