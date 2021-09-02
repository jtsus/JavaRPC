package org.kipdev.rabbit.types;

import org.kipdev.rabbit.RabbitRPC;
import org.kipdev.rabbit.RabbitReceiver;

import java.util.UUID;

public enum IndirectRabbitReceiver implements RabbitReceiver {
    INSTANCE;

    @RabbitRPC
    public void renamePlayer(String name, String newName) {
        System.out.printf("%s has changed their name to %s\n", name, newName);
    }
}
