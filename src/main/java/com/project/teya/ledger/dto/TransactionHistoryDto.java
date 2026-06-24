package com.project.teya.ledger.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TransactionHistoryDto {

    List<TransactionDto> transactions;
}
