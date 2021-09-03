package org.kipdev.rpc.types;

import org.kipdev.rpc.RPC;
import org.kipdev.rpc.Exchange;

import java.util.UUID;

public enum SimpleExchange implements Exchange {
    INSTANCE;

    @RPC
    public void synchronizePlayer(UUID id, int bodyCount) {
        System.out.printf("%s's body count updated to %d\n", id, bodyCount);
    }
}
