package com.bankingpoc.service;

import com.bankingpoc.model.TransactionLog;
import com.bankingpoc.model.TransactionRequest;
import com.bankingpoc.model.TransactionResponse;
import com.bankingpoc.repository.BankRepository;

public class CardNetworkRouter {
    private static final String SUPPORTED_CARD_PREFIX = "4";
    private static final String UNSUPPORTED_CARD_REASON = "Card range not supported";

    private final CardProcessor cardProcessor;
    private final BankRepository bankRepository;

    public CardNetworkRouter(CardProcessor cardProcessor, BankRepository bankRepository) {
        this.cardProcessor = cardProcessor;
        this.bankRepository = bankRepository;
    }

    public TransactionResponse route(TransactionRequest transactionRequest) {
        transactionRequest.validateBasicFields();
        if (!transactionRequest.cardNumber().startsWith(SUPPORTED_CARD_PREFIX)) {
            TransactionLog declinedTransaction = TransactionLog.from(transactionRequest.withoutPlainPin(), null, false, UNSUPPORTED_CARD_REASON, null);
            bankRepository.saveTransaction(declinedTransaction);
            return TransactionResponse.declined(declinedTransaction.id(), UNSUPPORTED_CARD_REASON);
        }
        return cardProcessor.process(transactionRequest);
    }
}
