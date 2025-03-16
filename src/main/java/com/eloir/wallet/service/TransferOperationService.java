package com.eloir.wallet.service;

import java.math.BigDecimal;

public interface TransferOperationService extends OperationService {
    void executeTransfer(String senderUserId, String receiverCodAccount, BigDecimal amount);
}

