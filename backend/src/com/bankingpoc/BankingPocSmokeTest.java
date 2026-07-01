package com.bankingpoc;

import com.bankingpoc.model.TransactionRequest;
import com.bankingpoc.model.TransactionResponse;
import com.bankingpoc.repository.BankRepository;
import com.bankingpoc.service.CardNetworkRouter;
import com.bankingpoc.service.CardProcessor;

import java.math.BigDecimal;
import java.util.Map;

public class BankingPocSmokeTest {
    public static void main(String[] args) {
        BankRepository bankRepository = BankRepository.withSampleData();
        CardProcessor cardProcessor = new CardProcessor(bankRepository);
        CardNetworkRouter cardNetworkRouter = new CardNetworkRouter(cardProcessor, bankRepository);

        expectApproved(cardNetworkRouter, Map.of("cardNumber", "4111111111111111", "pin", "1234", "amount", "100", "type", "withdraw"), "valid withdrawal");
        expectApproved(cardNetworkRouter, Map.of("cardNumber", "4111111111111111", "pin", "1234", "amount", "50", "type", "topup"), "valid top-up");
        expectDeclined(cardNetworkRouter, Map.of("cardNumber", "4999999999999999", "pin", "1234", "amount", "25", "type", "withdraw"), "Invalid card", "invalid card");
        expectDeclined(cardNetworkRouter, Map.of("cardNumber", "4111111111111111", "pin", "9999", "amount", "25", "type", "withdraw"), "Invalid PIN", "invalid PIN");
        expectDeclined(cardNetworkRouter, Map.of("cardNumber", "4222222222222222", "pin", "5678", "amount", "9999", "type", "withdraw"), "Insufficient balance", "insufficient balance");
        expectDeclined(cardNetworkRouter, Map.of("cardNumber", "5111111111111111", "pin", "1234", "amount", "25", "type", "topup"), "Card range not supported", "unsupported card range");

        var newCustomer = bankRepository.createCustomer("Meera Shah", "4333333333333333", "4321");
        BigDecimal openingBalance = bankRepository.findAccountByCustomerId(newCustomer.id()).orElseThrow().balance();
        if (openingBalance.compareTo(new BigDecimal("0.00")) != 0) {
            throw new AssertionError("New customer should have a zero opening balance");
        }
        try {
            bankRepository.createCustomer("Duplicate Card", "4333333333333333", "4321");
            throw new AssertionError("Duplicate card registration should be rejected");
        } catch (IllegalArgumentException expected) {
            // Expected validation outcome.
        }

        if (bankRepository.findAllTransactions().size() != 6) {
            throw new AssertionError("Admin transaction log should include every transaction outcome");
        }
        if (bankRepository.findTransactionsByCustomer("cust-1001").isEmpty()) {
            throw new AssertionError("Customer transaction history should include own approved and declined transactions");
        }
        BigDecimal balance = bankRepository.findAccountByCustomerId("cust-1001").orElseThrow().balance();
        if (balance.compareTo(new BigDecimal("1450.00")) != 0) {
            throw new AssertionError("Unexpected balance for cust-1001: " + balance);
        }

        System.out.println("All banking POC smoke tests passed.");
    }

    private static void expectApproved(CardNetworkRouter cardNetworkRouter, Map<String, String> requestBody, String testName) {
        TransactionResponse response = cardNetworkRouter.route(TransactionRequest.from(requestBody));
        if (!response.approved()) {
            throw new AssertionError(testName + " should be approved but was declined: " + response.reason());
        }
    }

    private static void expectDeclined(CardNetworkRouter cardNetworkRouter, Map<String, String> requestBody, String expectedReason, String testName) {
        TransactionResponse response = cardNetworkRouter.route(TransactionRequest.from(requestBody));
        if (response.approved() || !expectedReason.equals(response.reason())) {
            throw new AssertionError(testName + " expected " + expectedReason + " but got " + response.reason());
        }
    }
}
