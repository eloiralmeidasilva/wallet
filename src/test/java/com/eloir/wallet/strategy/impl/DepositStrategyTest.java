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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositStrategyTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DepositStrategy depositStrategy;

    private WalletOperationRequest request;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        request = new WalletOperationRequest(
                "user123", new BigDecimal("100.00"),
                TransactionType.DEPOSIT, null);
        wallet = new Wallet();
        wallet.setUserId("user123");
        wallet.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void shouldReturnTransactionTypeDeposit() {
        assertEquals(TransactionType.DEPOSIT, depositStrategy.getTransactionType());
    }

    @Test
    void shouldExecuteDepositSuccessfully() {
        when(walletRepository.findByUserId(request.userId())).thenReturn(Optional.of(wallet));

        depositStrategy.execute(request);

        assertEquals(new BigDecimal("600.00"), wallet.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenWalletNotFound() {
        when(walletRepository.findByUserId(request.userId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> depositStrategy.execute(request));

        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void shouldThrowWalletLockedExceptionOnPessimisticLockException() {
        when(walletRepository.findByUserId(request.userId())).thenThrow(PessimisticLockException.class);

        WalletLockedException exception = assertThrows(WalletLockedException.class, () -> depositStrategy.execute(request));

        assertEquals("The wallet is temporarily locked due to another operation. Please try again later.", exception.getMessage());
    }

    @Test
    void shouldThrowWalletLockedExceptionOnLockTimeoutException() {
        when(walletRepository.findByUserId(request.userId())).thenThrow(LockTimeoutException.class);

        WalletLockedException exception = assertThrows(WalletLockedException.class, () -> depositStrategy.execute(request));

        assertEquals("The wallet is temporarily locked due to another operation. Please try again later.", exception.getMessage());
    }

    @Test
    void shouldThrowRuntimeExceptionOnUnexpectedError() {
        when(walletRepository.findByUserId(request.userId())).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> depositStrategy.execute(request));
    }

    @Test
    void shouldFallbackDeposit() {
        RuntimeException ex = new RuntimeException("Test error");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> depositStrategy.fallbackDeposit(request, ex));

        assertEquals("Deposit service is temporarily unavailable. Please try again later.", thrown.getMessage());
    }

    @Test
    void shouldRetryFallback() {
        RuntimeException ex = new RuntimeException("Test error");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> depositStrategy.retryFallback(request, ex));

        assertEquals("The deposit operation failed after multiple retries. Please try again later.", thrown.getMessage());
    }
}