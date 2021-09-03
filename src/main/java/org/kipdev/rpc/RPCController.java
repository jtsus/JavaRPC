package org.kipdev.rpc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.Setter;
import org.kipdev.rpc.impact.ClassImpactor;

public abstract class RPCController {
    public static RPCController INSTANCE;

    @Getter
    private final Multimap<String, Exchange> exchanges = Multimaps.synchronizedMultimap(HashMultimap.create());

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
        exchanges.put(channelName, receiver);
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
