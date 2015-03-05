/*
* File: Consumer.java
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

public class Consumer {

	private Connection connection;
	private Channel channel;
	private static String server = "hostname";
	private static int port = 5672;
	private static String vhost = "yourvhost";
	private static String username = "username";
	private static String password = "password";
	private String exchangeName = "testEx";
	private String requestQueue = "requestQ";
	private String requestKey = "request";

	private void consume() {
		while (true) {
			try {
				//connect
				ConnectionFactory factory = new ConnectionFactory();
				factory.setHost(server);
				factory.setPort(port);
				factory.setVirtualHost(vhost);
				factory.setUsername(username);
				factory.setPassword(password);
				connection = factory.newConnection();
				channel = connection.createChannel();
			
				//declare exchange and queue, bind them and consume messages
				channel.exchangeDeclare(exchangeName, "direct", false, true, false, null);
				channel.queueDeclare(requestQueue, false, true, true, null);
				channel.queueBind(requestQueue, exchangeName, requestKey, null);
				QueueingConsumer qc = new QueueingConsumer(channel);
				channel.basicConsume(requestQueue, false, qc);
				while (true) {
					QueueingConsumer.Delivery delivery = qc.nextDelivery();
					String message = new String(delivery.getBody());
					System.out.println(message);

					//when receives messages, reply to the reply_to header
					String replyMessage = "Reply to " + message;
					BasicProperties properties = new BasicProperties.Builder().
							contentType("text/plain").
							deliveryMode(1).
							correlationId(delivery.getProperties().getCorrelationId()).
							build();
					try {
						channel.basicPublish(exchangeName, delivery.getProperties().getReplyTo(), properties, replyMessage.getBytes());
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					} catch(Exception e) {
						channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);	
					}
				}
			} catch(Exception e) {
				//reconnect on exception
				System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", e);
				try {
					connection.close();
				} catch (Exception e1) {}
				try {
					Thread.sleep(5000); 
				} catch(Exception e2) {}
			}
		}
	}

	public static void main(String[] args) {
		Consumer c = new Consumer();
		c.consume();
	}
}
