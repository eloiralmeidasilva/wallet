package com.eloir.wallet.validation.input;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransferValidationInput extends WalletValidationInput {
    private String receiverCodAccount;

    public TransferValidationInput(String senderUserId, String receiverCodAccount, BigDecimal amount) {
        super(senderUserId, amount);
        this.receiverCodAccount = receiverCodAccount;
    }
}
