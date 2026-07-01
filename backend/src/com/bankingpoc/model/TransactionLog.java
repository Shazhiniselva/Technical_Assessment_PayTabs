package com.bankingpoc.model;

import com.bankingpoc.util.CardMasker;
import com.bankingpoc.util.JsonHelper;
import com.bankingpoc.util.MoneyFormatter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.UUID;

public record TransactionLog(
        String id,
        String timestamp,
        String customerId,
        String maskedCard,
        String transactionType,
        String amount,
        boolean approved,
        String reason,
        String resultingBalance
) {
    public static TransactionLog from(TransactionRequest request, String customerId, boolean approved, String reason, BigDecimal resultingBalance) {
        String visibleCardNumber = request.maskedCard() == null ? CardMasker.mask(request.cardNumber()) : request.maskedCard();
        return new TransactionLog(
                UUID.randomUUID().toString(),
                Instant.now().toString(),
                customerId,
                visibleCardNumber,
                request.transactionType(),
                MoneyFormatter.format(request.amount()),
                approved,
                reason,
                resultingBalance == null ? null : MoneyFormatter.format(resultingBalance)
        );
    }

    public String toJson() {
        LinkedHashMap<String, Object> transaction = new LinkedHashMap<>();
        transaction.put("id", id);
        transaction.put("timestamp", timestamp);
        transaction.put("customerId", customerId);
        transaction.put("maskedCard", maskedCard);
        transaction.put("type", transactionType);
        transaction.put("amount", amount);
        transaction.put("approved", approved);
        transaction.put("reason", reason);
        transaction.put("resultingBalance", resultingBalance);
        return JsonHelper.object(transaction);
    }
}
