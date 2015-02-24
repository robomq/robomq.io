/**
* File: producer.java
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

public class producer {

	private Connection connection;
	private Channel channel;
	private static String server = "localhost";
	private static int port = 5672;
	private static String vhost = "/";
	private static String username = "guest";
	private static String password = "guest";
	private static String routingKey = "testQ";

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
			connection = factory.newConnection();
			channel = connection.createChannel();
		} catch(Exception e) {
			System.out.println("Error: Failed to initialize connection");
			System.exit(-1);
		}		
	}

	/**
	 * This method publishes a hello-world message to the specified routing key.
	 * @ exception on publish error.
	 */
	private void publish() {
		try {
			String message = "Hello World!";
			//assigning blank string to exchange is to use the default exchange, where queue name is the routing key
			channel.basicPublish("", routingKey, MessageProperties.TEXT_PLAIN, message.getBytes());
		} catch(Exception e) {
			System.out.println("Error: Failed to publish message");
			System.exit(-1);			
		}	
	}

	/**
	 * This method disconnect client from the broker.
	 * @ exception on disconnection error.
	 */
	private void disconnect() {
		try {
			channel.close();
			connection.close();
		} catch(Exception e) {
			System.out.println("Error: Failed to disconnect");
			System.exit(-1);			
		}
	}

	/**
	 * This is the main method which creates and runs producer instance.
	*/
	public static void main(String[] args) {
		producer p = new producer();
		p.connect();
		p.publish();
		p.disconnect();
	}
}
