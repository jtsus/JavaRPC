package org.kipdev.rpc;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.pixelverse.gson.internal.Primitives;

import java.lang.reflect.Method;

public interface Exchange {

    default void receiveMessage(byte[] message) throws Exception {
        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String methodName = input.readUTF();
        int paramCount = input.readInt();
        Object[] paramValues = new Object[paramCount];
        for (int i = 0; i < paramCount; i++) {
            int dataSize = input.readInt();
            byte[] paramData = new byte[dataSize];
            input.readFully(paramData);
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
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(method);
        output.writeInt(values.length);
        for (Object value : values) {
            byte[] data = RPCController.INSTANCE.getParser().write(value);
            output.writeInt(data.length);
            output.write(data);
        }

        RPCController.INSTANCE.publish(RPCController.INSTANCE.getRegisteredChannel(this), output.toByteArray());
    }
}
