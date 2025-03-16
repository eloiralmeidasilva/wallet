package com.eloir.wallet.validation.input;

import java.math.BigDecimal;

public class DepositValidationInput extends WalletValidationInput {
    public DepositValidationInput(String userId, BigDecimal amount) {
        super(userId, amount);
    }
}
