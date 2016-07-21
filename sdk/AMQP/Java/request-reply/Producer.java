/**
* File: Producer.java
* Description: This is the AMQP producer publishes outgoing AMQP
*     communication to  clients consuming messages from a broker server.
*     Messages can be sent over AMQP exchange types including one-to-one,
*     from broadcast pattern, or selectively using specified routing key.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*/

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

public class Producer {

	private Connection connection;
	private Channel channel;
	private static String server = "hostname";
	private static int port = 5672;
	private static String vhost = "yourvhost";
	private static String username = "username";
	private static String password = "password";
	private String exchangeName = "testEx";
	private String replyQueue = "replyQ";
	private String requestKey = "request";
	private String replyKey = "reply";

	private void produce() {
		try {
			//connect
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(server);
			factory.setPort(port);
			factory.setVirtualHost(vhost);
			factory.setUsername(username);
			factory.setPassword(password);
			factory.setRequestedHeartbeat(60);
			connection = factory.newConnection();
			channel = connection.createChannel();

			//listen for reply messages
			String message = "Hello World!";
			channel.queueDeclare(replyQueue, false, true, true, null);
			channel.queueBind(replyQueue, exchangeName, replyKey, null);
			QueueingConsumer qc = new QueueingConsumer(channel);
			channel.basicConsume(replyQueue, true, qc);

			//send request message
			BasicProperties properties = new BasicProperties.Builder().
					contentType("text/plain").
					deliveryMode(1).
					replyTo(replyKey).
					build();
			channel.basicPublish(exchangeName, requestKey, properties, message.getBytes());

			//receive the reply message
			QueueingConsumer.Delivery delivery = qc.nextDelivery();
			String replyMessage = new String(delivery.getBody());
			System.out.println(replyMessage);

			//disconnect
			connection.close();
		} catch(Exception e) {
			System.out.println(e);
			System.exit(-1);			
		}	
	}

	public static void main(String[] args) {
		Producer p = new Producer();
		p.produce();
	}
}