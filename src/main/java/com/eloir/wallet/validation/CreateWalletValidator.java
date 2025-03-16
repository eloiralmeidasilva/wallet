package com.eloir.wallet.validation;

import com.eloir.wallet.repository.WalletRepository;
import com.eloir.wallet.validation.input.DepositValidationInput;
import org.springframework.stereotype.Component;

@Component
public class CreateWalletValidator implements Validator<DepositValidationInput> {

    private final WalletRepository walletRepository;

    public CreateWalletValidator(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public void validate(DepositValidationInput input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("Invalid input: Deposit information cannot be null.");
        }

        if (input.getUserId() == null || input.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        } else if (walletRepository.findByUserId(input.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User ID already has a registered wallet. It is only possible to have one");
        }
    }
}

