package org.kipdev.rpc;

import net.pixelverse.gson.internal.Primitives;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

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

                    declaredMethod.setAccessible(true);
                    declaredMethod.invoke(this, values);
                    return;
                }
            }
        }
    }

    default void sendMessage(String method, Object[] values) {
        ExchangeHandler.sendMessage(this, method, values);
    }
}
