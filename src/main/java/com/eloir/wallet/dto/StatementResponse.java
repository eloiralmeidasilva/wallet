package com.eloir.wallet.dto;

import com.eloir.wallet.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StatementResponse(
        Long transactionId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal finalBalance,
        LocalDateTime createdAt
) {}
