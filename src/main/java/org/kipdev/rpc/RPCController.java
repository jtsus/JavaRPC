package org.kipdev.rpc;

import lombok.Getter;
import lombok.Setter;
import org.kipdev.rpc.impact.ClassImpactor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class RPCController {
    public static RPCController INSTANCE;

    @Getter
    private final Map<String, List<Exchange>> exchanges = new ConcurrentHashMap<>();

    @Setter
    @Getter
    private DataSerializer parser = GsonDataSerializer.INSTANCE;

    public void registerPackage(String pkg) {
        ClassImpactor.registerPackage(pkg);
    }

    public final void registerExchange(String channelName, Exchange receiver) {
        if (exchanges.containsKey(channelName)) {
            return;
        }
        exchanges.computeIfAbsent(channelName, n -> new CopyOnWriteArrayList<>()).add(receiver);
        initializeExchange(channelName, receiver);
    }

    public void initializeExchange(String channelName, Exchange exchange) {}

    public abstract void publish(String exchange, byte[] data);

    public void handleMessage(String channel, byte[] data) {
        for (Exchange exchange : exchanges.get(channel)) {
            try {
                exchange.receiveMessage(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getRegisteredChannel(Exchange exchange) {
        for (String key : exchanges.keySet()) {
            if (exchanges.get(key).contains(exchange)) {
                return key;
            }
        }

        return null;
    }
}
