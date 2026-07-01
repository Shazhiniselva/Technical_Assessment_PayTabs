package com.bankingpoc.model;

import com.bankingpoc.exception.BadRequestException;
import com.bankingpoc.util.CardMasker;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

public record TransactionRequest(
        String cardNumber,
        String pin,
        BigDecimal amount,
        String transactionType,
        String maskedCard
) {
    public static TransactionRequest from(Map<String, String> input) {
        String amountText = input.get("amount");
        BigDecimal requestedAmount = null;
        if (amountText != null && !amountText.isBlank()) {
            try {
                requestedAmount = new BigDecimal(amountText);
            } catch (NumberFormatException exception) {
                throw new BadRequestException("amount must be numeric");
            }
        }

        String type = input.get("type") == null ? null : input.get("type").toLowerCase(Locale.ROOT);
        return new TransactionRequest(input.get("cardNumber"), input.get("pin"), requestedAmount, type, null);
    }

    public void validateBasicFields() {
        if (isBlank(cardNumber) || isBlank(pin) || amount == null || isBlank(transactionType)) {
            throw new BadRequestException("cardNumber, pin, amount, and type are required");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("amount must be positive");
        }
        if (!"withdraw".equals(transactionType) && !"topup".equals(transactionType)) {
            throw new BadRequestException("type must be either withdraw or topup");
        }
    }

    public TransactionRequest withMaskedCard(String visibleCardNumber) {
        return new TransactionRequest(cardNumber, pin, amount, transactionType, visibleCardNumber);
    }

    public TransactionRequest withoutPlainPin() {
        String visibleCardNumber = maskedCard == null ? CardMasker.mask(cardNumber) : maskedCard;
        return new TransactionRequest(cardNumber, null, amount, transactionType, visibleCardNumber);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
