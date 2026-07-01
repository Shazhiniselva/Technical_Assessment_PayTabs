package com.bankingpoc.model;

import com.bankingpoc.util.JsonHelper;
import com.bankingpoc.util.MoneyFormatter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public record TransactionResponse(
        boolean approved,
        String transactionId,
        String reason,
        String customerId,
        String maskedCard,
        BigDecimal balance
) {
    public static TransactionResponse approved(String transactionId, String customerId, String maskedCard, BigDecimal balance) {
        return new TransactionResponse(true, transactionId, "Approved", customerId, maskedCard, balance);
    }

    public static TransactionResponse declined(String transactionId, String reason) {
        return new TransactionResponse(false, transactionId, reason, null, null, null);
    }

    public String toJson() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("approved", approved);
        response.put("transactionId", transactionId);
        response.put("reason", reason);
        response.put("customerId", customerId);
        response.put("maskedCard", maskedCard);
        response.put("balance", balance == null ? null : MoneyFormatter.format(balance));
        return JsonHelper.object(response);
    }
}
