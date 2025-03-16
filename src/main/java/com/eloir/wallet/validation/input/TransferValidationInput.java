package com.eloir.wallet.validation.input;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransferValidationInput extends OperationValidationInput {
    private String receiverCodAccount;

    public TransferValidationInput(String senderUserId, String receiverCodAccount, BigDecimal amount) {
        super(senderUserId, amount);
        this.receiverCodAccount = receiverCodAccount;
    }
}
