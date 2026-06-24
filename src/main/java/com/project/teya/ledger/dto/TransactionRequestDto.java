package com.project.teya.ledger.dto;

import com.project.teya.ledger.model.TransactionType;
import com.project.teya.ledger.validator.ValidAmount;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class TransactionRequestDto {

    @NotNull
    @ValidAmount
    BigDecimal amount;

    @NotNull
    TransactionType type;
}
