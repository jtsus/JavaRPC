package org.kipdev.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import lombok.Setter;
import org.kipdev.rabbit.impact.ClassImpactor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class RabbitMessageController {

    public static RabbitMessageController INSTANCE = new RabbitMessageController();

    Connection connection = null;
    List<RabbitConsumer> consumers = new ArrayList<>();

    private RabbitCredentials credentials;
    @Setter
    private DataSerializer parser = GsonDataSerializer.INSTANCE;

    /**
     * Must be called in order to interface with RabbitMQ
     * @param credentials Credentials of Rabbit
     */
    public void initialize(RabbitCredentials credentials, String basePackage) {
        this.credentials = credentials;

        ClassImpactor.registerPackage(basePackage);

        try {
            connection = getNewConnectionFactory().newConnection();
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to RabbitMQ", e);
        }
    }

    private ConnectionFactory getNewConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(credentials.getHost());
        factory.setUsername(credentials.getUsername());
        factory.setPassword(credentials.getPassword());
        factory.setVirtualHost(credentials.getVirtualHost());
        return factory;
    }

    public void registerExchange(RabbitExchange receiver, String exchange) {
        try {
            Channel channel = getChannel();
            channel.exchangeDeclare(exchange, "fanout", false, false, false, null);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, exchange, "");


            RabbitConsumer consumer = new RabbitConsumer(channel, exchange, receiver);
            channel.basicConsume(queueName, true, consumer);
            consumers.add(consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(String exchange, byte[] data) {
        try {
            Channel channel = getChannel();
            channel.basicPublish(exchange, "", new AMQP.BasicProperties.Builder().deliveryMode(1).build(), data);
            channel.close();
        } catch (Exception e) {
            throw new RuntimeException("Could not send Rabbit message", e);
        }
    }

    private Channel getChannel() {
        if (!connection.isOpen()) {
            reconnect();
        }
        try {
            return connection.createChannel();
        } catch (IOException e) {
            throw new RuntimeException("Error creating RabbitMQ channel....", e);
        }
    }

    public String getExchange(RabbitExchange receiver) {
        for (RabbitConsumer consumer : consumers) {
            if (consumer.receiver == receiver) {
                return consumer.exchange;
            }
        }
        return null;
    }

    public void reconnect() {
        try {
            System.err.println("RabbitMQ wasn't connected... Opening new connection.");
            connection = getNewConnectionFactory().newConnection();

            ArrayList<RabbitConsumer> newConsumers = new ArrayList<>();
            for (RabbitConsumer consumer : consumers) {
                Channel channel = getChannel();
                String exchange = consumer.exchange;

                channel.exchangeDeclare(exchange, "fanout", false, false, false, null);
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, exchange, "");


                RabbitConsumer newConsumer = new RabbitConsumer(channel, exchange, consumer.receiver);
                channel.basicConsume(queueName, true, newConsumer);
                newConsumers.add(newConsumer);
            }

            consumers.clear();
            consumers.addAll(newConsumers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            for (RabbitConsumer consumer : this.consumers) {
                consumer.close();
            }
            connection.close();
        } catch (IOException ignored) {

        }
    }
}
