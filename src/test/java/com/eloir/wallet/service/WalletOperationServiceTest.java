package com.eloir.wallet.service;

import com.eloir.wallet.dto.WalletOperationRequest;
import com.eloir.wallet.enums.TransactionType;
import com.eloir.wallet.strategy.WalletOperationStrategy;
import com.eloir.wallet.strategy.impl.DepositStrategy;
import com.eloir.wallet.strategy.impl.TransferStrategy;
import com.eloir.wallet.strategy.impl.WithdrawStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletOperationServiceTest {
/*
    @Mock
    private DepositStrategy depositStrategy;

    @Mock
    private WithdrawStrategy withdrawStrategy;

    @Mock
    private TransferStrategy transferStrategy;

    @InjectMocks
    private WalletOperationService walletOperationService;

    private WalletOperationRequest request;

    @BeforeEach
    void setUp() {
        request = new WalletOperationRequest(
                "user123", new BigDecimal("100.00"), TransactionType.DEPOSIT, null);

        List<WalletOperationStrategy> strategies = List.of(depositStrategy, withdrawStrategy, transferStrategy);

        walletOperationService = new WalletOperationService(strategies);
    }

    @Test
    void shouldExecuteWithdrawStrategy() {
        when(withdrawStrategy.getTransactionType()).thenReturn(TransactionType.WITHDRAW);
        WalletOperationRequest request = new WalletOperationRequest(
                "user123", new BigDecimal("100.00"), TransactionType.WITHDRAW, null);

        walletOperationService.execute(request);

        verify(withdrawStrategy, times(1)).execute(request);
        verifyNoInteractions(depositStrategy);
        verifyNoInteractions(transferStrategy);
    }

    @Test
    void shouldExecuteDepositStrategy() {
        when(depositStrategy.getTransactionType()).thenReturn(TransactionType.DEPOSIT);
        when(withdrawStrategy.getTransactionType()).thenReturn(TransactionType.WITHDRAW);
        when(transferStrategy.getTransactionType()).thenReturn(TransactionType.TRANSFER);

        walletOperationService.execute(request);

        verifyNoInteractions(withdrawStrategy);
        verifyNoInteractions(transferStrategy);
        verify(depositStrategy, times(1)).execute(request);
    }

    @Test
    void shouldExecuteTransferStrategy() {
        when(transferStrategy.getTransactionType()).thenReturn(TransactionType.TRANSFER);
        when(withdrawStrategy.getTransactionType()).thenReturn(TransactionType.WITHDRAW);
        when(depositStrategy.getTransactionType()).thenReturn(TransactionType.DEPOSIT);

        walletOperationService.execute(request);

        verifyNoInteractions(withdrawStrategy);
        verifyNoInteractions(depositStrategy);
        verify(transferStrategy, times(1)).execute(request);
    }

    @Test
    void shouldNotCallAnyStrategyIfNoneMatch() {
        when(withdrawStrategy.getTransactionType()).thenReturn(TransactionType.WITHDRAW);
        when(depositStrategy.getTransactionType()).thenReturn(TransactionType.DEPOSIT);
        when(transferStrategy.getTransactionType()).thenReturn(TransactionType.TRANSFER);

        // Aqui, simulamos um tipo de transação não correspondente
        request = new WalletOperationRequest(
                "user123", new BigDecimal("100.00"), TransactionType.DEPOSIT, null);

        walletOperationService.execute(request);

        verifyNoInteractions(withdrawStrategy);
        verify(depositStrategy, times(1)).execute(request);
        verifyNoInteractions(transferStrategy);
    }
    */
}
