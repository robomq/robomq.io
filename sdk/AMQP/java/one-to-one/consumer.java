/**
* File: consumer.java
* Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Routing-key based method.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*
*/
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class Consumer {

	private final static String QUEUE_NAME = 'queueName';

	public static void main(String[] argv)
	    throws java.io.IOException,
	    	java.lang.InterruptedException {

	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost('your host');
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    System.out.println('Waiting for messages. To exit press CTRL+C');
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(QUEUE_NAME, true, consumer);

	    while (true) {
	      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	      String message = new String(delivery.getBody());
	      System.out.println("Received '" + message + "'");
	    }
    }
}

