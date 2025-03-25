package com.eloir.wallet.controller;

import com.eloir.wallet.dto.WalletOperationRequest;
import com.eloir.wallet.enums.TransactionType;
import com.eloir.wallet.service.WalletOperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class OperationControllerTest {

    @InjectMocks
    private OperationController operationController;

    @Mock
    private WalletOperationService walletOperationService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(operationController).build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("user123");
    }

    @Test
    public void testDeposit() throws Exception {
        BigDecimal depositAmount = new BigDecimal("100.00");

        doNothing().when(walletOperationService).execute(any(WalletOperationRequest.class));

        mockMvc.perform(post("/api/operations/deposit")
                        .param("amount", depositAmount.toString())
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());

        verify(walletOperationService).execute(new WalletOperationRequest(
                "user123", depositAmount, TransactionType.DEPOSIT, null));
    }

    @Test
    public void testWithdraw() throws Exception {
        BigDecimal withdrawAmount = new BigDecimal("50.00");

        doNothing().when(walletOperationService).execute(any(WalletOperationRequest.class));

        mockMvc.perform(post("/api/operations/withdraw")
                        .param("amount", withdrawAmount.toString())
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());

        verify(walletOperationService).execute(new WalletOperationRequest(
                "user123", withdrawAmount, TransactionType.WITHDRAW, null));
    }

    @Test
    public void testTransfer() throws Exception {
        BigDecimal transferAmount = new BigDecimal("30.00");
        String destinationAccount = "acc456";

        doNothing().when(walletOperationService).execute(any(WalletOperationRequest.class));

        mockMvc.perform(post("/api/operations/transfer")
                        .param("codAccount", destinationAccount)
                        .param("amount", transferAmount.toString())
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());

        verify(walletOperationService).execute(new WalletOperationRequest(
                "user123", transferAmount, TransactionType.TRANSFER, destinationAccount));
    }

}