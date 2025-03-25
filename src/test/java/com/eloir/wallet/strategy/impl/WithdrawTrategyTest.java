package com.eloir.wallet.strategy.impl;

import com.eloir.wallet.dto.WalletOperationRequest;
import com.eloir.wallet.entity.Transaction;
import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.enums.TransactionType;
import com.eloir.wallet.exception.WalletLockedException;
import com.eloir.wallet.repository.TransactionRepository;
import com.eloir.wallet.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawStrategyTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WithdrawStrategy withdrawStrategy;

    private WalletOperationRequest request;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        request = new WalletOperationRequest(
                "user123", new BigDecimal("100.00"),
                TransactionType.WITHDRAW, null);

        wallet = new Wallet();
        wallet.setUserId("user123");
        wallet.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void shouldReturnTransactionTypeWithdraw() {
        assertEquals(TransactionType.WITHDRAW, withdrawStrategy.getTransactionType());
    }

    @Test
    void shouldExecuteWithdrawSuccessfully() {
        when(walletRepository.findByUserId(request.userId())).thenReturn(Optional.of(wallet));

        withdrawStrategy.execute(request);

        assertEquals(new BigDecimal("400.00"), wallet.getBalance());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenWalletNotFound() {
        when(walletRepository.findByUserId(request.userId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> withdrawStrategy.execute(request));

        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInsufficientBalance() {
        wallet.setBalance(new BigDecimal("50.00"));
        when(walletRepository.findByUserId(request.userId())).thenReturn(Optional.of(wallet));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> withdrawStrategy.execute(request));

        assertEquals("Insufficient balance.", exception.getMessage());
    }

    @Test
    void shouldThrowWalletLockedExceptionOnPessimisticLockException() {
        when(walletRepository.findByUserId(request.userId())).thenThrow(PessimisticLockException.class);

        WalletLockedException exception = assertThrows(WalletLockedException.class, () -> withdrawStrategy.execute(request));

        assertEquals("The wallet is temporarily locked due to another operation. Please try again later.", exception.getMessage());
    }

    @Test
    void shouldThrowWalletLockedExceptionOnLockTimeoutException() {
        when(walletRepository.findByUserId(request.userId())).thenThrow(LockTimeoutException.class);

        WalletLockedException exception = assertThrows(WalletLockedException.class, () -> withdrawStrategy.execute(request));

        assertEquals("The wallet is temporarily locked due to another operation. Please try again later.", exception.getMessage());
    }

    @Test
    void shouldThrowRuntimeExceptionOnUnexpectedError() {
        when(walletRepository.findByUserId(request.userId())).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> withdrawStrategy.execute(request));
    }

    @Test
    void shouldFallbackWithdraw() {
        RuntimeException ex = new RuntimeException("Test error");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> withdrawStrategy.fallbackWithdraw(request, ex));

        assertEquals("Withdraw service is temporarily unavailable. Please try again later.", thrown.getMessage());
    }

    @Test
    void shouldRetryFallback() {
        RuntimeException ex = new RuntimeException("Test error");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> withdrawStrategy.retryFallback(request, ex));

        assertEquals("The withdraw operation failed after multiple retries. Please try again later.", thrown.getMessage());
    }
}