package com.project.teya.ledger.controller;

import com.project.teya.ledger.dto.CurrentBalanceResponseDto;
import com.project.teya.ledger.dto.TransactionDto;
import com.project.teya.ledger.dto.TransactionHistoryDto;
import com.project.teya.ledger.dto.TransactionRequestDto;
import com.project.teya.ledger.service.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/transactions")
    public TransactionHistoryDto getTransactionHistory() {
        return ledgerService.getTransactionHistory();
    }

    @GetMapping("/balance")
    public CurrentBalanceResponseDto getCurrentBalance() {
        return ledgerService.getCurrentBalance();
    }

    @PostMapping("/transactions")
    public TransactionDto transaction(@Valid @RequestBody TransactionRequestDto request, @RequestHeader String txnId) {
        return ledgerService.performTransaction(request, txnId);
    }
}
