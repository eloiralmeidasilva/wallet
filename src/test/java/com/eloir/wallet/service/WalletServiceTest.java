package com.eloir.wallet.service;

import com.eloir.wallet.dto.WalletResponse;
import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.repository.WalletRepository;
import com.eloir.wallet.validation.CreateWalletValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CreateWalletValidator createWalletValidator;

    @InjectMocks
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa os mocks
    }

    @Test
    void testCreateWallet() {
        String userId = "user123";

        doNothing().when(createWalletValidator).validate(any());

        when(walletRepository.save(any(Wallet.class))).thenReturn(new Wallet());

        Wallet createdWallet = walletService.createWallet(userId);

        verify(walletRepository, times(1)).save(any(Wallet.class));
        assertNotNull(createdWallet);
    }

    @Test
    void testGetBalance_Success() {
        String userId = "user123";
        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.valueOf(100));
        wallet.setCodAccount("2025.00000001-01");

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        WalletResponse walletResponse = walletService.getBalance(userId);

        assertEquals(BigDecimal.valueOf(100), walletResponse.amount());
    }

    @Test
    void testGetBalance_WalletNotFound() {
        String userId = "nonExistingUser";

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> walletService.getBalance(userId));
    }

    @Test
    void testSetRandomCodAccount() {
        String userId = "user123";
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);

        when(walletRepository.findMaxNumAccountWithLock()).thenReturn(5);

        walletService.setRandomCodAccount(wallet);

        String year = String.valueOf(LocalDate.now().getYear());
        String expectedCodAccount = year + ".00000006-01"; // ano + número + tipo de conta
        assertEquals(expectedCodAccount, wallet.getCodAccount());
    }

    @Test
    void testSetRandomCodAccount_InitialCase() {
        String userId = "user123";
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);

        when(walletRepository.findMaxNumAccountWithLock()).thenReturn(null);

        walletService.setRandomCodAccount(wallet);

        String year = String.valueOf(LocalDate.now().getYear());
        String expectedCodAccount = year + ".00000001-01"; // ano + número + tipo de conta
        assertEquals(expectedCodAccount, wallet.getCodAccount());
    }
}

