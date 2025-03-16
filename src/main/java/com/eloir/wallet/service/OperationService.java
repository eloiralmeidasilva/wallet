package com.eloir.wallet.service;

import java.math.BigDecimal;

public interface OperationService {
    void execute(String userId, BigDecimal amount);
}
