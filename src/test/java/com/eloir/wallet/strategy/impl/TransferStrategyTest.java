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
class TransferStrategyTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferStrategy transferStrategy;

    private WalletOperationRequest request;
    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        request = new WalletOperationRequest(
                "user123", new BigDecimal("100.00"),
                TransactionType.TRANSFER, "receiver456");

        senderWallet = new Wallet();
        senderWallet.setUserId("user123");
        senderWallet.setBalance(new BigDecimal("500.00"));

        receiverWallet = new Wallet();
        receiverWallet.setCodAccount("receiver456");
        receiverWallet.setBalance(new BigDecimal("200.00"));
    }

    @Test
    void shouldReturnTransactionTypeTransfer() {
        assertEquals(TransactionType.TRANSFER, transferStrategy.getTransactionType());
    }

    @Test
    void shouldExecuteTransferSuccessfully() {
        when(walletRepository.findByUserId(request.userId())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByCodWallet(request.receiverCodAccount())).thenReturn(Optional.of(receiverWallet));

        transferStrategy.execute(request);

        assertEquals(new BigDecimal("400.00"), senderWallet.getBalance());
        assertEquals(new BigDecimal("300.00"), receiverWallet.getBalance());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository, times(1)).save(senderWallet);
        verify(walletRepository, times(1)).save(receiverWallet);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenSenderWalletNotFound() {
        when(walletRepository.findByUserId(request.userId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> transferStrategy.execute(request));

        assertEquals("Sender wallet not found", exception.getMessage());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenReceiverWalletNotFound() {
        when(walletRepository.findByUserId(request.userId())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByCodWallet(request.receiverCodAccount())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> transferStrategy.execute(request));

        assertEquals("Receiver wallet not found", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInsufficientBalance() {
        senderWallet.setBalance(new BigDecimal("50.00"));
        when(walletRepository.findByUserId(request.userId())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByCodWallet(request.receiverCodAccount())).thenReturn(Optional.of(receiverWallet));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transferStrategy.execute(request));

        assertEquals("Insufficient balance in sender wallet.", exception.getMessage());
    }

    @Test
    void shouldThrowWalletLockedExceptionOnPessimisticLockException() {
        when(walletRepository.findByUserId(request.userId())).thenThrow(PessimisticLockException.class);

        WalletLockedException exception = assertThrows(WalletLockedException.class, () -> transferStrategy.execute(request));

        assertEquals("The wallet is temporarily locked due to another operation. Please try again later.", exception.getMessage());
    }

    @Test
    void shouldThrowWalletLockedExceptionOnLockTimeoutException() {
        when(walletRepository.findByUserId(request.userId())).thenThrow(LockTimeoutException.class);

        WalletLockedException exception = assertThrows(WalletLockedException.class, () -> transferStrategy.execute(request));

        assertEquals("The wallet is temporarily locked due to another operation. Please try again later.", exception.getMessage());
    }

    @Test
    void shouldThrowRuntimeExceptionOnUnexpectedError() {
        when(walletRepository.findByUserId(request.userId())).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> transferStrategy.execute(request));
    }

    @Test
    void shouldFallbackTransfer() {
        RuntimeException ex = new RuntimeException("Test error");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> transferStrategy.fallbackTransfer(request, ex));

        assertEquals("Transfer service is temporarily unavailable. Please try again later.", thrown.getMessage());
    }

    @Test
    void shouldRetryFallback() {
        RuntimeException ex = new RuntimeException("Test error");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> transferStrategy.retryFallback(request, ex));

        assertEquals("The transfer operation failed after multiple retries. Please try again later.", thrown.getMessage());
    }
}
