package com.eloir.wallet.model;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Getter
@Setter
public class OperationRequest {
    @Min(0)
    @NotNull
    @Parameter(description = "Amount to operation")
    private BigDecimal amount;
}
