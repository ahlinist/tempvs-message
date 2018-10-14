package club.tempvs.message.amqp.impl;

import club.tempvs.message.util.ObjectFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.function.Consumer;

public abstract class AbstractAMQPConnector {

    private static final int DELIVERY_TIMEOUT = 30000; //30 seconds

    protected ConnectionFactory amqpConnectionFactory;
    protected  ObjectFactory objectFactory;

    public AbstractAMQPConnector(ConnectionFactory amqpConnectionFactory, ObjectFactory objectFactory) {
        this.amqpConnectionFactory = amqpConnectionFactory;
        this.objectFactory = objectFactory;
    }

    protected void execute(Consumer<String> action) {
        Connection connection = null;
        Channel channel = null;

        try {
            connection = amqpConnectionFactory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(getQueue(), false, false, false, null);

            QueueingConsumer consumer = objectFactory.getInstance(QueueingConsumer.class,channel);
            channel.basicConsume(getQueue(), true, consumer);

            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery(DELIVERY_TIMEOUT);

                if (delivery != null) {
                    String message = new String(delivery.getBody());
                    action.accept(message);
                } else {
                    throw new RuntimeException("The " + getQueue() + " queue is empty");
                }
            }
        } catch (RuntimeException e) {
            //do nothing, just skip until the next execution
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract String getQueue();
}
