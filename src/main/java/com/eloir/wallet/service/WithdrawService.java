package com.eloir.wallet.service;

import com.eloir.wallet.entity.Transaction;
import com.eloir.wallet.enums.TransactionType;
import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.exception.WalletLockedException;
import com.eloir.wallet.repository.TransactionRepository;
import com.eloir.wallet.repository.WalletRepository;
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
public class WithdrawService implements OperationService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WithdrawService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @CircuitBreaker(name = "walletService", fallbackMethod = "fallbackWithdraw")
    @Retry(name = "walletService", fallbackMethod = "retryFallback")
    @Transactional
    public void execute(String userId, BigDecimal amount) {
        log.info("Withdraw operation started for userId: {} with amount: {}", userId, amount);

        try {
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance.");
            }

            BigDecimal newBalance = wallet.getBalance().subtract(amount);
            wallet.setBalance(newBalance);

            Transaction transaction = new Transaction(wallet, TransactionType.WITHDRAW, amount, newBalance);
            transactionRepository.save(transaction);

            walletRepository.save(wallet);

            log.info("Withdraw operation successful for userId: {} with amount: {}", userId, amount);

        } catch (EntityNotFoundException ex) {
            log.error("Wallet not found: {}", ex.getMessage());
            throw ex;
        } catch (PessimisticLockException | LockTimeoutException ex) {
            log.error("Wallet is locked for userId: {} - Error: {}", userId, ex.getMessage());
            throw new WalletLockedException("The wallet is temporarily locked due to another operation. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error during withdraw for userId: {} - Error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public void fallbackWithdraw(String userId, BigDecimal amount, Throwable t) {
        log.error("Fallback method invoked due to error: {}", t.getMessage());
        log.error("userId {} and amount: {} ", userId, amount);
        throw new RuntimeException("Withdraw service is temporarily unavailable. Please try again later.");
    }

    public void retryFallback(String userId, BigDecimal amount, Throwable t) {
        log.error("Retry fallback method invoked due to error: {}", t.getMessage());
        log.error("userId {} and amount: {} ", userId, amount);
        throw new RuntimeException("The withdraw operation failed after multiple retries. Please try again later.");
    }
}