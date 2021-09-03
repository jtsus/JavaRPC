package org.kipdev.rpc.types;

import org.kipdev.rpc.Exchange;
import org.kipdev.rpc.RPC;

public enum CustomSendExchange implements Exchange {
    INSTANCE;

    @RPC
    public void ping(String source) {
        System.out.printf("Ping received from %s\n", source);
    }

    @Override
    public void sendMessage(String method, Object[] values) {
        System.out.println("Sent with style!");
        Exchange.super.sendMessage(method, values);
    }
}
