package com.project.teya.ledger.integration;

import com.project.teya.ledger.TestUtils;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureRestTestClient
public class LedgerApplicationIT {

    @Autowired
    RestTestClient restTestClient;

    @Test
    @DirtiesContext
    @DisplayName("When no transaction history, ensure no data returned")
    public void testGetTransactionHistory() throws JSONException, IOException {
        String response = restTestClient.get()
                .uri("/api/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        JSONAssert.assertEquals(TestUtils.loadFileAsString("data/integration/response/notransactionhistory.json"),
                response, false);
    }

    @Test
    @DirtiesContext
    @DisplayName("When no balance in account, ensure withdrawals are prevented")
    public void testOverdraft() throws JSONException, IOException {
        String response = restTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(TestUtils.loadFileAsString("data/integration/request/withdrawalrequest100.json"))
                .header("txnId", "txn1")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST.value())
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        JSONAssert.assertEquals(TestUtils.loadFileAsString("data/400.json"), response, false);
    }

    @Test
    @DirtiesContext
    @DisplayName("When a transaction with the same txnId is received, ensure 409 CONFLICT is returned")
    public void testDuplicateTransaction() throws JSONException, IOException {
        restTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(TestUtils.loadFileAsString("data/integration/request/depositrequest100.json"))
                .header("txnId", "txn1")
                .exchange()
                .expectStatus()
                .isOk();

        restTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(TestUtils.loadFileAsString("data/integration/request/withdrawalrequest100.json"))
                .header("txnId", "txn2")
                .exchange()
                .expectStatus()
                .isOk();

        /* Duplicate the deposit request from before */
        restTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(TestUtils.loadFileAsString("data/integration/request/depositrequest100.json"))
                .header("txnId", "txn1")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    @DirtiesContext
    @DisplayName("When multiple transactions made, ensure balance handled correctly and transaction history accurate")
    public void testMultipleTransactions() throws JSONException, IOException {
        String depositResponse = restTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(TestUtils.loadFileAsString("data/integration/request/depositrequest100.json"))
                .header("txnId", "txn1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        String withdrawalResponse = restTestClient.post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(TestUtils.loadFileAsString("data/integration/request/withdrawalrequest100.json"))
                .header("txnId", "txn2")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        String currentBalance = restTestClient.get()
                .uri("/api/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        String transactionHistory = restTestClient.get()
                .uri("/api/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertAll(
                () -> JSONAssert.assertEquals(TestUtils.loadFileAsString("data/integration/response/depositresponse100.json"), depositResponse, false),
                () -> JSONAssert.assertEquals(TestUtils.loadFileAsString("data/integration/response/withdrawalresponse100.json"), withdrawalResponse, false),
                () -> JSONAssert.assertEquals(TestUtils.loadFileAsString("data/integration/response/currentbalance0.json"), currentBalance, false),
                () -> JSONAssert.assertEquals(TestUtils.loadFileAsString("data/integration/response/transactionhistory2transactions.json"), transactionHistory, false)
        );
    }

    @TestConfiguration
    public static class TestConfig {
        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.parse("2026-06-23T19:00:00.000000Z"), ZoneId.systemDefault());
        }
    }
}
