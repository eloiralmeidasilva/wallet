package com.eloir.wallet.validation;

import java.math.BigDecimal;

import com.eloir.wallet.validation.input.DepositValidationInput;
import org.springframework.stereotype.Component;

@Component
public class DepositValidator implements Validator<DepositValidationInput> {

    @Override
    public void validate(DepositValidationInput input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("Invalid input: Deposit information cannot be null.");
        }

        if (input.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }

        if (input.getUserId() == null || input.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
    }
}

