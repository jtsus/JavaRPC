package org.kipdev.rpc.types;

import org.kipdev.rpc.RPCController;

import java.util.Arrays;

public class MockMessageController extends RPCController {

    public static MockMessageController INSTANCE = new MockMessageController();

    public void initialize(String pkg) {
        RPCController.INSTANCE = INSTANCE = this;

        super.registerPackage(pkg);
    }

    @Override
    public void publish(String exchange, byte[] data) {
        System.out.printf("Sending message %s to %s\n", Arrays.toString(data), exchange);

        handleMessage(exchange, data);
    }
}
