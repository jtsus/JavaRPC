package org.kipdev.rpc;

import lombok.Getter;
import lombok.Setter;
import org.kipdev.rpc.impact.ClassImpactor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RPCController {
    public static RPCController INSTANCE;

    @Getter
    private final Map<String, Exchange> exchanges = new ConcurrentHashMap<>();

    @Setter
    @Getter
    private DataSerializer parser = GsonDataSerializer.INSTANCE;

    public void registerPackage(String pkg) {
        if (pkg != null) {
            ClassImpactor.registerPackage(pkg);
        }
    }

    public final void registerExchange(String channelName, Exchange receiver) {
        dismissExchange(channelName);
        exchanges.put(channelName, receiver);
        initializeExchange(channelName, receiver);
    }

    public void initializeExchange(String channelName, Exchange exchange) {}

    public void dismissExchange(String channelName) {}

    public abstract void publish(String exchange, byte[] data);

    public void handleMessage(String channel, byte[] data) {
        Exchange exchange = exchanges.get(channel);
        try {
            if (exchange != null) {
                exchange.receiveMessage(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRegisteredChannel(Exchange exchange) {
        for (String key : exchanges.keySet()) {
            if (exchanges.get(key) == exchange) {
                return key;
            }
        }

        return null;
    }
}
