# One to One (Direct)

For one to one messaging, a producer sends messages to specified queue.  A consumer receives messages from that queue.  To ensure message is not lost, message acknowledgments can be sent back to producer to confirm a particular message has been received.

![Diagram of One to One messaging](images/one-one.png)

----------

## Python
###Prerequisites
**python client AMQP library**
Using AMQP protocol for python programming language requires downloading and installing the following library.

Installing pika 

	sudo pip install pika</pre>

- Ubuntu

		sudo apt-get install python-pip git-core</pre>

- On Debian

		sudo apt-get install python-setuptools git-core
		sudo easy_install pip

- on Windows

		easy_install pip
		pip install pika



###Producer
For one-to-one message communication, the producer should first initialize a connection to the [robomq.io](http://www.robomq.io) broker. 

	connection = pika.BlockingConnection(pika.connectionParameters(host='your host'))
	channel = 	connection.channel()

Then producer should create a queue and publish messages to this queue. This queue will work as a mailbox where all messages published to it will be stored until they are consumed.

	channel.queue_declare(queue='queueName')

A queue declared without a specific routing key use their queue name as the default routing key.  

Then producer should publish messages to the default exchange attached with routing key. That routing key is the queue name. Based on that routing key, messages will be distributed through the default exchange to the right queue. 

	import pika 

	channel.basic_publish(exchange='',
					routing_key='queueName',
					body='message')

After all messages are published, producer should terminate this connection.

	channel.close( )


###Consumer
First, consumer should initialize connection to the [robomq.io](http://www.robomq.io) server. 

	connection = pika.BlockingConnection(pika.connectionParameters(host='your host'))
	channel = connection.channel()


Then consumer should subscribe to a specific queue to listen and consume messages from.

	channel.queue_declare(queue='queueName')


Then consumer should define the callback method.  When messages are received, this callback is invoked performing any desired processing on message contents.

	def callback(ch, method, properties, body):
		print 'Received %r' % (body)


At this point, consumer should start consuming messages.

	channel.basic_consume(callback, queue='queueName', no_ack=True)

The **no_ack** parameter indicates whether consumer will automatically send acknowledgment back to broker. For this example, producer does not explicitly acknowledge received messages.  Therefore, we set **no_ack** attribute value as true.

Then, consumer should receive messages and implement any desired processing on message contents.

	channel.start_consuming()

###Putting it all together
 **producer.py**
 
	import pika

	connection = pika.BlockingConnection(pika.ConnectionParameters(host='your host'))
	channel = connection.channel()

	channel.queue_declare(queue='queueName')

	channel.basic_publish(exchange='', routing_key='queueName', body='Hello World!')
	print 'Sent 'Hello World!''
	connection.close()

**consumer.py**

	import pika

	connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='your host'))
	channel = connection.channel()

	channel.queue_declare(queue='queueName')
	print 'Waiting for messages. To exit press CTRL+C'

	def callback(ch, method, properties, body):
		print 'Received %r' % (body)

	channel.basic_consume(callback, queue='queueName', no_ack=True)
	channel.start_consuming() 

## Java
###Prerequisites
**java client AMQP library**
Using AMQP protocol for java programming language requires downloading and installing the following library.

[client library package](www.rabbitmq.com/java-client.html)
Unzip it into your working directory and grab the JAR files from the unzipped directory:

	$ unzip rabbitmq-java-client-bin-\*.zip
	$ cp rabbitmq-java-client-bin-\*/\*.jar ./


###Producer
Producer should including the following libraries.

	import com.rabbitmq.client.ConnectionFactory;
	import com.rabbitmq.client.Connection;
	import com.rabbitmq.client.Channel;


