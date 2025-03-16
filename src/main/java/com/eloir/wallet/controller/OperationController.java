package com.eloir.wallet.controller;

import com.eloir.wallet.service.DepositService;
import com.eloir.wallet.service.WithdrawService;
import com.eloir.wallet.service.TransferService;
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

    private boolean validateUserId(String userIdFromRequest) {
        String userIdFromToken = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userIdFromRequest.equals(userIdFromToken);
    }

    @Operation(
            summary = "Make a deposit into the wallet",
            description = "Allows the user to make a deposit as long as the user ID in the request is valid.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(
            @Parameter(description = "User ID making the deposit") @RequestParam String userId,
            @Parameter(description = "Amount to be deposited") @RequestParam BigDecimal amount
    ) {
        if (!validateUserId(userId)) {
            return ResponseEntity.status(403).body("Forbidden: User ID does not match.");
        }

        depositService.execute(userId, amount);
        return ResponseEntity.ok("Deposit successful.");
    }

    @Operation(
            summary = "Make a withdrawal from the wallet",
            description = "Allows the user to make a withdrawal as long as the user ID in the request is valid.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @Parameter(description = "User ID making the withdrawal") @RequestParam String userId,
            @Parameter(description = "Amount to be withdrawn") @RequestParam BigDecimal amount
    ) {
        if (!validateUserId(userId)) {
            return ResponseEntity.status(403).body("Forbidden: User ID does not match.");
        }

        withdrawService.execute(userId, amount);
        return ResponseEntity.ok("Withdrawal successful.");
    }

    @Operation(
            summary = "Make a transfer between wallets",
            description = "Allows the user to make a transfer between wallets as long as the user ID and destination account code are valid.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @Parameter(description = "User ID making the transfer") @RequestParam String userId,
            @Parameter(description = "Destination account code") @RequestParam String codAccount,
            @Parameter(description = "Amount to be transferred") @RequestParam BigDecimal amount
    ) {
        if (!validateUserId(userId)) {
            return ResponseEntity.status(403).body("Forbidden: User ID does not match.");
        }

        transferService.executeTransfer(userId, codAccount, amount);
        return ResponseEntity.ok("Transfer successful.");
    }
}
