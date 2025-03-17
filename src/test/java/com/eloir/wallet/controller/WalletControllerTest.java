package com.eloir.wallet.controller;

import com.eloir.wallet.config.security.JwtTokenProvider;
import com.eloir.wallet.dto.WalletResponse;
import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.model.User;
import com.eloir.wallet.service.WalletService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private WalletController walletController;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId("user123");
        mockUser.setUserName("user");
        mockUser.setEmail("user.wallet@gmail.com");
        mockUser.setRole("USER");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("user123");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createWallet_ShouldReturnWallet_WhenSuccessful() {
        Wallet wallet = new Wallet();
        wallet.setCodAccount("2025.00000001-01");
        when(walletService.createWallet("user123")).thenReturn(wallet);

        ResponseEntity<?> response = walletController.createWallet();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void getBalance_ShouldReturnBalance_WhenUserIsAuthenticated() {
        WalletResponse walletResponse = new WalletResponse("2025.00000001-01", BigDecimal.valueOf(1000));
        when(walletService.getBalance("user123")).thenReturn(walletResponse);

        ResponseEntity<?> response = walletController.getBalance();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(walletResponse, response.getBody());
    }
}