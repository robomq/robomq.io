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
import com.rabbitmq.client.MessageProperties;

public class Producer {

	private Connection connection;
	private Channel channel;
	private static String server = "hostname";
	private static int port = 5672;
	private static String vhost = "yourvhost";
	private static String username = "username";
	private static String password = "password";
	private static String exchangeName = "testEx";

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

			//send message
			String message = "Hello World!";
			//for fanout type exchange, routing key is useless
			channel.basicPublish(exchangeName, "", MessageProperties.TEXT_PLAIN, message.getBytes());

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
