package org.kipdev.rpc;

import net.pixelverse.gson.internal.Primitives;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public interface Exchange {

    default void receiveMessage(byte[] message) throws Exception {
        ByteBuffer buf = ByteBuffer.wrap(message);
        int nameLen = buf.getInt();
        byte[] nameData = new byte[nameLen];
        buf.get(nameData);
        String methodName = new String(nameData);
        int paramCount = buf.getInt();
        Object[] paramValues = new Object[paramCount];
        for (int i = 0; i < paramCount; i++) {
            int dataSize = buf.getInt();
            byte[] paramData = new byte[dataSize];
            buf.get(paramData);
            paramValues[i] = RPCController.INSTANCE.getParser().parse(paramData);
        }

        receiveMessage(methodName, paramValues);
    }

    default void receiveMessage(String method, Object... values) throws Exception {
        for (Method declaredMethod : getClass().getDeclaredMethods()) {
            if (declaredMethod.getName().equals(method + "$receive")) {
                if (declaredMethod.getParameterCount() == values.length) {
                    for (int i = 0; i < declaredMethod.getParameterTypes().length; i++) {
                        Class<?> expected = Primitives.wrap(declaredMethod.getParameterTypes()[i]);
                        if (values[i] != null && expected != values[i].getClass()) {
                            break;
                        }
                    }

                    declaredMethod.invoke(this, values);
                    return;
                }
            }
        }
    }

    default void sendMessage(String method, Object... values) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] methodNameBytes = method.getBytes(StandardCharsets.UTF_8);
            ByteUtils.putInt(methodNameBytes.length, bos);
            bos.write(methodNameBytes);
            ByteUtils.putInt(values.length, bos);
            for (Object value : values) {
                byte[] data = RPCController.INSTANCE.getParser().write(value);
                ByteUtils.putInt(data.length, bos);
                bos.write(data);
            }

            RPCController.INSTANCE.publish(RPCController.INSTANCE.getRegisteredChannel(this), bos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not send message on method %s with parameters %s", method, Arrays.toString(values)), e);
        }
    }
}
