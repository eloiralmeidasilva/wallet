package com.eloir.wallet.controller;

import com.eloir.wallet.service.DepositService;
import com.eloir.wallet.service.WithdrawService;
import com.eloir.wallet.service.TransferService;
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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
        doNothing().when(depositService).execute(userId, BigDecimal.TEN); // Supondo que execute seja void

        ResponseEntity<String> response = operationController.deposit(userId, BigDecimal.TEN);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Deposit successful.", response.getBody());
        verify(depositService, times(1)).execute(userId, BigDecimal.TEN); // Verificando se o método foi chamado uma vez
    }

    @Test
    void withdraw_ShouldReturnOk_WhenUserIsAuthenticated() {
        doNothing().when(withdrawService).execute(userId, BigDecimal.TEN); // Supondo que execute seja void

        ResponseEntity<String> response = operationController.withdraw(userId, BigDecimal.TEN);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Withdrawal successful.", response.getBody());
        verify(withdrawService, times(1)).execute(userId, BigDecimal.TEN); // Verificando se o método foi chamado uma vez
    }

    @Test
    void transfer_ShouldReturnOk_WhenUserIsAuthenticated() {
        doNothing().when(transferService).executeTransfer(userId, "2025.00000001-01", BigDecimal.TEN); // Supondo que executeTransfer seja void

        ResponseEntity<String> response = operationController.transfer(userId, "2025.00000001-01", BigDecimal.TEN);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Transfer successful.", response.getBody());
        verify(transferService, times(1)).executeTransfer(userId, "2025.00000001-01", BigDecimal.TEN); // Verificando se o método foi chamado uma vez
    }

    @Test
    void deposit_ShouldReturnForbidden_WhenUserIdDoesNotMatch() {
        when(authentication.getPrincipal()).thenReturn("wrongUser");

        ResponseEntity<String> response = operationController.deposit(userId, BigDecimal.TEN);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Forbidden: User ID does not match.", response.getBody());
    }

    @Test
    void withdraw_ShouldReturnForbidden_WhenUserIdDoesNotMatch() {
        when(authentication.getPrincipal()).thenReturn("wrongUser");

        ResponseEntity<String> response = operationController.withdraw(userId, BigDecimal.TEN);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Forbidden: User ID does not match.", response.getBody());
    }

    @Test
    void transfer_ShouldReturnForbidden_WhenUserIdDoesNotMatch() {
        when(authentication.getPrincipal()).thenReturn("wrongUser");

        ResponseEntity<String> response = operationController.transfer(userId, "2025.00000001-01", BigDecimal.TEN);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Forbidden: User ID does not match.", response.getBody());
    }
}
