package com.bankingpoc.repository;

import com.bankingpoc.model.CardAccount;
import com.bankingpoc.model.Customer;
import com.bankingpoc.model.TransactionLog;
import com.bankingpoc.util.CardMasker;
import com.bankingpoc.util.Hashing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;

public class BankRepository {
    private final Map<String, CardAccount> accountsByCardHash = new ConcurrentHashMap<>();
    private final Map<String, Customer> customersById = new ConcurrentHashMap<>();
    private final List<TransactionLog> transactionHistory = new CopyOnWriteArrayList<>();

    public static BankRepository withSampleData() {
        BankRepository bankRepository = new BankRepository();
        bankRepository.saveCustomer(new Customer("cust-1001", "Asha Rao"));
        bankRepository.saveCustomer(new Customer("cust-1002", "Rahul Mehta"));
        bankRepository.saveCardAccount("cust-1001", "4111111111111111", "1234", new BigDecimal("1500.00"));
        bankRepository.saveCardAccount("cust-1002", "4222222222222222", "5678", new BigDecimal("750.00"));
        return bankRepository;
    }

    public void saveCustomer(Customer customer) {
        customersById.put(customer.id(), customer);
    }

    public void saveCardAccount(String customerId, String cardNumber, String plainPin, BigDecimal openingBalance) {
        String cardHash = Hashing.sha256(cardNumber);
        CardAccount cardAccount = new CardAccount(
                customerId,
                cardHash,
                Hashing.sha256(plainPin),
                CardMasker.mask(cardNumber),
                openingBalance,
                cardNumber
        );
        accountsByCardHash.put(cardHash, cardAccount);
    }

    public Optional<CardAccount> findAccountByCardHash(String cardHash) {
        return Optional.ofNullable(accountsByCardHash.get(cardHash));
    }

    public Optional<CardAccount> findAccountByCustomerId(String customerId) {
        return accountsByCardHash.values().stream()
                .filter(cardAccount -> cardAccount.customerId().equals(customerId))
                .findFirst();
    }

    public List<TransactionLog> findAllTransactions() {
        return new ArrayList<>(transactionHistory).reversed();
    }

    public List<TransactionLog> findTransactionsByCustomer(String customerId) {
        return transactionHistory.stream()
                .filter(transaction -> customerId.equals(transaction.customerId()))
                .toList()
                .reversed();
    }

    public List<Customer> findAllCustomers() {
        return new ArrayList<>(customersById.values());
    }

    public Optional<String> findCustomerName(String customerId) {
        return Optional.ofNullable(customersById.get(customerId)).map(Customer::name);
    }

    public void saveTransaction(TransactionLog transactionLog) {
        transactionHistory.add(transactionLog);
    }

    public synchronized Customer createCustomer(String name, String cardNumber, String plainPin) {
        String normalizedName = name == null ? "" : name.trim();
        String normalizedCardNumber = cardNumber == null ? "" : cardNumber.replaceAll("\\s+", "");

        if (normalizedName.length() < 2 || normalizedName.length() > 80) {
            throw new IllegalArgumentException("Name must be between 2 and 80 characters");
        }
        if (!normalizedCardNumber.matches("4\\d{12,18}")) {
            throw new IllegalArgumentException("Card number must contain 13 to 19 digits and start with 4");
        }
        if (plainPin == null || !plainPin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must contain exactly 4 digits");
        }

        String cardHash = Hashing.sha256(normalizedCardNumber);
        if (accountsByCardHash.containsKey(cardHash)) {
            throw new IllegalArgumentException("Card number is already registered");
        }

        String customerId = "cust-" + UUID.randomUUID().toString().substring(0, 8);
        Customer customer = new Customer(customerId, normalizedName);
        saveCustomer(customer);
        saveCardAccount(customerId, normalizedCardNumber, plainPin, BigDecimal.ZERO.setScale(2));
        return customer;
    }
}
