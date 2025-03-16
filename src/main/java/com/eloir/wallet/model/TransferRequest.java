package com.eloir.wallet.model;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest extends OperationRequest {
    @Parameter(description = "Destination account code")
    private String codAccount;
}
