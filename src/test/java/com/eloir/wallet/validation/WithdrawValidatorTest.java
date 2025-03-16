package com.eloir.wallet.validation;

import com.eloir.wallet.validation.input.WithdrawValidationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WithdrawValidatorTest {

    private WithdrawValidator withdrawValidator;

    @BeforeEach
    void setUp() {
        withdrawValidator = new WithdrawValidator();
    }

    @Test
    void validate_ShouldNotThrowException_WhenAmountIsGreaterThanZero() {
        WithdrawValidationInput input = new WithdrawValidationInput("user123", BigDecimal.TEN);

        assertDoesNotThrow(() -> withdrawValidator.validate(input));
    }

    @Test
    void validate_ShouldThrowException_WhenAmountIsZeroOrNegative() {
        WithdrawValidationInput input = new WithdrawValidationInput("user123", null);

        input.setAmount(BigDecimal.ZERO);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> withdrawValidator.validate(input));
        assertEquals("Withdrawal amount must be greater than zero.", exception.getMessage());

        input.setAmount(BigDecimal.valueOf(-1));
        exception = assertThrows(IllegalArgumentException.class, () -> withdrawValidator.validate(input));
        assertEquals("Withdrawal amount must be greater than zero.", exception.getMessage());
    }

    @Test
    void validate_ShouldThrowException_WhenUserIdIsNull() {
        WithdrawValidationInput input = new WithdrawValidationInput(null, BigDecimal.TEN);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> withdrawValidator.validate(input));
        assertEquals("User ID must be provided.", exception.getMessage());
    }
}