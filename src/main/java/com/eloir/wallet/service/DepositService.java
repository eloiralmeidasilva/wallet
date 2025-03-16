package com.eloir.wallet.service;

import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.exception.WalletLockedException;
import com.eloir.wallet.repository.WalletRepository;
import com.eloir.wallet.validation.input.DepositValidationInput;
import com.eloir.wallet.validation.DepositValidator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class DepositService implements OperationService {

    private final WalletRepository walletRepository;
    private final DepositValidator depositValidator;

    public DepositService(WalletRepository walletRepository, DepositValidator depositValidator) {
        this.walletRepository = walletRepository;
        this.depositValidator = depositValidator;
    }

    @Override
    @CircuitBreaker(name = "depositService", fallbackMethod = "fallbackDeposit")
    @Retry(name = "depositService", fallbackMethod = "retryFallback")
    @Transactional
    public void execute(String userId, BigDecimal amount) {

        DepositValidationInput validationInput = new DepositValidationInput(userId, amount);
        depositValidator.validate(validationInput);

        log.info("Deposit operation started for userId: {} with amount: {}", userId, amount);

        try {
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

            wallet.setBalance(amount.add(wallet.getBalance()));

            walletRepository.save(wallet);

            log.info("Deposit operation successful for userId: {} with amount: {}", userId, amount);
        } catch (EntityNotFoundException ex){
            log.error(ex.getMessage());
            throw new EntityNotFoundException(ex);
        } catch (Exception e) {
            log.error("Deposit operation failed for userId: {} with amount: {}. Error: {}", userId, amount, e.getMessage());
            throw new WalletLockedException("The wallet is temporarily locked due to another operation. Please try again later.");
        }
    }

    public void fallbackDeposit(String userId, BigDecimal amount, Throwable t) {
        log.error("Fallback method invoked due to error: {}", t.getMessage());
        throw new RuntimeException("Deposit service is temporarily unavailable. Please try again later.");
    }

    public void retryFallback(String userId, BigDecimal amount, Throwable t) {
        log.error("Retry fallback method invoked due to error: {}", t.getMessage());
        throw new RuntimeException("The deposit operation failed after multiple retries. Please try again later.");
    }
}
