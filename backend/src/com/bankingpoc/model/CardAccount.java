package com.bankingpoc.model;

import java.math.BigDecimal;

public class CardAccount {
    private final String customerId;
    private final String cardHash;
    private final String pinHash;
    private final String maskedCard;
    private final String demoCardNumber;
    private BigDecimal balance;

    public CardAccount(String customerId, String cardHash, String pinHash, String maskedCard, BigDecimal balance, String demoCardNumber) {
        this.customerId = customerId;
        this.cardHash = cardHash;
        this.pinHash = pinHash;
        this.maskedCard = maskedCard;
        this.balance = balance;
        this.demoCardNumber = demoCardNumber;
    }

    public String customerId() {
        return customerId;
    }

    public String cardHash() {
        return cardHash;
    }

    public String pinHash() {
        return pinHash;
    }

    public String maskedCard() {
        return maskedCard;
    }

    public String demoCardNumber() {
        return demoCardNumber;
    }

    public BigDecimal balance() {
        return balance;
    }

    public void withdraw(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void topUp(BigDecimal amount) {
        balance = balance.add(amount);
    }
}
