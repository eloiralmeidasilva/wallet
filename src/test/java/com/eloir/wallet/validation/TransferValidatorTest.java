package com.eloir.wallet.validation;

import com.eloir.wallet.validation.input.TransferValidationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferValidatorTest {

    private TransferValidator transferValidator;

    @BeforeEach
    void setUp() {
        transferValidator = new TransferValidator();
    }

    @Test
    void validate_ShouldThrowException_WhenAmountIsZeroOrNegative() {
        final TransferValidationInput input1 = new TransferValidationInput("user1", "account2", BigDecimal.ZERO);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transferValidator.validate(input1));
        assertEquals("Transfer amount must be greater than zero.", exception.getMessage());

        final TransferValidationInput input2 = new TransferValidationInput("user1", "account2", new BigDecimal("-10"));
        exception = assertThrows(IllegalArgumentException.class,
                () -> transferValidator.validate(input2));
        assertEquals("Transfer amount must be greater than zero.", exception.getMessage());
    }

    @Test
    void validate_ShouldThrowException_WhenUserIdOrReceiverCodAccountIsNull() {
        final TransferValidationInput input1 = new TransferValidationInput(null, "account2", BigDecimal.TEN);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transferValidator.validate(input1));
        assertEquals("Sender and receiver information must be provided.", exception.getMessage());

        final TransferValidationInput input2 = new TransferValidationInput("user1", null, BigDecimal.TEN);
        exception = assertThrows(IllegalArgumentException.class,
                () -> transferValidator.validate(input2));
        assertEquals("Sender and receiver information must be provided.", exception.getMessage());
    }

    @Test
    void validate_ShouldNotThrowException_WhenValidInput() {
        final TransferValidationInput input = new TransferValidationInput("user1", "account2", BigDecimal.TEN);
        assertDoesNotThrow(() -> transferValidator.validate(input));
    }
}