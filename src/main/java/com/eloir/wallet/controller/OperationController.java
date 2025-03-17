package com.eloir.wallet.controller;

import com.eloir.wallet.service.DepositService;
import com.eloir.wallet.service.TransferService;
import com.eloir.wallet.service.WithdrawService;
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

    private final DepositService depositService;
    private final WithdrawService withdrawService;
    private final TransferService transferService;

    public OperationController(DepositService depositService, WithdrawService withdrawService, TransferService transferService) {
        this.depositService = depositService;
        this.withdrawService = withdrawService;
        this.transferService = transferService;
    }

    @Operation(
            summary = "Make a deposit into the wallet",
            description = "Allows the user to make a deposit as long as the user ID in the request is valid.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @PostMapping(value = "/deposit")
    public ResponseEntity<String> deposit(
            @Parameter(description = "Amount to be deposited")
            @Valid @Min(value = 0, message = "Min value is Zero")
            @NotNull @RequestParam BigDecimal amount
    ) {
        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Amount : {} userId: {}", amount, authenticatedUserId);
        depositService.execute(authenticatedUserId, amount);
        return ResponseEntity.ok("Deposit successful.");
    }

    @Operation(
            summary = "Make a withdrawal from the wallet",
            description = "Allows the user to make a withdrawal as long as the user ID in the request is valid.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @PostMapping(value = "/withdraw")
    public ResponseEntity<String> withdraw(
            @Parameter(description = "Amount to be withdraw")
            @NotNull @Valid @Min(value = 0, message = "Min value is Zero")
            @RequestParam BigDecimal amount
    ) {
        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Amount : {} userId: {}", amount, authenticatedUserId);
        withdrawService.execute(authenticatedUserId, amount);
        return ResponseEntity.ok("Withdrawal successful.");
    }

    @Operation(
            summary = "Make a transfer between wallets",
            description = "Allows the user to make a transfer between wallets as long as the user ID and destination account code are valid.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @PostMapping(value = "/transfer")
    public ResponseEntity<String> transfer(
            @Parameter(description = "Destination account code") @NotNull @RequestParam String codAccount,
            @Parameter(description = "Amount to be transferred")
            @NotNull @Min(value = 0, message = "Min value is Zero") @RequestParam BigDecimal amount
    ) {
        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("transfer - Amount : {} userId: {} receiverCodAccount: {}", amount, authenticatedUserId, codAccount);
        transferService.executeTransfer(authenticatedUserId, codAccount, amount);
        return ResponseEntity.ok("Transfer successful.");
    }
}
