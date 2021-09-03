package org.kipdev.rpc.implementations.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.Getter;

import java.util.function.BiConsumer;

@Getter
public class RabbitConsumer extends DefaultConsumer {

    private final String exchange;
    private final BiConsumer<String, byte[]> dataProcessor;

    public RabbitConsumer(Channel channel, String exchange, BiConsumer<String, byte[]> dataProcessor) {
        super(channel);

        this.exchange = exchange;
        this.dataProcessor = dataProcessor;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        //Try catch because the rabbitMQ driver closes connection on exception
        try {
            dataProcessor.accept(exchange, body);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("RabbitMQ exchange " + consumerTag + " threw an error on receive.");
        }
    }

}
