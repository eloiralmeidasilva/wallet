package com.eloir.wallet.controller;

import com.eloir.wallet.config.security.JwtTokenProvider;
import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;
    private final JwtTokenProvider jwtTokenProvider;

    public WalletController(WalletService walletService, JwtTokenProvider jwtTokenProvider) {
        this.walletService = walletService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(
            summary = "Create a new wallet",
            description = "Creates a new wallet for the authenticated user.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @PostMapping
    public ResponseEntity<Wallet> createWallet() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Wallet wallet = walletService.createWallet(userId);
        log.info("WalletController - createWallet - wallet created for userId: " + userId);
        return ResponseEntity.ok(wallet);
    }

    @Operation(
            summary = "Get wallet balance",
            description = "Retrieves the balance of a user's wallet, ensuring that the user is authenticated.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @Parameter(description = "User ID to retrieve balance") @PathVariable String userId
    ) {
        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("WalletController - getBalance - userId: " + userId + " and authenticatedUserId: " + authenticatedUserId);
        if (!authenticatedUserId.equals(userId)) {
            return ResponseEntity.status(403).body(BigDecimal.ZERO);
        }
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }
}
