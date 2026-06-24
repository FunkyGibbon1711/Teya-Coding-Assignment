package com.project.teya.ledger.service;

import com.project.teya.ledger.dto.CurrentBalanceResponseDto;
import com.project.teya.ledger.dto.TransactionDto;
import com.project.teya.ledger.dto.TransactionHistoryDto;
import com.project.teya.ledger.dto.TransactionRequestDto;
import com.project.teya.ledger.exception.ConflictException;
import com.project.teya.ledger.exception.DatabaseException;
import com.project.teya.ledger.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.project.teya.ledger.model.TransactionType.WITHDRAWAL;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final List<TransactionDto> transactions = new ArrayList<>();
    private final Clock clock;
    private BigDecimal balance = BigDecimal.ZERO;

    /* Add support for multiple users? */
    public CurrentBalanceResponseDto getCurrentBalance() throws DatabaseException, NotFoundException {
        return CurrentBalanceResponseDto.builder()
                .currentBalance(balance)
                .time(LocalDateTime.now(clock))
                .build();
    }

    public TransactionHistoryDto getTransactionHistory() throws DatabaseException, NotFoundException {
        return TransactionHistoryDto.builder()
                .transactions(List.copyOf(transactions))
                .build();
    }

    /* Some conditions to consider
     *  1. Trying to withdraw more than we have as a balance (or if balance is just zero)
     *  2. Trying to withdraw a negative amount (or deposit a negative amount)
     *  */
    public synchronized TransactionDto performTransaction(TransactionRequestDto request) throws ConflictException, DatabaseException {

        /* Trying to withdraw more than account balance will throw an error
         *  Not going to allow overdrafts */
        if (request.getType().equals(WITHDRAWAL) && request.getAmount().compareTo(balance) > 0) {
            throw new IllegalArgumentException("Trying to withdraw more than account balance");
        }

        /* If any of the transaction IDs match the current transaction ID being processed
         *  I will throw a ConflictException - this is to effectively account for an idempotency check
         *  to prevent duplicate transactions taking place */
        transactions.forEach(transactionDto -> {
            if (transactionDto.getId().equals(request.getId())) {
                throw new ConflictException("Duplicate transaction received");
            }
        });

        balance = switch (request.getType()) {
            case DEPOSIT -> balance.add(request.getAmount());
            case WITHDRAWAL -> balance.subtract(request.getAmount());
        };

        TransactionDto transaction = TransactionDto.builder()
                .id(request.getId())
                .type(request.getType())
                .amount(request.getAmount())
                .time(LocalDateTime.now(clock))
                .balanceAfter(balance)
                .build();

        transactions.add(transaction);

        return transaction;
    }
}
