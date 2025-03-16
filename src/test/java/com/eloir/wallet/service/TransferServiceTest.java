package com.eloir.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.repository.WalletRepository;
import com.eloir.wallet.service.TransferService;
import com.eloir.wallet.validation.TransferValidator;
import com.eloir.wallet.validation.input.TransferValidationInput;
import jakarta.persistence.EntityNotFoundException;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

class TransferServiceTest {

    private TransferService transferService;
    private WalletRepository walletRepository;
    private TransferValidator transferValidator;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        transferValidator = mock(TransferValidator.class);
        transferService = new TransferService(walletRepository, transferValidator);
    }

    @Test
    void executeTransfer_ShouldCallValidator_WhenValidInput() {
        String senderUserId = "sender";
        String receiverCodAccount = "receiver";
        BigDecimal amount = BigDecimal.TEN;

        TransferValidationInput input = new TransferValidationInput(senderUserId, receiverCodAccount, amount);

        Wallet senderWallet = new Wallet();
        senderWallet.setUserId(senderUserId);
        senderWallet.setBalance(BigDecimal.valueOf(100));

        Wallet receiverWallet = new Wallet();
        receiverWallet.setCodAccount(receiverCodAccount);
        receiverWallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findByUserId(senderUserId)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByCodWallet(receiverCodAccount)).thenReturn(Optional.of(receiverWallet));

        ArgumentCaptor<TransferValidationInput> captor = ArgumentCaptor.forClass(TransferValidationInput.class);

        transferService.executeTransfer(senderUserId, receiverCodAccount, amount);

        verify(transferValidator, times(1)).validate(captor.capture());
        TransferValidationInput capturedInput = captor.getValue();

        assertEquals(senderUserId, capturedInput.getUserId());
        assertEquals(receiverCodAccount, capturedInput.getReceiverCodAccount());
        assertEquals(amount, capturedInput.getAmount());
    }
    @Test
    void executeTransfer_ShouldThrowEntityNotFoundException_WhenSenderWalletNotFound() {
        String senderUserId = "sender";
        String receiverCodAccount = "receiver";
        BigDecimal amount = BigDecimal.TEN;

        when(walletRepository.findByUserId(senderUserId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> transferService.executeTransfer(senderUserId, receiverCodAccount, amount));

        assertEquals("Sender wallet not found", exception.getMessage());
    }

    @Test
    void executeTransfer_ShouldThrowEntityNotFoundException_WhenReceiverWalletNotFound() {
        String senderUserId = "sender";
        String receiverCodAccount = "receiver";
        BigDecimal amount = BigDecimal.TEN;

        when(walletRepository.findByUserId(senderUserId)).thenReturn(Optional.of(new Wallet()));
        when(walletRepository.findByCodWallet(receiverCodAccount)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> transferService.executeTransfer(senderUserId, receiverCodAccount, amount));

        assertEquals("Receiver wallet not found", exception.getMessage());
    }

    @Test
    void executeTransfer_ShouldThrowIllegalArgumentException_WhenInsufficientBalance() {
        String senderUserId = "sender";
        String receiverCodAccount = "receiver";
        BigDecimal amount = BigDecimal.TEN;

        Wallet senderWallet = new Wallet();
        senderWallet.setUserId(senderUserId);
        senderWallet.setBalance(BigDecimal.valueOf(5));

        Wallet receiverWallet = new Wallet();
        receiverWallet.setCodAccount(receiverCodAccount);
        receiverWallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findByUserId(senderUserId)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByCodWallet(receiverCodAccount)).thenReturn(Optional.of(receiverWallet));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transferService.executeTransfer(senderUserId, receiverCodAccount, amount));

        assertEquals("Insufficient balance in sender wallet.", exception.getMessage());
    }

    @Test
    void executeTransfer_ShouldUpdateBalances_WhenTransferIsSuccessful() {
        String senderUserId = "sender";
        String receiverCodAccount = "receiver";
        BigDecimal amount = BigDecimal.TEN;

        Wallet senderWallet = new Wallet();
        senderWallet.setUserId(senderUserId);
        senderWallet.setBalance(BigDecimal.valueOf(100));

        Wallet receiverWallet = new Wallet();
        receiverWallet.setCodAccount(receiverCodAccount);
        receiverWallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findByUserId(senderUserId)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByCodWallet(receiverCodAccount)).thenReturn(Optional.of(receiverWallet));

        transferService.executeTransfer(senderUserId, receiverCodAccount, amount);

        assertEquals(BigDecimal.valueOf(90), senderWallet.getBalance());  // Remetente perdeu o valor
        assertEquals(BigDecimal.TEN, receiverWallet.getBalance());  // Destinatário recebeu o valor

        verify(walletRepository, times(1)).save(senderWallet);
        verify(walletRepository, times(1)).save(receiverWallet);
    }

    @Test
    void fallbackTransfer_ShouldThrowRuntimeException_WhenCalled() {
        String senderUserId = "sender";
        String receiverCodAccount = "receiver";
        BigDecimal amount = BigDecimal.TEN;

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transferService.fallbackTransfer(senderUserId, receiverCodAccount, amount, new Exception("Error")));

        assertEquals("Transfer service is temporarily unavailable. Please try again later.", exception.getMessage());
    }


}
