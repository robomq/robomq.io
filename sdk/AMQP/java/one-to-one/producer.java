/**
* File: producer.java
* Description: AMQP protocol. This is producer code which can send message to exchange. One-to-one method.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*
*/
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class Producer {
	
    private final static String QUEUE_NAME = 'queueName';
    	
	public static void main(String[] argv)
      		throws java.io.IOException {
		ConnectionFactory factory = new ConnectionFactory();
    	factory.setHost('your host');
    	Connection connection = factory.newConnection();
    	Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    	String message = 'Hello World!';
    	channel.basicPublish('', QUEUE_NAME, null, message.getBytes());
    	System.out.println("Sent '" + message + "'");
		channel.close();
		connection.close();
  	}
}

