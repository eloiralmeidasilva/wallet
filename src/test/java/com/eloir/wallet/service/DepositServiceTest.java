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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepositServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DepositService depositService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_ShouldCallValidator_WhenValidInput() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.TEN;

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        depositService.execute(userId, amount);

        assertEquals(BigDecimal.TEN, wallet.getBalance());
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void execute_ShouldUpdateWalletBalance_WhenWalletExists() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.TEN;

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.valueOf(50));

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        depositService.execute(userId, amount);

        assertEquals(BigDecimal.valueOf(60), wallet.getBalance());
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void execute_ShouldThrowEntityNotFoundException_WhenWalletNotFound() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.TEN;

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> depositService.execute(userId, amount));

        assertTrue(exception.getMessage().contains("Wallet not found"));
    }


    @Test
    void execute_ShouldThrowWalletLockedException_WhenUnexpectedErrorOccurs() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.TEN;

        when(walletRepository.findByUserId(userId)).thenThrow(new PessimisticLockException("Unexpected error"));

        WalletLockedException exception = assertThrows(WalletLockedException.class,
                () -> depositService.execute(userId, amount));

        assertEquals("The wallet is temporarily locked due to another operation. Please try again later.", exception.getMessage());
    }

    @Test
    void fallbackDeposit_ShouldThrowRuntimeException_WhenFallbackMethodCalled() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.TEN;

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> depositService.fallbackDeposit(userId, amount, new Exception("Test")));

        assertEquals("Deposit service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    void retryFallback_ShouldThrowRuntimeException_WhenRetryFallbackMethodCalled() {
        String userId = "user1";
        BigDecimal amount = BigDecimal.TEN;

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> depositService.retryFallback(userId, amount, new Exception("Test")));

        assertEquals("The deposit operation failed after multiple retries. Please try again later.", exception.getMessage());
    }
}
