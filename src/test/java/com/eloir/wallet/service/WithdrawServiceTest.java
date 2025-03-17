package com.eloir.wallet.service;

import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.exception.WalletLockedException;
import com.eloir.wallet.repository.TransactionRepository;
import com.eloir.wallet.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class WithdrawServiceTest {

    @InjectMocks
    private WithdrawService withdrawService;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        wallet = new Wallet();
        wallet.setUserId("user1");
        wallet.setBalance(BigDecimal.valueOf(100));
    }

    @Test
    void execute_ShouldPerformWithdraw_WhenBalanceIsSufficient() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.valueOf(50);
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        withdrawService.execute(userId, amount);

        assertEquals(BigDecimal.valueOf(50), wallet.getBalance());
        verify(walletRepository, times(1)).save(wallet); // Verifica se a carteira foi salva
    }

    @Test
    void execute_ShouldThrowIllegalArgumentException_WhenBalanceIsInsufficient() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.valueOf(200);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            withdrawService.execute(userId, amount);
        });
        assertEquals("Insufficient balance.", exception.getMessage());
    }

    @Test
    void execute_ShouldThrowEntityNotFoundException_WhenWalletNotFound() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.valueOf(50);
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            withdrawService.execute(userId, amount);
        });
        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void execute_ShouldThrowWalletLockedException_WhenAnUnexpectedErrorOccurs() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.valueOf(50);
        when(walletRepository.findByUserId(userId)).thenThrow(new PessimisticLockException("Database error"));

        WalletLockedException exception = assertThrows(WalletLockedException.class, () -> {
            withdrawService.execute(userId, amount);
        });
        assertEquals("The wallet is temporarily locked due to another operation. Please try again later.", exception.getMessage());
    }

    @Test
    void fallbackWithdraw_ShouldThrowRuntimeException_WhenCalled() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.valueOf(50);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            withdrawService.fallbackWithdraw(userId, amount, new Exception("Circuit breaker triggered"));
        });

        assertEquals("Withdraw service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    void retryFallback_ShouldThrowRuntimeException_WhenCalled() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.valueOf(50);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            withdrawService.retryFallback(userId, amount, new Exception("Retries exhausted"));
        });

        assertEquals("The withdraw operation failed after multiple retries. Please try again later.", exception.getMessage());
    }
}
