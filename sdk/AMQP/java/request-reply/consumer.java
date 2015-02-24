/*
* File: consumer.java
* Description: This is the AMQP consumer handles incoming
*     communication from clients publishing messages to a broker server.
*     Messages can be received over AMQP exchange types including one-to-one,
*     from broadcast pattern, or selectively using specified binding key.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*/

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

public class consumer {

	private Connection connection;
	private Channel channel;
	private static String server = "localhost";
	private static int port = 5672;
	private static String vhost = "/";
	private static String username = "guest";
	private static String password = "guest";
	private String exchangeName = "testEx";
	private String reqQueueName = "requestQ";
	private String reqRoutingKey = "request";

	/**
	 * This method connects client to the broker.
	 * @ exception on connection error.
	 */
	private void connect() {
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(server);
			factory.setPort(port);
			factory.setVirtualHost(vhost);
			factory.setUsername(username);
			factory.setPassword(password);
			factory.setAutomaticRecoveryEnabled(true); //connection will recover automatically
			connection = factory.newConnection();
			channel = connection.createChannel();
		} catch(Exception e) {
			System.out.println("Error: Failed to initialize connection");
			System.exit(-1);
		}		
	}

	/**
	 * This method declares exchange and queue, binds them and consumes messages.
	 * @ exception on consuming error.
	 */
	private void consume() {
		try {
			channel.exchangeDeclare(exchangeName, "direct", false, true, false, null);
			channel.queueDeclare(reqQueueName, false, true, true, null);
			channel.queueBind(reqQueueName, exchangeName, reqRoutingKey, null);
			QueueingConsumer qc = new QueueingConsumer(channel);
			channel.basicConsume(reqQueueName, false, qc);
			while (true) {
				QueueingConsumer.Delivery delivery = qc.nextDelivery();
				String message = new String(delivery.getBody());
				System.out.println(message);
				String replyMessage = "Reply to " + message;
				BasicProperties properties = new BasicProperties.Builder().
						contentType("text/plain").
						correlationId(delivery.getProperties().getCorrelationId()).
						build();
				channel.basicPublish(exchangeName, delivery.getProperties().getReplyTo(), properties, replyMessage.getBytes());
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			}
		} catch(Exception e) {
			System.out.println("Error: Failed to consume messages");
			System.exit(-1);		
		}	
	}

	/**
	 * This is the main method which creates and runs consumer instance.
	*/
	public static void main(String[] args) {
		consumer c = new consumer();
		c.connect();
		c.consume();
	}
}
