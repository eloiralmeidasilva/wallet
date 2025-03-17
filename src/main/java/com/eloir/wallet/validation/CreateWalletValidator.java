package com.eloir.wallet.validation;

import com.eloir.wallet.repository.WalletRepository;
import com.eloir.wallet.validation.input.CreateWalletValidationInput;
import org.springframework.stereotype.Component;

@Component
public class CreateWalletValidator implements Validator<CreateWalletValidationInput> {

    private final WalletRepository walletRepository;

    public CreateWalletValidator(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public void validate(CreateWalletValidationInput input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("Invalid input: Deposit information cannot be null.");
        }

        if (walletRepository.findByUserId(input.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User ID already has a registered wallet. It is only possible to have one");
        }
    }
}

