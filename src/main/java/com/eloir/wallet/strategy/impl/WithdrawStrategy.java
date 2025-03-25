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
public class WithdrawStrategy implements WalletOperationStrategy {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WithdrawStrategy(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.WITHDRAW;
    }

    @Override
    @CircuitBreaker(name = "walletService", fallbackMethod = "fallbackWithdraw")
    @Retry(name = "walletService", fallbackMethod = "retryFallback")
    @Transactional
    public void execute(WalletOperationRequest request) {
        log.info("Withdraw operation started for userId: {} with amount: {}", request.userId(), request.amount());

        try {
            Wallet wallet = walletRepository.findByUserId(request.userId())
                    .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

            if (wallet.getBalance().compareTo(request.amount()) < 0) {
                throw new IllegalArgumentException("Insufficient balance.");
            }

            BigDecimal newBalance = wallet.getBalance().subtract(request.amount());
            wallet.setBalance(newBalance);

            Transaction transaction = new Transaction(wallet, request.transactionType(), request.amount(), newBalance);
            transactionRepository.save(transaction);

            walletRepository.save(wallet);

            log.info("Withdraw operation successful for userId: {} with amount: {}", request.userId(), request.amount());

        } catch (EntityNotFoundException ex) {
            log.error("Wallet not found: {}", ex.getMessage());
            throw ex;
        } catch (PessimisticLockException | LockTimeoutException ex) {
            log.error("Wallet is locked for userId: {} - Error: {}", request.userId(), ex.getMessage());
            throw new WalletLockedException("The wallet is temporarily locked due to another operation. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error during withdraw for userId: {} - Error: {}", request.userId(), e.getMessage(), e);
            throw e;
        }
    }

    public void fallbackWithdraw(WalletOperationRequest request, Throwable t) {
        log.error("Fallback method invoked due to error: {}", t.getMessage());
        log.error("userId {} and amount: {} ", request.userId(), request.amount());
        throw new RuntimeException("Withdraw service is temporarily unavailable. Please try again later.");
    }

    public void retryFallback(WalletOperationRequest request, Throwable t) {
        log.error("Retry fallback method invoked due to error: {}", t.getMessage());
        log.error("userId {} and amount: {} ", request.userId(), request.amount());
        throw new RuntimeException("The withdraw operation failed after multiple retries. Please try again later.");
    }
}
