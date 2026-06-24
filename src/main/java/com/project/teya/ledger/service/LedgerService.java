package com.project.teya.ledger.service;

import com.project.teya.ledger.dto.CurrentBalanceResponseDto;
import com.project.teya.ledger.dto.TransactionDto;
import com.project.teya.ledger.dto.TransactionHistoryDto;
import com.project.teya.ledger.dto.TransactionRequestDto;

public interface LedgerService {

    public TransactionHistoryDto getTransactionHistory();

    public CurrentBalanceResponseDto getCurrentBalance();

    public TransactionDto performTransaction(TransactionRequestDto request, String txnId);
}
