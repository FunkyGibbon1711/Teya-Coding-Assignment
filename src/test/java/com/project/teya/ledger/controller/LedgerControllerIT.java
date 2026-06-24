package com.project.teya.ledger.controller;

import com.project.teya.ledger.TestUtils;
import com.project.teya.ledger.dto.CurrentBalanceResponseDto;
import com.project.teya.ledger.dto.TransactionDto;
import com.project.teya.ledger.dto.TransactionHistoryDto;
import com.project.teya.ledger.dto.TransactionRequestDto;
import com.project.teya.ledger.exception.ConflictException;
import com.project.teya.ledger.exception.DatabaseException;
import com.project.teya.ledger.exception.NotFoundException;
import com.project.teya.ledger.model.TransactionType;
import com.project.teya.ledger.service.LedgerService;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureRestTestClient
public class LedgerControllerIT {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");

    @Autowired
    RestTestClient restTestClient;

    @MockitoBean
    LedgerService ledgerService;

    @Nested
    public class getCurrentBalanceTests {

        @Test
        @DisplayName("When ledger service successfully returns balance data, 200 OK response received with correct body")
        public void testSuccessfulRetrieval() throws IOException, JSONException {
            LocalDateTime time = LocalDateTime.now();

            when(ledgerService.getCurrentBalance())
                    .thenReturn(CurrentBalanceResponseDto.builder()
                            .currentBalance(BigDecimal.valueOf(123L))
                            .time(time)
                            .build());

            String response = restTestClient.get()
                    .uri("/api/balance")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONAssert.assertEquals(TestUtils.loadFileAndTemplate("data/controller/response/currentbalance.json",
                    formatter.format(time)), response, false);
        }

        /* This test is to simulate an error with the database
         *  Obviously my implementation doesn't use a database, but this is just to showcase that I know how to handle
         *  exceptions correctly for a Spring Boot application
         *  */
        @Test
        @DisplayName("When ledger service throws a DatabaseException, 500 Internal Server Error response received with correct body")
        public void testInternalServerError() throws IOException, JSONException {
            when(ledgerService.getCurrentBalance()).thenThrow(new DatabaseException("Error performing database query"));

            String response = restTestClient.get()
                    .uri("/api/balance")
                    .exchange()
                    .expectStatus()
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONAssert.assertEquals(TestUtils.loadFileAsString("data/500.json"), response, false);
        }

        @Test
        @DisplayName("When ledger service throws a NotFoundException, 404 Not Found response received with correct body")
        public void testNotFound() throws IOException, JSONException {
            when(ledgerService.getCurrentBalance()).thenThrow(new NotFoundException("Entity not found"));

            String response = restTestClient.get()
                    .uri("/api/balance")
                    .exchange()
                    .expectStatus()
                    .isEqualTo(HttpStatus.NOT_FOUND.value())
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONAssert.assertEquals(TestUtils.loadFileAsString("data/404.json"), response, false);
        }
    }

    @Nested
    public class getTransactionHistoryTests {

        @Test
        @DisplayName("When transaction data successfully retrieved, ensure 200 OK response and correct response body")
        public void successfullyGetTransactionHistory() throws IOException, JSONException {
            LocalDateTime earlyTime = LocalDateTime.now();
            LocalDateTime lateTime = LocalDateTime.now();

            TransactionDto trans1 = TransactionDto.builder()
                    .id("txn1")
                    .type(TransactionType.DEPOSIT)
                    .amount(BigDecimal.valueOf(100L))
                    .balanceAfter(BigDecimal.valueOf(100L))
                    .time(earlyTime)
                    .build();

            TransactionDto trans2 = TransactionDto.builder()
                    .id("txn2")
                    .type(TransactionType.WITHDRAWAL)
                    .amount(BigDecimal.valueOf(100L))
                    .balanceAfter(BigDecimal.ZERO)
                    .time(lateTime)
                    .build();

            when(ledgerService.getTransactionHistory())
                    .thenReturn(TransactionHistoryDto.builder()
                            .transactions(new ArrayList<>(List.of(trans1, trans2)))
                            .build());

            String response = restTestClient.get()
                    .uri("/api/transactions")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONAssert.assertEquals(
                    TestUtils.loadFileAndTemplate("data/controller/response/gettransactionhistory.json",
                            formatter.format(earlyTime), formatter.format(lateTime)), response, false);
        }

