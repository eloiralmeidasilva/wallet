package com.eloir.wallet.validation.input;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CreateWalletValidationInput extends WalletValidationInput {
    public CreateWalletValidationInput(String userId, BigDecimal amount) {
        super(userId, amount);
    }
}
