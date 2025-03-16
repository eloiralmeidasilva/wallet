package com.eloir.wallet.service;

import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.exception.WalletLockedException;
import com.eloir.wallet.repository.WalletRepository;
import com.eloir.wallet.validation.input.WithdrawValidationInput;
import com.eloir.wallet.validation.WithdrawValidator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class WithdrawService implements OperationService {

    private final WalletRepository walletRepository;
    private final WithdrawValidator withdrawValidator;

    public WithdrawService(WalletRepository walletRepository, WithdrawValidator withdrawValidator) {
        this.walletRepository = walletRepository;
        this.withdrawValidator = withdrawValidator;
    }

    @Override
    @CircuitBreaker(name = "walletService", fallbackMethod = "fallbackWithdraw")
    @Retry(name = "walletService", fallbackMethod = "retryFallback")
    @Transactional
    public void execute(String userId, BigDecimal amount) {

        WithdrawValidationInput validationInput = new WithdrawValidationInput(userId, amount);
        withdrawValidator.validate(validationInput);

        log.info("Withdraw operation started for userId: {} with amount: {}", userId, amount);

        try {
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance.");
            }

            wallet.setBalance(wallet.getBalance().subtract(amount));

            walletRepository.save(wallet);

            log.info("Withdraw operation successful for userId: {} with amount: {}", userId, amount);

        } catch (EntityNotFoundException ex) {
            log.error("Entity not found: {}", ex.getMessage());
            throw new EntityNotFoundException(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Withdraw operation failed for userId: {} with amount: {}. Error: {}", userId, amount, ex.getMessage());
            throw new WalletLockedException("The wallet is temporarily locked due to another operation. Please try again later.");
        }
    }

    public void fallbackWithdraw(Throwable t) {
        log.error("Fallback method invoked due to error: {}", t.getMessage());
        throw new WalletLockedException("Withdraw service is temporarily unavailable. Please try again later.");
    }

    public void retryFallback(Throwable t) {
        log.error("Retry fallback method invoked due to error: {}", t.getMessage());
        throw new WalletLockedException("The withdraw operation failed after multiple retries. Please try again later.");
    }
}