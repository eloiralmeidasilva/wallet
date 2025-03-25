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
public class TransferStrategy implements WalletOperationStrategy {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransferStrategy(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.TRANSFER;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "walletService", fallbackMethod = "fallbackTransfer")
    @Retry(name = "walletService", fallbackMethod = "retryFallback")
    public void execute(WalletOperationRequest request) {
        log.info("Transfer operation started: senderUserId={} → receiverCodAccount={} | amount={}",
                request.userId(), request.receiverCodAccount(), request.amount());

        try {
            Wallet senderWallet = walletRepository.findByUserId(request.userId())
                    .orElseThrow(() -> new EntityNotFoundException("Sender wallet not found"));

            Wallet receiverWallet = walletRepository.findByCodWallet(request.receiverCodAccount())
                    .orElseThrow(() -> new EntityNotFoundException("Receiver wallet not found"));

            if (senderWallet.getBalance().compareTo(request.amount()) < 0) {
                throw new IllegalArgumentException("Insufficient balance in sender wallet.");
            }

            BigDecimal finalBalanceSender = senderWallet.getBalance().subtract(request.amount());
            BigDecimal finalBalanceReceiver = receiverWallet.getBalance().add(request.amount());

            Transaction transaction = new Transaction(
                    senderWallet, receiverWallet,
                    senderWallet.getUserId(), receiverWallet.getUserId(),
                    request.amount(), finalBalanceSender, finalBalanceReceiver
            );

            transactionRepository.save(transaction);

            senderWallet.setBalance(finalBalanceSender);
            receiverWallet.setBalance(finalBalanceReceiver);

            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);

            log.info("Transfer successful: senderUserId={} → receiverCodAccount={} | amount={} | finalSenderBalance={} | finalReceiverBalance={}",
                    request.userId(), request.receiverCodAccount(), request.amount(), finalBalanceSender, finalBalanceReceiver);

        } catch (EntityNotFoundException ex) {
            log.error("Wallet not found: {}", ex.getMessage());
            throw ex;
        } catch (PessimisticLockException | LockTimeoutException ex) {
            log.error("Wallet is locked: senderUserId={} - Error: {}", request.userId(), ex.getMessage());
            throw new WalletLockedException("The wallet is temporarily locked due to another operation. Please try again later.");
        } catch (Exception e) {
            log.error("Unexpected error in transfer: senderUserId={} - Error: {}", request.userId(), e.getMessage(), e);
            throw e;
        }
    }

    public void fallbackTransfer(WalletOperationRequest request, Throwable t) {
        log.error("Fallback transfer due to error: {}", t.getMessage());
        throw new RuntimeException("Transfer service is temporarily unavailable. Please try again later.");
    }

    public void retryFallback(WalletOperationRequest request, Throwable t) {
        log.error("Retry fallback due to error: {}", t.getMessage());
        throw new RuntimeException("The transfer operation failed after multiple retries. Please try again later.");
    }
}

