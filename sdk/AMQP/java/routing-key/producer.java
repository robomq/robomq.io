/**
* File: producer.java
* Description: AMQP protocol. This is producer code which can send message to exchange. Routing-key based method.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*
*/
import java.io.IOException;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;


public class Producer {
	private static final String EXCHANGE_NAME = 'exchangeName';

    public static void main(String[] argv)
                  throws java.io.IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost('your host');
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, 'direct');

        String severity = 'routingKey';
        String message = 'hello world';

        channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes());
        System.out.println("Sent '" + severity + "':'" + message + "'");

        channel.close();
        connection.close();
    }

}
