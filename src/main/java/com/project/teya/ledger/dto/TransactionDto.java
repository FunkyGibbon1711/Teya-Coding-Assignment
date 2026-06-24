package com.project.teya.ledger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.teya.ledger.model.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionDto {

    private String id;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime time;
}
