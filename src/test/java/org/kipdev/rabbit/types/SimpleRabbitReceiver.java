package org.kipdev.rabbit.types;

import org.kipdev.rabbit.RabbitRPC;
import org.kipdev.rabbit.RabbitReceiver;

import java.util.UUID;

public enum SimpleRabbitReceiver implements RabbitReceiver {
    INSTANCE;

    @RabbitRPC
    public void synchronizePlayer(UUID id, int bodyCount) {
        System.out.printf("%s's body count updated to %d\n", id, bodyCount);
    }
}