For one-to-one message communication, the producer should first initialize a connection to the [robomq.io](http://www.robomq.io) broker.  You can input the following information based on your account.

	ConnectionFactory factory = new ConnectionFactory();
	factory.setUsername(userName);
	factory.setPassword(password);
	factory.setVirtualHost(virtualHost);
	factory.setHost(hostName);
	factory.setPort(portNumber);
	Connection conn = factory.newConnection();
	Channel channel = connection.createChannel();


you can also can use following code to initialize you connection.

	factory.setUri("amqp://userName:password@hostName:portNumber/virtualHost");


Then producer should create a queue and publish messages to this queue. This queue will work as a mailbox where all messages published to it will be stored until they are consumed.

	channel.queueDeclare(QUEUE_NAME, false, false, false, null);

A queue declared without a specific routing key use their queue name as the default routing key.  

Then producer should publish messages to the default exchange attached with routing key. That routing key is the queue name. Based on that routing key, messages will be distributed through the default exchange to the right queue.  

	String message = 'Hello World!';
    	channel.basicPublish('', QUEUE_NAME, null, message.getBytes());

After all messages are published, producer should terminate this connection.

	channel.close( )
	connection.close();


###Consumer
Consumer should including following libraries. 

	import com.rabbitmq.client.ConnectionFactory;
	import com.rabbitmq.client.Connection;
	import com.rabbitmq.client.Channel;
	import com.rabbitmq.client.QueueingConsumer;


First, consumer should initialize connection to the [robomq.io](http://www.robomq.io) server. 

	ConnectionFactory factory = new ConnectionFactory();
	factory.setUri("amqp://userName:password@hostName:portNumber/virtualHost");	
	Connection connection = factory.newConnection();
	Channel channel = connection.createChannel();

Then Consumer should subscribe a queue to listen. (One same queue can be declare multiple times and there's will be only one shows in [robomq.io](http://www.robomq.io))

	QueueingConsumer consumer = new QueueingConsumer(channel);


Then consumer should setting consuming method.
After that, consumer should define the while loop for keep reading messages. User can keep consuming the messages in the queue. User can also add some situation for ending consumer's work.  

	channel.basicConsume(QUEUE_NAME, true, consumer);
	while (true) {
		QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		String message = new String(delivery.getBody());
		System.out.println("Received '" + message + "'");
	}


<b>true</b> is a parameter which indicating that if consumer will automatic send acknowledge back. For this code, we are not setting reply handle mechanism on producer side, so we don't want consumer send auto reply back. That's why we mark this attribute value as True.

###Putting it all together
**producer.java**

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


**consumer.java**

	import com.rabbitmq.client.ConnectionFactory;
	import com.rabbitmq.client.Connection;
	import com.rabbitmq.client.Channel;
	import com.rabbitmq.client.QueueingConsumer;

	public class Consumer {

		private final static String QUEUE_NAME = 'queueName';

		public static void main(String[] argv)
			throws java.io.IOException,
			java.lang.InterruptedException {

			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost('your host');
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			System.out.println('Waiting for messages. To exit press CTRL+C');
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(QUEUE_NAME, true, consumer);

			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String message = new String(delivery.getBody());
				System.out.println("Received '" + message + "'");
			}
		}
	}
	
## Node.js

###Prerequisites
**node.js client AMQP library**
For programming in node.js, we recommend using node-amqp library for implementing AMQP protocol.

For detailed installation steps, refer to [node.js](https://github.com/postwait/node-amqp#connection-options-and-url).


###Producer
Producer should including the following libraries.

	var amqp = require('amqp');


For one-to-one message communication, the producer should first initialize a connection to the [robomq.io](http://www.robomq.io) broker.  You can input the following information based on your account.

	var connection = amqp.createConnection({ host: "yourhost", port: 'port',  login:'username', password:'password', vhost:'vhost-name' });


you can also use following url format to initialize you connection.

	amqp[s]://[user:password@]hostname[:port][/vhost]


Then producer should publish messages to the default exchange attached with routing key. That routing key is the queue name. Based on that routing key, messages will be distributed through the default exchange to the right queue

	connection.on('ready',function(){
		connection.exchange('', options={type:'direct', autoDelete:false}, function(exchange){
			exchange.publish('routingKey', 'hello world');
			print('message sent');
		});
	});
	
After all messages are published, producer should terminate this connection.

	connection.disconnect();


###Consumer
Consumer should including following libraries. 

	var amqp = require('amqp');

First, consumer should initialize connection to the [robomq.io](http://www.robomq.io) server. 

	var connection = amqp.createConnection({ host: 'your host', port: 'port' });

Then consumer should subscribe to a specific queue to listen and consume messages from.

Then consumer should define the while loop for keep reading messages. User can keep consuming the messages in the queue. User can also add some situation for ending consumer's work.  
After that, consumer should setting consuming method.


	connection.on('ready', function(){
		var queue = connection.queue('queueName', options={},function(queue){
			console.log('Declare one queue, name is ' + queue.name);
			queue.bind('', 'routingKey');
			queue.subscribe(function (msg){
				console.log('the message is '+msg.data);
			});
		});
	});
	
###Putting it all together

**producer.js**

	var amqp = require('amqp');
	var connection = amqp.createConnection({ host: 'your host', port: 'port' });

	connection.on('ready',function(){
		connection.exchange('', options={type:'direct', autoDelete:false}, function(exchange){
			exchange.publish('routingKey', 'hello world');
			print('message sent');
		});
	});

**consumer.js**

	var amqp = require('amqp');
	var connection = amqp.createConnection({ host: 'your host', port: 'port' });

	connection.on('ready', function(){
		var queue = connection.queue('queueName', options={},function(queue){
			console.log('Declare one queue, name is ' + queue.name);
			queue.bind('', 'routingKey');
			queue.subscribe(function (msg){
				console.log('the message is '+msg.data);
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
[https://www.rabbitmq.com/amqp-0-9-1-reference.html](https://www.rabbitmq.com/amqp-0-9-1-reference.html)


### Producer
First, producer should initialize a connection to the robomq.io server. 

	amqp_connection_state_t conn = amqp_new_connection();
	amqp_socket_t *socket = NULL;
	char hostname[] = "hostname"; // robomq.io hostname
	int port = 5672; //default
	char user[] = "username"; // robomq.io username
	char password[] = "password"; // robomq.io password
	char vhost[] = "/"; // robomq.io account vhost
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
 

Then, producer should publish messages to the specified exchange attached with routing key. If not specified, that routing key is the queue name. Based on that routing key, messages will be sent through the exchange and distributed to the right queue. 

	amqp_basic_properties_t props;
	props._flags = AMQP_BASIC_CONTENT_TYPE_FLAG | AMQP_BASIC_DELIVERY_MODE_FLAG;
	props.content_type = amqp_cstring_bytes("text/plain");
	props.delivery_mode = 1; /* non-persistent delivery mode */
	amqp_boolean_t mandatory = 0;
	amqp_boolean_t immediate = 0;
	char exchange_name[] = "hello-exchange";
	char routing_key[] = "hola";
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

Finally, after all messaged are produced, producer should terminate this connection.

	amqp_channel_close(conn, channel, AMQP_REPLY_SUCCESS);
	amqp_connection_close(conn, AMQP_REPLY_SUCCESS);
	amqp_destroy_connection(conn);

### Consumer
First, consumer should initialize connection to the robomq.io server.
 
	amqp_connection_state_t conn = amqp_new_connection();
	amqp_socket_t *socket = NULL;
	char hostname[] = "hostname"; // robomq.io hostname
	int port = 5672; //default
	char user[] = "username"; // robomq.io username
	char password[] = "password"; // robomq.io password
	char vhost[] = "/"; // robomq.io account vhost
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

Then consumer should create a queue and subscribe to a queue. This queue will work as a mailbox where all messages published to it will be stored until they are consumed.
The **direct** exchange type is specified below in **exchange_type** definition.

	amqp_bytes_t queue;
	amqp_channel_t channel = 1;
	amqp_boolean_t passive = 0;
	amqp_boolean_t durable = 0;
	amqp_boolean_t exclusive = 0;
	amqp_boolean_t auto_delete = 1;
	char exchange_name[] = "hello-exchange";
	char exchange_type[] = "direct";
	char queue_name[] = "hello-queue";
	char binding_key[] = "hola";
	
	// Declaring exchange
	amqp_exchange_declare(conn, channel, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(exchange_type),
			passive, durable, amqp_empty_table);
	
	if(amqp_get_rpc_reply(conn).reply_type != AMQP_RESPONSE_NORMAL) {
		printf("Error declaring exchange: %d\n", amqp_get_rpc_reply(conn));
		exit(1);
	}
	
	// Declaring queue
	amqp_queue_declare_ok_t *r = amqp_queue_declare(conn, channel, amqp_cstring_bytes(queue_name),
			passive, durable, exclusive, auto_delete, amqp_empty_table);
	
	if(amqp_get_rpc_reply(conn).reply_type != AMQP_RESPONSE_NORMAL) {
		printf("Error declaring queue: %d\n", amqp_get_rpc_reply(conn));
		exit(1);
	}
	queue = amqp_bytes_malloc_dup(r->queue);
	
	// Binding to queue
	amqp_queue_bind(conn, channel, queue, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(binding_key),
			amqp_empty_table);


Note that all the queues declared without specific binding key use the queue name as the default binding key.

At this point, consumer should start consuming messages.
The **no_ack** parameter indicates whether consumer will automatically send acknowledgment back to broker. For this example, producer does not explicitly acknowledge received messages.  Therefore, we set **no_ack** attribute value as true.

Then, consumer should receive messages and implement any desired processing on message contents.

	amqp_boolean_t no_local = 0;
	amqp_boolean_t no_ack = 1;
	amqp_boolean_t exclusive = 0;
	amqp_frame_t frame;
	
	// Consuming the message
	amqp_basic_consume(conn, channel, queue, amqp_empty_bytes, no_local, no_ack, exclusive, amqp_empty_table);
	
	amqp_rpc_reply_t result;
	amqp_envelope_t envelope;
	
	amqp_maybe_release_buffers(conn);
	result = amqp_consume_message(conn, &envelope, NULL, 0);
	
	if (AMQP_RESPONSE_NORMAL == result.reply_type) {
	
		printf("Received message size: %d\nbody: %s\n", envelope.message.body.len, envelope.message.body.bytes);
	
		amqp_destroy_envelope(&envelope);
	}

Finally, after all messaged are consumed, consumer should terminate this connection.

	amqp_channel_close(conn, channel, AMQP_REPLY_SUCCESS);
	amqp_connection_close(conn, AMQP_REPLY_SUCCESS);
	amqp_destroy_connection(conn);

### How to build/run client
Now we have two c files, one is producer.c, another is consumer.c. 
(1) Use the Makefile in sdk repository to compile under a Linux terminal. 
(2) cd to the directory which containing client source code 
(3) Run make all


### Putting it all together
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
		char exchange_name[] = "hello-exchange";
		char routing_key[] = "hola";
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
		char exchange_type[] = "direct";
		char queue_name[] = "hello-queue";
		char binding_key[] = "hola";
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
