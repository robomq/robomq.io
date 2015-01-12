/**
* File: consumer.java
* Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Key-based method.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*
*/
import java.io.IOException;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;


public class Consumer {
	private static final String EXCHANGE_NAME = 'exchangeName';

    public static void main(String[] argv)
                  throws java.io.IOException,
                  java.lang.InterruptedException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost('your host');
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, 'direct');
        String queueName = channel.queueDeclare().getQueue();

        String severity = 'routingKey';
        channel.queueBind(queueName, EXCHANGE_NAME, severity);

        System.out.println('Waiting for messages. To exit press CTRL+C');

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();

            System.out.println("Received '" + routingKey + "':'" + message + "'");
        }
    }
}
