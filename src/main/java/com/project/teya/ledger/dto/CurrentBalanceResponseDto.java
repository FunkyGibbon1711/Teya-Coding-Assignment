package com.project.teya.ledger.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
public class CurrentBalanceResponseDto {

    private BigDecimal currentBalance;

    private LocalDateTime time;
}
