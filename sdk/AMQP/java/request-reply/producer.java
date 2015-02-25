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
import java.util.UUID;

public class Producer {

	private Connection connection;
	private Channel channel;
	private static String server = "hostname";
	private static int port = 5672;
	private static String vhost = "yourvhost";
	private static String username = "username";
	private static String password = "password";
	private String exchangeName = "testEx";
	private String repQueueName = "replyQ";
	private String reqRoutingKey = "request";
	private String repRoutingKey = "reply";

	private void produce() {
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

			//listen for reply messages
			String message = "Hello World!";
			channel.exchangeDeclare(exchangeName, "direct", false, true, false, null);
			channel.queueDeclare(repQueueName, false, true, true, null);
			channel.queueBind(repQueueName, exchangeName, repRoutingKey, null);
			QueueingConsumer qc = new QueueingConsumer(channel);
			channel.basicConsume(repQueueName, true, qc);

			//send message
			BasicProperties properties = new BasicProperties.Builder().
					contentType("text/plain").
					deliveryMode(1).
					correlationId(UUID.randomUUID().toString()).
					replyTo(repRoutingKey).
					build();
			channel.basicPublish(exchangeName, reqRoutingKey, properties, message.getBytes());

			//receive the reply message
			QueueingConsumer.Delivery delivery = qc.nextDelivery();
			String replyMessage = new String(delivery.getBody());
			System.out.println(replyMessage);

			//disconnect
			channel.close();
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
