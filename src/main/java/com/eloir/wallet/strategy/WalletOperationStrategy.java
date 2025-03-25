package com.eloir.wallet.strategy;

import com.eloir.wallet.dto.WalletOperationRequest;
import com.eloir.wallet.enums.TransactionType;

public interface WalletOperationStrategy {
    void execute(WalletOperationRequest request);
    TransactionType getTransactionType();
}
