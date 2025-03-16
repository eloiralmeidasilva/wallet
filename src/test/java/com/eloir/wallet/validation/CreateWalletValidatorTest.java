package com.eloir.wallet.validation;

import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.repository.WalletRepository;
import com.eloir.wallet.validation.input.CreateWalletValidationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateWalletValidatorTest {

    private CreateWalletValidator createWalletValidator;
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        createWalletValidator = new CreateWalletValidator(walletRepository);
    }

    @Test
    void validate_ShouldThrowException_WhenInputIsNull() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> createWalletValidator.validate(null));
        assertEquals("Invalid input: Deposit information cannot be null.", exception.getMessage());
    }

    @Test
    void validate_ShouldThrowException_WhenUserIdIsNullOrEmpty() {
        final CreateWalletValidationInput input1 = new CreateWalletValidationInput(null, BigDecimal.TEN);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> createWalletValidator.validate(input1));
        assertEquals("User ID cannot be null or empty.", exception.getMessage());

        final CreateWalletValidationInput input2 = new CreateWalletValidationInput("", BigDecimal.TEN);
        exception = assertThrows(IllegalArgumentException.class,
                () -> createWalletValidator.validate(input2));
        assertEquals("User ID cannot be null or empty.", exception.getMessage());
    }

    @Test
    void validate_ShouldThrowException_WhenUserAlreadyHasAWallet() {
        String userId = "user123";
        CreateWalletValidationInput input = new CreateWalletValidationInput(userId, BigDecimal.TEN);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(new Wallet()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> createWalletValidator.validate(input));
        assertEquals("User ID already has a registered wallet. It is only possible to have one", exception.getMessage());
    }

    @Test
    void validate_ShouldNotThrowException_WhenInputIsValid() {
        String userId = "user123";
        CreateWalletValidationInput input = new CreateWalletValidationInput(userId, BigDecimal.TEN);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> createWalletValidator.validate(input));
    }
}