        /* This test is to simulate an error with the database
         *  Obviously my implementation doesn't use a database, but this is just to showcase that I know how to handle
         *  exceptions correctly for a Spring Boot application
         *  */
        @Test
        @DisplayName("When ledger service throws a DatabaseException, 500 Internal Server Error response received with correct body")
        public void testInternalServerError() throws IOException, JSONException {
            when(ledgerService.getTransactionHistory()).thenThrow(new DatabaseException("Error performing database query"));

            String response = restTestClient.get()
                    .uri("/api/transactions")
                    .exchange()
                    .expectStatus()
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONAssert.assertEquals(TestUtils.loadFileAsString("data/500.json"), response, false);
        }

        @Test
        @DisplayName("When ledger service throws a NotFoundException, 404 Not Found response received with correct body")
        public void testNotFound() throws IOException, JSONException {
            when(ledgerService.getTransactionHistory()).thenThrow(new NotFoundException("Entity not found"));

            String response = restTestClient.get()
                    .uri("/api/transactions")
                    .exchange()
                    .expectStatus()
                    .isEqualTo(HttpStatus.NOT_FOUND.value())
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONAssert.assertEquals(TestUtils.loadFileAsString("data/404.json"), response, false);
        }
    }

    @Nested
    public class makeTransaction {

        @Test
        @DisplayName("When valid transaction request made, ensure 200 OK response returned with transaction details")
        public void makeSuccessfulTransaction() throws IOException, JSONException {
            LocalDateTime time = LocalDateTime.now();

            when(ledgerService.performTransaction(any(TransactionRequestDto.class)))
                    .thenReturn(TransactionDto.builder()
                            .id("txn1")
                            .type(TransactionType.DEPOSIT)
                            .amount(BigDecimal.valueOf(100L))
                            .balanceAfter(BigDecimal.valueOf(100L))
                            .time(time)
                            .build());

            String response = restTestClient.post()
                    .uri("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(TestUtils.loadFileAsString("data/controller/request/transaction.json"))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONAssert.assertEquals(response,
                    TestUtils.loadFileAndTemplate("data/controller/response/completedtransaction.json",
                            formatter.format(time)), false);

        }

        /* For the sake of speed I will omit testing for DatabaseException handling and possible not found errors as well */
        @Test
        @DisplayName("When ConflictException thrown, ensure 409 response returned with correct body")
        public void testConflictedTransaction() throws IOException, JSONException {
            when(ledgerService.performTransaction(any(TransactionRequestDto.class)))
                    .thenThrow(new ConflictException("Duplicated payment request being made"));

            String response = restTestClient.post()
                    .uri("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(TestUtils.loadFileAsString("data/controller/request/transaction.json"))
                    .exchange()
                    .expectStatus()
                    .isEqualTo(HttpStatus.CONFLICT.value())
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONAssert.assertEquals(TestUtils.loadFileAsString("data/409.json"), response, false);
        }

        @Test
        @DisplayName("When negative transaction amount provided, ensure 400 BAD Request returned")
        public void testNegativeTransAmount() throws IOException, JSONException {
            String response = restTestClient.post()
                    .uri("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(TestUtils.loadFileAsString("data/controller/request/negativetransaction.json"))
                    .exchange()
                    .expectStatus()
                    .isEqualTo(HttpStatus.BAD_REQUEST.value())
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONAssert.assertEquals(TestUtils.loadFileAsString("data/400.json"), response, false);
        }

        @Test
        @DisplayName("When NULL field provided where not expected, ensure 400 BAD REQUEST returned ")
        public void testNullTransAmount() throws IOException, JSONException {
            String response = restTestClient.post()
                    .uri("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(TestUtils.loadFileAsString("data/controller/request/nulltransaction.json"))
                    .exchange()
                    .expectStatus()
                    .isEqualTo(HttpStatus.BAD_REQUEST.value())
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            JSONAssert.assertEquals(TestUtils.loadFileAsString("data/400.json"), response, false);
        }
    }
}
