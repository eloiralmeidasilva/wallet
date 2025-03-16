package com.eloir.wallet.validation;

import com.eloir.wallet.validation.input.DepositValidationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DepositValidatorTest {

    private DepositValidator depositValidator;

    @BeforeEach
    void setUp() {
        depositValidator = new DepositValidator();
    }

    @Test
    void validate_ShouldThrowException_WhenInputIsNull() {
        final DepositValidationInput input = null;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> depositValidator.validate(input));
        assertEquals("Invalid input: Deposit information cannot be null.", exception.getMessage());
    }

    @Test
    void validate_ShouldThrowException_WhenAmountIsZeroOrNegative() {
        final DepositValidationInput input1 = new DepositValidationInput("user1", BigDecimal.ZERO);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> depositValidator.validate(input1));
        assertEquals("Deposit amount must be greater than zero.", exception.getMessage());

        final DepositValidationInput input2 = new DepositValidationInput("user1", new BigDecimal("-10"));
        exception = assertThrows(IllegalArgumentException.class,
                () -> depositValidator.validate(input2));
        assertEquals("Deposit amount must be greater than zero.", exception.getMessage());
    }

    @Test
    void validate_ShouldThrowException_WhenUserIdIsNullOrEmpty() {
        final DepositValidationInput input1 = new DepositValidationInput(null, BigDecimal.TEN);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> depositValidator.validate(input1));
        assertEquals("User ID cannot be null or empty.", exception.getMessage());

        final DepositValidationInput input2 = new DepositValidationInput("", BigDecimal.TEN);
        exception = assertThrows(IllegalArgumentException.class,
                () -> depositValidator.validate(input2));
        assertEquals("User ID cannot be null or empty.", exception.getMessage());
    }

    @Test
    void validate_ShouldNotThrowException_WhenValidInput() {
        final DepositValidationInput input = new DepositValidationInput("user1", BigDecimal.TEN);
        assertDoesNotThrow(() -> depositValidator.validate(input));
    }
}