package com.bankingpoc.service;

import com.bankingpoc.model.CardAccount;
import com.bankingpoc.model.TransactionLog;
import com.bankingpoc.model.TransactionRequest;
import com.bankingpoc.model.TransactionResponse;
import com.bankingpoc.repository.BankRepository;
import com.bankingpoc.util.Hashing;

import java.util.Optional;

public class CardProcessor {
    private final BankRepository bankRepository;

    public CardProcessor(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    public TransactionResponse process(TransactionRequest transactionRequest) {
        transactionRequest.validateBasicFields();

        String cardHash = Hashing.sha256(transactionRequest.cardNumber());
        Optional<CardAccount> possibleCardAccount = bankRepository.findAccountByCardHash(cardHash);
        if (possibleCardAccount.isEmpty()) {
            return decline(transactionRequest, "Invalid card", null);
        }

        CardAccount cardAccount = possibleCardAccount.get();
        if (!Hashing.sha256(transactionRequest.pin()).equals(cardAccount.pinHash())) {
            return decline(transactionRequest.withMaskedCard(cardAccount.maskedCard()), "Invalid PIN", cardAccount.customerId());
        }

        synchronized (cardAccount) {
            if ("withdraw".equals(transactionRequest.transactionType()) && cardAccount.balance().compareTo(transactionRequest.amount()) < 0) {
                return decline(transactionRequest.withMaskedCard(cardAccount.maskedCard()), "Insufficient balance", cardAccount.customerId());
            }

            if ("withdraw".equals(transactionRequest.transactionType())) {
                cardAccount.withdraw(transactionRequest.amount());
            } else {
                cardAccount.topUp(transactionRequest.amount());
            }

            TransactionLog approvedTransaction = TransactionLog.from(
                    transactionRequest.withMaskedCard(cardAccount.maskedCard()),
                    cardAccount.customerId(),
                    true,
                    "Approved",
                    cardAccount.balance()
            );
            bankRepository.saveTransaction(approvedTransaction);
            return TransactionResponse.approved(approvedTransaction.id(), cardAccount.customerId(), cardAccount.maskedCard(), cardAccount.balance());
        }
    }

    private TransactionResponse decline(TransactionRequest transactionRequest, String reason, String customerId) {
        TransactionLog declinedTransaction = TransactionLog.from(transactionRequest.withoutPlainPin(), customerId, false, reason, null);
        bankRepository.saveTransaction(declinedTransaction);
        return TransactionResponse.declined(declinedTransaction.id(), reason);
    }
}
