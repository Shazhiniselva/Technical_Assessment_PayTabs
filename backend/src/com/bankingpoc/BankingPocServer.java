package com.bankingpoc;

import com.bankingpoc.api.BankingApiHandler;
import com.bankingpoc.repository.BankRepository;
import com.bankingpoc.service.CardNetworkRouter;
import com.bankingpoc.service.CardProcessor;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BankingPocServer {
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        BankRepository bankRepository = BankRepository.withSampleData();
        CardProcessor cardProcessor = new CardProcessor(bankRepository);
        CardNetworkRouter cardNetworkRouter = new CardNetworkRouter(cardProcessor, bankRepository);
        BankingApiHandler apiHandler = new BankingApiHandler(bankRepository, cardNetworkRouter, cardProcessor);

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        httpServer.createContext("/", apiHandler::handle);
        httpServer.setExecutor(null);
        httpServer.start();

        System.out.println("Banking POC API running at http://localhost:" + SERVER_PORT);
    }
}
