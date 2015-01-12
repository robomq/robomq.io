/**
* File: consumer.java
* Description: AMQP protocol. This is producer code which can send message to exchange. Request-reply method.
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

import java.util.UUID;
public class Producer {
	private Connection connection;
	private Channel channel;
	private String requestQueueName = 'queueName';
	private String replyQueueName;
	private QueueingConsumer consumer;
	public Producer() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost('your host');
		connection = factory.newConnection();
		channel = connection.createChannel();
		replyQueueName = channel.queueDeclare('replyQueueName', false, false, false, null).getQueue();
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);
	}
	public String call(String message) throws Exception {
		String response = null;
		String corrId = UUID.randomUUID().toString();
		BasicProperties props = new BasicProperties
		.Builder()
		.correlationId(corrId)
		.replyTo(replyQueueName)
		.build();
		channel.basicPublish('exchangeName', requestQueueName, props, message.getBytes());
		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery.getProperties().getCorrelationId().equals(corrId)) {
				response = new String(delivery.getBody(),'UTF-8');
				break;
			}
		}
		return response;
	}
	public void close() throws Exception {
		connection.close();
	}
	public static void main(String[] argv) {
		Producer fibonacciRpc = null;
		String response = null;
		try {
			fibonacciRpc = new Producer();
			System.out.println('hello world');
			response = fibonacciRpc.call('hello world');
			System.out.println(response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (fibonacciRpc!= null) {
				try {
					fibonacciRpc.close();
				}
					catch (Exception ignore) {}
				}
		}
	}
}