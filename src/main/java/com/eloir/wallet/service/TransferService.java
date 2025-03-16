package com.eloir.wallet.service;

import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.exception.WalletLockedException;
import com.eloir.wallet.repository.WalletRepository;
import com.eloir.wallet.validation.input.TransferValidationInput;
import com.eloir.wallet.validation.TransferValidator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TransferService implements TransferOperationService {

    private final WalletRepository walletRepository;
    private final TransferValidator transferValidator;

    public TransferService(WalletRepository walletRepository, TransferValidator transferValidator) {
        this.walletRepository = walletRepository;
        this.transferValidator = transferValidator;
    }

    @Transactional
    @CircuitBreaker(name = "walletService", fallbackMethod = "fallbackTransfer")
    @Retry(name = "walletService", fallbackMethod = "retryFallback")
    public void executeTransfer(String senderUserId, String receiverCodAccount, BigDecimal amount) {

        TransferValidationInput validationInput = new TransferValidationInput(senderUserId, receiverCodAccount, amount);
        transferValidator.validate(validationInput);

        log.info("Transfer operation started for senderUserId: {} to receiverCodAccount: {} with amount: {}", senderUserId, receiverCodAccount, amount);

        try {
            Wallet senderWallet = walletRepository.findByUserId(senderUserId)
                    .orElseThrow(() -> new EntityNotFoundException("Sender wallet not found"));

            Wallet receiverWallet = walletRepository.findByCodWallet(receiverCodAccount)
                    .orElseThrow(() -> new EntityNotFoundException("Receiver wallet not found"));

            if (senderWallet.getBalance().compareTo(amount) < 0) {
                log.error("Insufficient balance in sender wallet.");
                throw new IllegalArgumentException("Insufficient balance in sender wallet.");
            }

            senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
            receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);

            log.info("Transfer operation successful for senderUserId: {} to receiverCodAccount: {} with amount: {}", senderUserId, receiverCodAccount, amount);

        } catch (EntityNotFoundException ex) {
            log.error("Entity not found: {}", ex.getMessage());
            throw new EntityNotFoundException(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Transfer operation failed for senderUserId: {} with amount: {}. Error: {}", senderUserId, amount, ex.getMessage());
            throw new WalletLockedException("The wallet is temporarily locked due to another operation. Please try again later.");
        }
    }

    public void fallbackTransfer(String senderUserId, String receiverCodAccount, BigDecimal amount, Throwable t) {
        log.error("Fallback method invoked due to error: {}", t.getMessage());
        throw new RuntimeException("Transfer service is temporarily unavailable. Please try again later.");
    }

    public void retryFallback(String senderUserId, String receiverCodAccount, BigDecimal amount, Throwable t) {
        log.error("Retry fallback method invoked due to error: {}", t.getMessage());
        throw new RuntimeException("The transfer operation failed after multiple retries. Please try again later.");
    }

    @Override
    public void execute(String userId, BigDecimal amount) {
        throw new UnsupportedOperationException("Transfer operation should use executeTransfer instead.");
    }
}