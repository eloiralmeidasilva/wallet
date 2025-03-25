package com.eloir.wallet.controller;

import com.eloir.wallet.dto.WalletOperationRequest;
import com.eloir.wallet.enums.TransactionType;
import com.eloir.wallet.service.WalletOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/operations")
public class OperationController {

    private final WalletOperationService walletOperationService;

    public OperationController(WalletOperationService walletOperationService) {
        this.walletOperationService = walletOperationService;
    }

    @Operation(summary = "Make a deposit", security = @SecurityRequirement(name = "BearerToken"))
    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(
            @Parameter(description = "Amount to be deposited")
            @Valid @Min(value = 0, message = "Min value is Zero")
            @NotNull @RequestParam BigDecimal amount) {

        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Deposit requested: userId={} amount={}", authenticatedUserId, amount);

        WalletOperationRequest request = new WalletOperationRequest(authenticatedUserId, amount, TransactionType.DEPOSIT, null);
        walletOperationService.execute(request);
        return ResponseEntity.ok("Deposit successful.");
    }

    @Operation(summary = "Make a withdrawal", security = @SecurityRequirement(name = "BearerToken"))
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @Parameter(description = "Amount to withdraw")
            @Valid @Min(value = 0, message = "Min value is Zero")
            @NotNull @RequestParam BigDecimal amount) {

        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Withdraw requested: userId={} amount={}", authenticatedUserId, amount);

        WalletOperationRequest request = new WalletOperationRequest(authenticatedUserId, amount, TransactionType.WITHDRAW, null);
        walletOperationService.execute(request);

        return ResponseEntity.ok("Withdrawal successful.");
    }

    @Operation(summary = "Make a transfer", security = @SecurityRequirement(name = "BearerToken"))
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @Parameter(description = "Destination account code")
            @NotNull @RequestParam String codAccount,
            @Parameter(description = "Amount to transfer")
            @Valid @Min(value = 0, message = "Min value is Zero")
            @NotNull @RequestParam BigDecimal amount) {

        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Transfer requested: senderUserId={} receiverCodAccount={} amount={}", authenticatedUserId, codAccount, amount);

        WalletOperationRequest request = new WalletOperationRequest(authenticatedUserId, amount, TransactionType.TRANSFER, codAccount);
        walletOperationService.execute(request);

        return ResponseEntity.ok("Transfer successful.");
    }
}
