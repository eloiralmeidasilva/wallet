package com.eloir.wallet.validation.input;

import java.math.BigDecimal;

public class CreateWalletValidationInput extends WalletValidationInput {
    public CreateWalletValidationInput(String userId, BigDecimal amount) {
        super(userId, amount);
    }
}
