package com.eloir.wallet.service;

import com.eloir.wallet.dto.StatementResponse;
import com.eloir.wallet.dto.WalletResponse;
import com.eloir.wallet.entity.Transaction;
import com.eloir.wallet.entity.Wallet;
import com.eloir.wallet.repository.TransactionRepository;
import com.eloir.wallet.repository.WalletRepository;
import com.eloir.wallet.validation.CreateWalletValidator;
import com.eloir.wallet.validation.input.CreateWalletValidationInput;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class WalletService {

    public static final String ACCOUNT_TYPE = "01";
    private final WalletRepository walletRepository;
    private final CreateWalletValidator validator;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository,
                         CreateWalletValidator validator, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.validator = validator;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Wallet createWallet(String userId) {
        CreateWalletValidationInput validationInput = new CreateWalletValidationInput(userId, null);
        validator.validate(validationInput);
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);

        setRandomCodAccount(wallet);

        return walletRepository.save(wallet);
    }

    public void setRandomCodAccount(Wallet wallet) {
        String year = String.valueOf(LocalDate.now().getYear());

        Integer maxNumAccount = walletRepository.findMaxNumAccountWithLock();
        int newNumAccount = (maxNumAccount == null) ? 1 : maxNumAccount + 1;

        String formattedNumAccount = String.format("%08d", newNumAccount); //regex to get 8 caracters to make a new account
        String accountType = ACCOUNT_TYPE;

        wallet.setNumAccount(newNumAccount);
        // Create on format new account "yyyy.00000001-01"
        wallet.setCodAccount(year + "." + formattedNumAccount + "-" + accountType);
    }

    @Transactional
    public WalletResponse getBalance(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));
        return new WalletResponse(wallet.getCodAccount(), wallet.getBalance());
    }

    @Transactional(readOnly = true)
    public List<StatementResponse> getStatement(String userId, LocalDate startDate, LocalDate endDate) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found for userId: " + userId));

        List<Transaction> transactions = transactionRepository.findByWalletAndCreatedAtBetween(wallet, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());

        return transactions.stream()
                .map(tx -> new StatementResponse(
                        tx.getId(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getFinalBalance(),
                        tx.getCreatedAt()
                ))
                .toList();
    }
}