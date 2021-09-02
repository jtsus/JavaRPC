package org.kipdev.rabbit.types;

import org.kipdev.rabbit.RabbitConsumer;
import org.kipdev.rabbit.RabbitCredentials;
import org.kipdev.rabbit.RabbitMessageController;
import org.kipdev.rabbit.RabbitExchange;
import org.kipdev.rabbit.impact.ClassImpactor;

import java.util.Arrays;

public class MockMessageController extends RabbitMessageController {

    public void initialize(String pkg) {
        initialize(null, pkg);
    }

    @Override
    public void initialize(RabbitCredentials credentials, String basePackage) {
        ClassImpactor.registerPackage(basePackage);
    }

    @Override
    public void registerExchange(RabbitExchange receiver, String exchange) {
        getConsumers().add(new RabbitConsumer(null, exchange, receiver));
    }

    @Override
    public void publish(String exchange, byte[] data) {
        System.out.printf("Sending message %s to %s\n", Arrays.toString(data), exchange);

        for (RabbitConsumer consumer : getConsumers()) {
            if (consumer.getExchange().equals(exchange)) {
                consumer.handleDelivery(exchange, null, null, data);
            }
        }
    }

    @Override
    public void close() {

    }
}
