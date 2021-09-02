package org.kipdev.rabbit.types;

import org.kipdev.rabbit.RabbitRPC;
import org.kipdev.rabbit.RabbitExchange;

public enum ExampleExchange implements RabbitExchange {
    INSTANCE;

    @RabbitRPC
    public void logChatMessage(String sender, String message) {
        System.out.printf("%s: %s\n", sender, message);
    }
}
