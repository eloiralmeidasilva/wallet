package com.eloir.wallet.validation.input;

import java.math.BigDecimal;

public class WithdrawValidationInput extends WalletValidationInput {
    public WithdrawValidationInput(String userId, BigDecimal amount) {
        super(userId, amount);
    }
}
