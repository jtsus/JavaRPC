package org.kipdev.rabbit.types;

import org.kipdev.rabbit.RabbitRPC;
import org.kipdev.rabbit.RabbitExchange;

public enum IndirectRabbitExchange implements RabbitExchange {
    INSTANCE;

    @RabbitRPC
    public void renamePlayer(String name, String newName) {
        System.out.printf("%s has changed their name to %s\n", name, newName);
    }
}
