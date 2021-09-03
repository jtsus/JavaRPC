package org.kipdev.rpc.types;

import org.kipdev.rpc.RPC;
import org.kipdev.rpc.Exchange;

public enum ExampleExchange implements Exchange {
    INSTANCE;

    @RPC
    public void logChatMessage(String sender, String message) {
        System.out.printf("%s: %s\n", sender, message);
    }
}
