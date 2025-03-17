package com.eloir.wallet.controller;

import com.eloir.wallet.config.security.JwtTokenProvider;
import com.eloir.wallet.dto.StatementResponse;
import com.eloir.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
    public ResponseEntity<?> createWallet() {
        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var wallet = walletService.createWallet(authenticatedUserId);
        log.info("WalletController - createWallet - wallet created for userId: {}", authenticatedUserId);
        return ResponseEntity.ok(wallet);
    }

    @Operation(
            summary = "Get wallet balance",
            description = "Retrieves the balance of a user's wallet, ensuring authentication.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("WalletController - getBalance - userId: {}", authenticatedUserId);
        var balance = walletService.getBalance(authenticatedUserId);
        return ResponseEntity.ok(balance);
    }

    @Operation(
            summary = "Get wallet statement",
            description = "Retrieves the transaction history for a user's wallet within a date range.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @GetMapping("/statement")
    public ResponseEntity<List<StatementResponse>> getStatement(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("WalletController - getStatement - userId: {} | startDate: {} | endDate: {}", userId, startDate, endDate);

        var statement = walletService.getStatement(userId, startDate, endDate);
        return ResponseEntity.ok(statement);
    }
}
