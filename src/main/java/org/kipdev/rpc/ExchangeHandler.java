package org.kipdev.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExchangeHandler {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("Rabbit RPC Thread");
        return thread;
    });

    private ExchangeHandler() {}

    public static void sendMessage(Exchange exchange, String method, Object[] values) {
        executor.execute(() -> {
            try {
                ByteArrayOutputStream bos;
                byte[] methodNameBytes = method.getBytes(StandardCharsets.UTF_8);
                bos = new ByteArrayOutputStream(4 + methodNameBytes.length + values.length + values.length * 4);
                ByteUtils.putInt(methodNameBytes.length, bos);
                bos.write(methodNameBytes);
                ByteUtils.putInt(values.length, bos);
                for (Object value : values) {
                    byte[] data = RPCController.INSTANCE.getParser().write(value);
                    ByteUtils.putInt(data.length, bos);
                    bos.write(data);
                }

                RPCController.INSTANCE.publish(RPCController.INSTANCE.getRegisteredChannel(exchange), bos.toByteArray());
            } catch (IOException e) {
                System.err.printf("Could not send message on method %s with parameters %s\n", method, Arrays.toString(values));
                e.printStackTrace();
            }
        });
    }
}
