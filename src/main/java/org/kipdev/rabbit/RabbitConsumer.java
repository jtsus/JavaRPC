package org.kipdev.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.Getter;

import java.io.IOException;

public class RabbitConsumer extends DefaultConsumer {

    @Getter
    String exchange;
    RabbitReceiver receiver;
    boolean stopped = false;

    public RabbitConsumer(Channel channel, String exchange, RabbitReceiver receiver) {
        super(channel);
        this.exchange = exchange;
        this.receiver = receiver;
    }

    @Override
    public void handleCancel(String consumerTag) {
        if (!stopped) {
            RabbitMessageController.INSTANCE.registerExchange(receiver, exchange);
        }
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        //Try catch because the rabbitMQ driver closes connection on exception
        try {
            receiver.receiveMessage(body);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("RabbitMQ exchange " + exchange + " threw an error on receive.");
        }
    }

    //TODO
    public void close() {
        this.stopped = true;

        try {
            getChannel().basicCancel(this.getConsumerTag());
            getChannel().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
