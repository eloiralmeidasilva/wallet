package com.eloir.wallet.dto;

import java.math.BigDecimal;

public record WalletResponse(
        String codAccount,
        BigDecimal amount
) {}
