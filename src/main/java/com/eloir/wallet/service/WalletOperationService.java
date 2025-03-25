package com.eloir.wallet.service;

import com.eloir.wallet.dto.WalletOperationRequest;
import com.eloir.wallet.enums.TransactionType;
import com.eloir.wallet.strategy.WalletOperationStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WalletOperationService {

    private final Map<TransactionType, WalletOperationStrategy> strategyMap;

    public WalletOperationService(List<WalletOperationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(WalletOperationStrategy::getTransactionType, s -> s));
    }

    public void execute(WalletOperationRequest request) {
        WalletOperationStrategy strategy = strategyMap.get(TransactionType.WITHDRAW);
        strategy.execute(request);
    }
}