package com.eloir.wallet.service;

import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.exception.WalletLockedException;
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
public class TransferService implements TransferOperationService {

    private final WalletRepository walletRepository;

    public TransferService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    @CircuitBreaker(name = "walletService", fallbackMethod = "fallbackTransfer")
    @Retry(name = "walletService", fallbackMethod = "retryFallback")
    public void executeTransfer(String senderUserId, String receiverCodAccount, BigDecimal amount) {
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
            log.error("Wallet not found: {}", ex.getMessage());
            throw ex;
        } catch (PessimisticLockException | LockTimeoutException ex) {
            log.error("Wallet is locked for userId: {} - Error: {}", senderUserId, ex.getMessage());
            throw new WalletLockedException("The wallet is temporarily locked due to another operation. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error during deposit for userId: {} - Error: {}", senderUserId, e.getMessage(), e);
            throw e;
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