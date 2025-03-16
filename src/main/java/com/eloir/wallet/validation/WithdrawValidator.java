package com.eloir.wallet.validation;

import com.eloir.wallet.validation.input.WithdrawValidationInput;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class WithdrawValidator {

    public void validate(WithdrawValidationInput input) {
        if (input.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        }
        if (input.getUserId() == null) {
            throw new IllegalArgumentException("User ID must be provided.");
        }
    }
}
