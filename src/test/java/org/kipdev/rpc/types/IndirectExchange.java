package org.kipdev.rpc.types;

import org.kipdev.rpc.RPC;
import org.kipdev.rpc.Exchange;

public enum IndirectExchange implements Exchange {
    INSTANCE;

    @RPC
    public void renamePlayer(String name, String newName) {
        System.out.printf("%s has changed their name to %s\n", name, newName);
    }
}
