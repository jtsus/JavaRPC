package org.kipdev.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ExchangeHandler {
    private ExchangeHandler() {}

    public static void sendMessage(Exchange exchange, String method, Object[] values) {
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
            throw new RuntimeException(String.format("Could not send message on method %s with parameters %s", method, Arrays.toString(values)), e);
        }
    }
}
