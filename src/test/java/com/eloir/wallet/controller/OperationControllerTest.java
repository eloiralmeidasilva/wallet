package com.eloir.wallet.controller;

import com.eloir.wallet.service.DepositService;
import com.eloir.wallet.service.TransferService;
import com.eloir.wallet.service.WithdrawService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationControllerTest {

    @Mock
    private DepositService depositService;

    @Mock
    private WithdrawService withdrawService;

    @Mock
    private TransferService transferService;

    @InjectMocks
    private OperationController operationController;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private String userId;

    @BeforeEach
    void setUp() {
        userId = "user123";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void deposit_ShouldReturnOk_WhenUserIsAuthenticated() {
        doNothing().when(depositService).execute(userId, BigDecimal.TEN);

        ResponseEntity<String> response = operationController.deposit(BigDecimal.TEN);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Deposit successful.", response.getBody());
        verify(depositService, times(1)).execute(userId, BigDecimal.TEN);
    }

    @Test
    void withdraw_ShouldReturnOk_WhenUserIsAuthenticated() {
        doNothing().when(withdrawService).execute(userId, BigDecimal.TEN);

        ResponseEntity<String> response = operationController.withdraw(BigDecimal.TEN);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Withdrawal successful.", response.getBody());
        verify(withdrawService, times(1)).execute(userId, BigDecimal.TEN);
    }

    @Test
    void transfer_ShouldReturnOk_WhenUserIsAuthenticated() {
        doNothing().when(transferService).executeTransfer(userId, "2025.00000001-01", BigDecimal.TEN);

        ResponseEntity<String> response = operationController.transfer("2025.00000001-01", BigDecimal.TEN);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Transfer successful.", response.getBody());
        verify(transferService, times(1))
                .executeTransfer(userId, "2025.00000001-01", BigDecimal.TEN);
    }
}
