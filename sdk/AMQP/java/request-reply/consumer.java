/**
* File: consumer.java
* Description: AMQP protocol. This is consumer code which can get message from exchange and consume them. Request-reply method.
*
* Author: Stanley
* robomq.io (http://www.robomq.io)
*
*/
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

public class Consumer {
	private static final String RPC_QUEUE_NAME = 'queueName';
	private static final String EXCHANGE_NAME = 'exchangeName';

	public static void main(String[] argv) {
		Connection connection = null;
		Channel channel = null;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost('your host');
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
			channel.exchangeDeclare(EXCHANGE_NAME, 'direct');
			String severity = 'routingKey';
			channel.queueBind(RPC_QUEUE_NAME, EXCHANGE_NAME, severity);
			
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
			System.out.println('Awaiting requests');
			while (true) {
				String response = null;
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties
				.Builder()
				.correlationId(props.getCorrelationId())
				.build();
				try {
					String message = new String(delivery.getBody(),'UTF-8');
					int n = Integer.parseInt(message);
					System.out.println('received ' + message);
					response = 'reply';
				}
				catch (Exception e){
					System.out.println(' [.] ' + e.toString());
					response = 'reply';
				}
				finally {
					channel.basicPublish( EXCHANGE_NAME, 'reply', replyProps, response.getBytes('UTF-8'));
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				}
			catch (Exception ignore) {}
			}
		}
	}
}