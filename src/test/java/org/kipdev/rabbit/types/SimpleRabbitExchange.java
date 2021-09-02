package org.kipdev.rabbit.types;

import org.kipdev.rabbit.RabbitRPC;
import org.kipdev.rabbit.RabbitExchange;

import java.util.UUID;

public enum SimpleRabbitExchange implements RabbitExchange {
    INSTANCE;

    @RabbitRPC
    public void synchronizePlayer(UUID id, int bodyCount) {
        System.out.printf("%s's body count updated to %d\n", id, bodyCount);
    }
}
