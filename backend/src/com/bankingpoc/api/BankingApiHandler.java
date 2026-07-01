package com.bankingpoc.api;

import com.bankingpoc.exception.BadRequestException;
import com.bankingpoc.model.CardAccount;
import com.bankingpoc.model.Customer;
import com.bankingpoc.model.TransactionRequest;
import com.bankingpoc.repository.BankRepository;
import com.bankingpoc.service.CardNetworkRouter;
import com.bankingpoc.service.CardProcessor;
import com.bankingpoc.util.JsonHelper;
import com.bankingpoc.util.MoneyFormatter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class BankingApiHandler {
    private final BankRepository bankRepository;
    private final CardNetworkRouter cardNetworkRouter;
    private final CardProcessor cardProcessor;

    public BankingApiHandler(BankRepository bankRepository, CardNetworkRouter cardNetworkRouter, CardProcessor cardProcessor) {
        this.bankRepository = bankRepository;
        this.cardNetworkRouter = cardNetworkRouter;
        this.cardProcessor = cardProcessor;
    }

    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange.getResponseHeaders());
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 204, "");
            return;
        }

        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method) && "/api/health".equals(path)) {
                sendJson(exchange, 200, JsonHelper.object(Map.of("status", "ok")));
                return;
            }

            if ("POST".equals(method) && "/api/system1/transactions".equals(path)) {
                TransactionRequest transactionRequest = TransactionRequest.from(JsonHelper.parseObject(readRequestBody(exchange)));
                sendJson(exchange, 200, cardNetworkRouter.route(transactionRequest).toJson());
                return;
            }

            if ("POST".equals(method) && "/api/system2/process".equals(path)) {
                TransactionRequest transactionRequest = TransactionRequest.from(JsonHelper.parseObject(readRequestBody(exchange)));
                sendJson(exchange, 200, cardProcessor.process(transactionRequest).toJson());
                return;
            }

            if ("GET".equals(method) && "/api/admin/transactions".equals(path)) {
                sendJson(exchange, 200, JsonHelper.array(bankRepository.findAllTransactions().stream().map(transaction -> transaction.toJson()).toList()));
                return;
            }

            if ("GET".equals(method) && "/api/customers".equals(path)) {
                sendJson(exchange, 200, JsonHelper.array(bankRepository.findAllCustomers().stream().map(customer -> customer.toJson()).toList()));
                return;
            }

            if ("POST".equals(method) && "/api/customers".equals(path)) {
                createCustomer(exchange);
                return;
            }

            if ("GET".equals(method) && path.matches("/api/customers/[^/]+/transactions")) {
                String customerId = pathSegment(path, 3);
                sendJson(exchange, 200, JsonHelper.array(bankRepository.findTransactionsByCustomer(customerId).stream().map(transaction -> transaction.toJson()).toList()));
                return;
            }

            if ("GET".equals(method) && path.matches("/api/customers/[^/]+/balance")) {
                sendCustomerBalance(exchange, pathSegment(path, 3));
                return;
            }

            if ("POST".equals(method) && path.matches("/api/customers/[^/]+/topups")) {
                sendCustomerTopUp(exchange, pathSegment(path, 3));
                return;
            }

            sendJson(exchange, 404, JsonHelper.object(Map.of("error", "Endpoint not found")));
        } catch (BadRequestException exception) {
            sendJson(exchange, 400, JsonHelper.object(Map.of("approved", false, "reason", exception.getMessage())));
        } catch (Exception exception) {
            sendJson(exchange, 500, JsonHelper.object(Map.of("approved", false, "reason", "Internal error")));
        }
    }

    private void sendCustomerBalance(HttpExchange exchange, String customerId) throws IOException {
        Optional<CardAccount> cardAccount = bankRepository.findAccountByCustomerId(customerId);
        if (cardAccount.isEmpty()) {
            sendJson(exchange, 404, JsonHelper.object(Map.of("approved", false, "reason", "Customer not found")));
            return;
        }

        sendJson(exchange, 200, JsonHelper.object(Map.of(
                "customerId", customerId,
                "customerName", bankRepository.findCustomerName(customerId).orElse("Unknown"),
                "maskedCard", cardAccount.get().maskedCard(),
                "balance", MoneyFormatter.format(cardAccount.get().balance())
        )));
    }

    private void createCustomer(HttpExchange exchange) throws IOException {
        Map<String, String> requestBody = JsonHelper.parseObject(readRequestBody(exchange));
        try {
            Customer customer = bankRepository.createCustomer(
                    requestBody.get("name"),
                    requestBody.get("cardNumber"),
                    requestBody.get("pin")
            );
            CardAccount account = bankRepository.findAccountByCustomerId(customer.id()).orElseThrow();
            sendJson(exchange, 201, JsonHelper.object(Map.of(
                    "id", customer.id(),
                    "name", customer.name(),
                    "maskedCard", account.maskedCard(),
                    "balance", MoneyFormatter.format(account.balance())
            )));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }

    private void sendCustomerTopUp(HttpExchange exchange, String customerId) throws IOException {
        Map<String, String> requestBody = JsonHelper.parseObject(readRequestBody(exchange));
        Optional<CardAccount> cardAccount = bankRepository.findAccountByCustomerId(customerId);
        if (cardAccount.isEmpty()) {
            sendJson(exchange, 404, JsonHelper.object(Map.of("approved", false, "reason", "Customer not found")));
            return;
        }

        requestBody.put("cardNumber", cardAccount.get().demoCardNumber());
        requestBody.put("type", "topup");
        TransactionRequest transactionRequest = TransactionRequest.from(requestBody);
        sendJson(exchange, 200, cardNetworkRouter.route(transactionRequest).toJson());
    }

    private static void addCorsHeaders(Headers headers) {
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String pathSegment(String path, int index) {
        return path.split("/")[index];
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        send(exchange, status, body);
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, responseBytes.length);
        try (OutputStream response = exchange.getResponseBody()) {
            response.write(responseBytes);
        }
    }
}
