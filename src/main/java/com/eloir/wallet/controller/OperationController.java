package com.eloir.wallet.controller;

import com.eloir.wallet.model.OperationRequest;
import com.eloir.wallet.model.TransferRequest;
import com.eloir.wallet.service.DepositService;
import com.eloir.wallet.service.TransferService;
import com.eloir.wallet.service.WithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@Parameter(description = "Amount to be deposited")
                                              @RequestBody @Valid OperationRequest request
    ) {
        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        depositService.execute(authenticatedUserId, request.getAmount());
        return ResponseEntity.ok("Deposit successful.");
    }

    @Operation(
            summary = "Make a withdrawal from the wallet",
            description = "Allows the user to make a withdrawal as long as the user ID in the request is valid.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@Parameter(description = "Amount to be withdrawn")
            @RequestBody @Valid OperationRequest request
    ) {
        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        withdrawService.execute(authenticatedUserId, request.getAmount());
        return ResponseEntity.ok("Withdrawal successful.");
    }

    @Operation(
            summary = "Make a transfer between wallets",
            description = "Allows the user to make a transfer between wallets as long as the user ID and destination account code are valid.",
            security = @SecurityRequirement(name = "BearerToken")
    )
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@Parameter(description = "Amount to be deposited")
                                               @RequestBody @Valid TransferRequest request
    ) {
        String authenticatedUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        transferService.executeTransfer(authenticatedUserId, request.getCodAccount(), request.getAmount());
        return ResponseEntity.ok("Transfer successful.");
    }
}
