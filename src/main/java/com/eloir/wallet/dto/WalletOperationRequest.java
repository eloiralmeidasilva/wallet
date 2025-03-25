package com.eloir.wallet.dto;

import com.eloir.wallet.enums.TransactionType;

import java.math.BigDecimal;

public record WalletOperationRequest(String userId, BigDecimal amount, TransactionType transactionType,
                                     String receiverCodAccount) {
}

