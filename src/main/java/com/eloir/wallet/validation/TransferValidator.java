package com.eloir.wallet.validation;

import com.eloir.wallet.validation.input.TransferValidationInput;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferValidator implements Validator<TransferValidationInput> {

    @Override
    public void validate(TransferValidationInput input) throws IllegalArgumentException {
        if (input.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }
        if (input.getUserId() == null || input.getReceiverCodAccount() == null) {
            throw new IllegalArgumentException("Sender and receiver information must be provided.");
        }
    }
}