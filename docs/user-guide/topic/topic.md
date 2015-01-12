# Routing - Filter Based

For filter based routing, a producer declares the topic exchange when publishing a message.  Messages sent with a particular routing key will be delivered to all the queues that are bound with a matching binding key.
Filter based routing provides a method to use filter policies on routing key for choosing the recipients of messages. <br>
<b> * (star) can substitute for exactly one word.</b>
<br>
example: 'topic.*' can be : topic1, topic2, topic3 etc.
<br>
<b># (hash) can substitute for zero or more words.</b>
<br>
example: "#.topic" can be: topic, Ftopic, Secondtopic, 123topic etc.

![Diagram of Routing - Filter based messaging](../../images/topic.png)

----------

## Python

###Producer: 
First, producer should initialize a new connection to [robomq.io](http://www.robomq.io) server and create a new channel.

After that producer should initialize a topic-type exchange for delivering messages. 


	channel.exchange_declare(exchange='exchangeName',
                         type='topic')

Now producer is ready to send messages to exchange.


	channel.basic_publish(exchange='exchangeName',
                      routing_key=routing_key,
                      body=message) 



###Consumer:
First, Consumer should initialize the connection and start a new channel. 

Then consumer should initialize a topic-type exchange as producer did. 

	channel.exchange_declare(exchange='exchangeName', type='topic')


Initialize a queue for customer listenting to: 

	channel.queue_declare(queue='queueName', exclusive = True)

After that, Consumer should define a routing policy for binding queues to exchanges.


	String bindingKey = "routingKey";
	# you can use any policy for binding key 
 

Binding the queue and exchange
	
	channel.queue_bind(exchange='exchangeName',
                    queue=queue_name,
                    routing_key=routing_key)
	

After that, starting consuming the messages and followed by your customize code.
	
	channel.basic_consume(callback,
                      queue=queue_name,
                      no_ack=True)

	channel.start_consuming()
	
###Putting it all together

**producer.py**
	
	import pika
	import sys

	connection = pika.BlockingConnection(pika.ConnectionParameters( host='your host'))
	channel = connection.channel()

	channel.exchange_declare(exchange='exchangeName', type='topic')

	routing_key ='routingKey'
	message = 'hello world'
	channel.basic_publish(exchange='exchangeName', routing_key=routing_key, body=message)
	print 'Sent %r:%r' % (routing_key, message)
	connection.close()

**consumer.py**

	import pika

	connection = pika.BlockingConnection(pika.ConnectionParameters(host='your host'))
	channel = connection.channel()

	channel.exchange_declare(exchange='exchangeName',
                         type='topic')

	queue_name = 'queueName'
	channel.queue_declare(queue=queue_name,exclusive=True)
	routing_key= 'routingKey'

	channel.queue_bind(exchange='exchangeName',
                    queue=queue_name,
                    routing_key=routing_key)

	print 'Waiting for logs. To exit press CTRL+C'

	def callback(ch, method, properties, body):
		print '%r:%r' % (method.routing_key, body,)

	channel.basic_consume(callback,
                      queue=queue_name,
                      no_ack=True)

	channel.start_consuming()
	
## Java
###Producer: 
First, producer should initialize a new connection to [robomq.io](http://www.robomq.io) server and create a new channel.

After that producer should initialize a topic-type exchange for delivering messages. 

	channel.exchangeDeclare(EXCHANGE_NAME, 'topic');
	
Now producer is ready to send messages to exchange.

	channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
	
###Consumer:
First, Consumer should initialize the connection and start a new channel. 

Then consumer should initialize an topic-type exchange as producer did. 

	channel.exchangeDeclare(EXCHANGE_NAME, 'topic');
	
Initialize a queue for customer listenting to: 

	channel.queueDeclare("queueName", false, false, false, null);

After that, Consumer should define a routing policy for binding queues to exchanges.Binding the queue and exchange.

	String bindingKey = 'routingKey';
	channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
	        
After that, starting consuming the messages. 

###Putting it all together

**producer.java**

	import java.io.IOException;
	import com.rabbitmq.client.ConnectionFactory;
	import com.rabbitmq.client.Connection;
	import com.rabbitmq.client.Channel;

	public class Producer {
		private static final String EXCHANGE_NAME = 'exchangeName';
		public static void main(String[] argv)
                  throws Exception {
                  ConnectionFactory factory = new ConnectionFactory();
                  factory.setHost('your host');
                  Connection connection = factory.newConnection();
                  Channel channel = connection.createChannel();
                  channel.exchangeDeclare(EXCHANGE_NAME, 'topic');
                  String routingKey = 'routingKey';
                  String message ='hello world';
                  channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
                  System.out.println('Sent '' + routingKey + '':'' + message + "'");
                  connection.close();
		}
	}

**consumer.java**

	import java.io.IOException;
	import com.rabbitmq.client.ConnectionFactory;
	import com.rabbitmq.client.Connection;
	import com.rabbitmq.client.Channel;
	import com.rabbitmq.client.QueueingConsumer;

	public class Consumer {
	 	private static final String EXCHANGE_NAME = 'exchangeName';
		public static void main(String[] argv)
			throws Exception {

	        ConnectionFactory factory = new ConnectionFactory();
	        factory.setHost('your host');
	        Connection connection = factory.newConnection();
	        Channel channel = connection.createChannel();

	        channel.exchangeDeclare(EXCHANGE_NAME, 'topic');
	        String queueName = channel.queueDeclare().getQueue();

	        String bindingKey = 'routingKey';
	        channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
	        

	        System.out.println('Waiting for messages. To exit press CTRL+C');

	        QueueingConsumer consumer = new QueueingConsumer(channel);
	        channel.basicConsume(queueName, true, consumer);

	        while (true) {
	            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	            String message = new String(delivery.getBody());
	            String routingKey = delivery.getEnvelope().getRoutingKey();

	            System.out.println("Received '" + routingKey + '":"' + message + "'");
	        }
	    }
	}

## Node.js
###Producer: 
First, producer should initialize a new connection to [robomq.io](http://www.robomq.io) server and create a new channel.

After that producer should initialize a topic-type exchange for delivering messages. 

Now producer is ready to send messages to exchange.
	
	connection.on('ready',function(){
	connection.exchange('test-exchange', options={type:"topic",
		autoDelete:false}, function(exchange){
		
			console.log("start send message");
			//setting routing key as "test1"
			exchange.publish('test1',"hello");

		
	})
	});




###Consumer:
First, Consumer should initialize the connection and start a new channel. 

Then consumer should initialize an topic-type exchange as producer did. 

<pre>
 channel.exchangeDeclare("exchangeName", "topic");
</pre>

Initialize a queue for customer listening to. After that, Consumer should define a routing policy for binding queues to exchanges.
string bindingKey = "#.key"
\# you can use any policy for binding key 

Binding the queue and exchange with the filter policy.



	var queue = connection.queue("my-queue3", options={},function(queue){
					console.log("Declare one queue, name is " + queue.name);
					queue.bind("test-exchange", '*.test3');
					queue.subscribe(function (msg){
							console.log("the message is "+msg.data);
						});
				});


 After that, starting consuming the messages.
 
###Putting it all together

**producer.js**

	var amqp = require('amqp');
	var connection = amqp.createConnection({ host: 'your host', port: 'port' });

	connection.on('ready',function(){
		connection.exchange('exchangeName', options={type:'topic', autoDelete:false}, function(exchange){
			console.log('start send message');
			exchange.publish('1routingKey','hello world');
		});
	});

**consumer.js**

	var amqp = require('amqp');
	var connection = amqp.createConnection({ host: 'your host', port: 'port' });

	connection.on('ready', function(){
		connection.exchange('exchangeName', options={type:'topic', autoDelete:false}, function(exchange){
			var queue = connection.queue('queueName', options={},function(queue){
				console.log('Declare one queue, name is ' + queue.name);
				queue.bind('exchangeName', '*.routingKey');
				queue.subscribe(function (msg){
					console.log('the message is '+msg.data);
				});
			});
		});
	});
	
## C
### Prerequisites

**C client AMQP library**

RoboMQ is built on AMQP, an open, general-purpose protocol for messaging. There are a number of clients for AMQP in many different languages.  However, we'll choose a simple C-language AMQP client library written for use with v2.0+ of the RabbitMQ broker.

[https://github.com/alanxz/rabbitmq-c/tree/master/librabbitmq](https://github.com/alanxz/rabbitmq-c/tree/master/librabbitmq)

Download the client library package, and copy it into your working directory:

	cp librabbitmq ./


Note that these examples provide a simple client implementation to get started but does not go into detailed description of all flags passed into the AMQP methods. 
A complete reference to RabbitMQ's implementaton of version 0-9-1 of the AMQP specification can be found in this guide.
[https://www.rabbitmq.com/amqp-0-9-1-reference.html](https://www.rabbitmq.com/amqp-0topic-exchange-9-1-reference.html)


### Producer
For filter based routing, the producer should publish messages to the **topic** type exchange. All messages sent with the routing key, **"mytopic.new"**, will be delivered to all the queues that are bound with a matching binding key. 
Note that the routing key must be a list of words, delimited by dots. The words can be anything, but usually they specify some features connected to the message. A few valid routing key examples: **"log.warning"**, **"log.error"**. 

	amqp_basic_properties_t props;
	props._flags = AMQP_BASIC_CONTENT_TYPE_FLAG | AMQP_BASIC_DELIVERY_MODE_FLAG;
	props.content_type = amqp_cstring_bytes("text/plain");
	props.delivery_mode = 1; /* non-persistent delivery mode */
	amqp_boolean_t mandatory = 0;
	amqp_boolean_t immediate = 0;
	char exchange_name[] = "topic-exchange";
	char routing_key[] = "mytopic.new";
	int result;
	
	// Sending message
	result = amqp_basic_publish(conn,
			channel,
			amqp_cstring_bytes(exchange_name),
			amqp_cstring_bytes(routing_key),
			mandatory,
			immediate,
			&props,
			amqp_cstring_bytes("Hello"));


### Consumer
Then the consumer should create an exchange and subscribe to a queue.  This exchange will be defined similarly to the one-to-one example, however, the **topic** exchange type is specified below as **exchange_type**.

	amqp_bytes_t queue;
	amqp_channel_t channel = 1;
	amqp_boolean_t passive = 0;
	amqp_boolean_t durable = 0;
	amqp_boolean_t exclusive = 0;
	amqp_boolean_t auto_delete = 1;
	char exchange_name[] = "topic-exchange";
	char exchange_type[] = "topic";
	char queue_name[] = "hello-queue";
	char binding_key[] = "mytopic.new";
	
	// Declaring exchange
	amqp_exchange_declare(conn, channel, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(exchange_type),
			passive, durable, amqp_empty_table);
	
	// Declaring queue
	amqp_queue_declare_ok_t *r = amqp_queue_declare(conn, channel, amqp_cstring_bytes(queue_name),
			passive, durable, exclusive, auto_delete, amqp_empty_table);

	queue = amqp_bytes_malloc_dup(r->queue);
	
	// Binding to queue
	amqp_queue_bind(conn, channel, queue, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(binding_key),
			amqp_empty_table);


At this point, consumer should start consuming messages.

### How to build/run client
Now we have two c files, one is producer.c, another is consumer.c. 
(1) Use the Makefile in sdk repository to compile under a Linux terminal. 
(2) cd to the directory which containing client source code 
(3) Run make all


### Putting it all together
The full code below includes some basic AMQP error handling for consumer that is useful when declaring exchanges and queues.

**producer.c**

	#include <stdlib.h>
	#include <stdio.h>
	#include <string.h>
	
	#include <amqp_tcp_socket.h>
	#include <amqp.h>
	#include <amqp_framing.h>
	
	amqp_connection_state_t mqconnect() {
	
		amqp_connection_state_t conn = amqp_new_connection();
		amqp_socket_t *socket = NULL;
		char hostname[] = "hostname"; // robomq.io hostname
		int port = 5672; //default
		char user[] = "username"; // robomq.io username
		char password[] = "password"; // robomq.io password
	    char vhost[] = "vhost"; // robomq.io account vhost
		amqp_channel_t channel = 1;
		int channel_max = 0;
		int frame_max = 131072;
		int heartbeat = 0;
		int status = 0;
	
		// Opening socket
		socket = amqp_tcp_socket_new(conn);
	
		status = amqp_socket_open(socket, hostname, port);
		if (status) {
			printf("Error opening TCP socket, status = %d, exiting.", status);
		}
	
		amqp_login(conn, vhost, channel_max, frame_max, heartbeat, AMQP_SASL_METHOD_PLAIN, user, password);
		amqp_channel_open(conn, channel);
	
		return conn;
	}
	
	int main(int argc, char const *const *argv)
	{
		amqp_connection_state_t conn;
		amqp_channel_t channel = 1;
		amqp_basic_properties_t props;
		props._flags = AMQP_BASIC_CONTENT_TYPE_FLAG | AMQP_BASIC_DELIVERY_MODE_FLAG;
		props.content_type = amqp_cstring_bytes("text/plain");
		props.delivery_mode = 1; /* non-persistent delivery mode */
		amqp_boolean_t mandatory = 0;
		amqp_boolean_t immediate = 0;
		char exchange_name[] = "topic-exchange";
		char routing_key[] = "mytopic.new";
		int result;
	
		conn = mqconnect();
	
		// Sending message
		result = amqp_basic_publish(conn,
				channel,
				amqp_cstring_bytes(exchange_name),
				amqp_cstring_bytes(routing_key),
				mandatory,
				immediate,
				&props,
				amqp_cstring_bytes("Hello"));
	
		// Closing connection
		amqp_channel_close(conn, channel, AMQP_REPLY_SUCCESS);
		amqp_connection_close(conn, AMQP_REPLY_SUCCESS);
		amqp_destroy_connection(conn);
	
		return 0;
	}


**consumer.c**

	#include <stdlib.h>
	#include <stdio.h>
	#include <string.h>
	
	#include <amqp_tcp_socket.h>
	#include <amqp.h>
	#include <amqp_framing.h>
	
	amqp_connection_state_t mqconnect() {
	
		amqp_connection_state_t conn = amqp_new_connection();
		amqp_socket_t *socket = NULL;
		char hostname[] = "hostname"; // robomq.io hostname
		int port = 5672; //default
		char user[] = "username"; // robomq.io username
		char password[] = "password"; // robomq.io password
		char vhost[] = "vhost"; // robomq.io account vhost
		amqp_channel_t channel = 1;
		int channel_max = 0;
		int frame_max = 131072;
		int heartbeat = 0;
		int status = 0;
	
		// Opening socket
		socket = amqp_tcp_socket_new(conn);
	
		status = amqp_socket_open(socket, hostname, port);
		if (status) {
			printf("Error opening TCP socket, status = %d, exiting.", status);
		}
	
		amqp_login(conn, vhost, channel_max, frame_max, heartbeat, AMQP_SASL_METHOD_PLAIN, user, password);
		amqp_channel_open(conn, channel);
	
		return conn;
	}
	
	amqp_bytes_t mqdeclare(amqp_connection_state_t conn) {
		amqp_bytes_t queue;
		amqp_channel_t channel = 1;
		amqp_boolean_t passive = 0;
		amqp_boolean_t durable = 0;
		amqp_boolean_t exclusive = 0;
		amqp_boolean_t auto_delete = 1;
		char exchange_name[] = "hello-exchange";
		char exchange_type[] = "topic";
		char queue_name[] = "hello-queue";
		char binding_key[] = "mytopic.new";
		amqp_rpc_reply_t reply;
	
		// Declaring exchange
		amqp_exchange_declare(conn, channel, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(exchange_type),
				passive, durable, amqp_empty_table);
	
		reply = amqp_get_rpc_reply(conn);
		if(reply.reply_type != AMQP_RESPONSE_NORMAL) {
			amqp_connection_close_t *m = (amqp_connection_close_t *) reply.reply.decoded;
			fprintf(stderr, "%s: server connection error %d, message: %.*s\n",
					"Error declaring exchange",
					m->reply_code,
					(int) m->reply_text.len, (char *) m->reply_text.bytes);
			exit(1);
		}
	
		// Declaring queue
		amqp_queue_declare_ok_t *r = amqp_queue_declare(conn, channel, amqp_cstring_bytes(queue_name),
				passive, durable, exclusive, auto_delete, amqp_empty_table);
	
		reply = amqp_get_rpc_reply(conn);
		if(reply.reply_type != AMQP_RESPONSE_NORMAL) {
			amqp_connection_close_t *m = (amqp_connection_close_t *) reply.reply.decoded;
					fprintf(stderr, "%s: server connection error %d, message: %.*s\n",
							"Error declaring queue",
							m->reply_code,
							(int) m->reply_text.len, (char *) m->reply_text.bytes);
			exit(1);
		}
		queue = amqp_bytes_malloc_dup(r->queue);
	
		// Binding to queue
		amqp_queue_bind(conn, channel, queue, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(binding_key),
				amqp_empty_table);
	
		return queue;
	}
	
	int main(int argc, char const *const *argv)
	{
		amqp_connection_state_t conn;
		amqp_bytes_t queue;
		amqp_channel_t channel = 1;
		amqp_boolean_t no_local = 0;
		amqp_boolean_t no_ack = 1;
		amqp_boolean_t exclusive = 0;
		amqp_frame_t frame;
	
		conn = mqconnect();
		queue = mqdeclare(conn);
	
		// Consuming the message
		amqp_basic_consume(conn, channel, queue, amqp_empty_bytes, no_local, no_ack, exclusive, amqp_empty_table);
	
		while (1) {
			amqp_rpc_reply_t result;
			amqp_envelope_t envelope;
	
			amqp_maybe_release_buffers(conn);
			result = amqp_consume_message(conn, &envelope, NULL, 0);
	
			if (AMQP_RESPONSE_NORMAL == result.reply_type) {
	
				printf("Received message size: %d\nbody: %s\n", envelope.message.body.len, envelope.message.body.bytes);
	
				amqp_destroy_envelope(&envelope);
			}
		}
	
		// Closing connection
		amqp_channel_close(conn, channel, AMQP_REPLY_SUCCESS);
		amqp_connection_close(conn, AMQP_REPLY_SUCCESS);
		amqp_destroy_connection(conn);
	
		return 0;
	}










