# Sign up for robomq.io

This guide covers the basics of creating messaging applications using [robomq.io](http://www.robomq.io). You need to have the [robomq.io](http://www.robomq.io) service account created before proceeding with client application development - please see the [Getting Started](gettingStarted.md).

#First application in under 10 lines!

##AMQP client
Now we are going to build our first AMQP application.

###Prerequisite
The Python library we use for this example can be found at <https://github.com/pika/pika>.  

You can install it through `sudo pip install pika`.  

Finally, import this library in your program.

	import pika

The full documentation of this library is at <https://pika.readthedocs.org/en/0.9.14/>.

###Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  

	connection = pika.BlockingConnection(pika.ConnectionParameters(host = hostname, port = 5672, virtual_host = yourvhost, credentials = pika.PlainCredentials(username, password)))
	channel = connection.channel()

Then producer can publish messages to the direct exchange where messages will be delivered to queues whose routing key matches.  

	channel.basic_publish(exchange = "amq.direct", routing_key = "test", body = "Hello World!", properties = None)

At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker.

	connection.close()

###Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Then consumer will declare a queue, and bind the queue to the direct exchange with a routing key. The routing key decides what messages will the queue receive.    

	channel.queue_declare(queue = "testQ")
	channel.queue_bind(exchange = "amq.direct", queue = "testQ", routing_key = "test")

Finally, consumer can consume messages from the queue.  

	channel.basic_consume(consumer_callback = onMessage, queue = "testQ", no_ack = True)
	channel.start_consuming()

When messages are received, a callback function `onMessage()` will be invoked to print the message content.  

	def onMessage(channel, method, properties, body):
		print body

###Putting it together

> Before testing the example code, replace hostname, yourvhost, username and password with the real variables in your network environment.  

**producer.py**
	
	import pika
	
	connection = pika.BlockingConnection(pika.ConnectionParameters(host = hostname, port = 5672, virtual_host = yourvhost, credentials = pika.PlainCredentials(username, password)))
	channel = connection.channel()
	
	channel.basic_publish(exchange = "amq.direct", routing_key = "test", body = "Hello World!", properties = None)
	
	connection.close()

**consumer.py**
	
	import pika

	def onMessage(channel, method, properties, body):
		print body
	
	connection = pika.BlockingConnection(pika.ConnectionParameters(host = hostname, port = 5672, virtual_host = yourvhost, credentials = pika.PlainCredentials(username, password)))
	channel = connection.channel()

	channel.queue_declare(queue = "testQ")
	channel.queue_bind(exchange = "amq.direct", queue = "testQ", routing_key = "test")

	channel.basic_consume(consumer_callback = onMessage, queue = "testQ", no_ack = True)
	channel.start_consuming()

##MQTT client
Now we are going to build our first MQTT application.

### Prerequisites
The Python library we use for this example can be found at <https://eclipse.org/paho/clients/python/>. Its source code is at <http://git.eclipse.org/c/paho/org.eclipse.paho.mqtt.python.git/>.  

You can install it through `sudo pip install paho-mqtt`.  

Finally, import this library in your program.

	import paho.mqtt.client as mqtt

The full documentation of this library is at <https://pypi.python.org/pypi/paho-mqtt>.  

> This library is built on the basis of a C++ library mosquitto. The documentation of mosquitto is at <http://mosquitto.org>.  

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker and start looping then.  
[robomq.io](http://www.robomq.io) allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
Many MQTT libraries, including this one, require network looping to complete and maintain the connection with broker. There could be several loop functions for you to choose. If none of them are called, incoming network data will not be processed and outgoing network data may not be sent in a timely fashion.  

	client = mqtt.Client()
	client.username_pw_set(yourvhost + ":" + username, password)
	client.connect(hostname, 1883)
	client.loop_start()

After that, producer can send messages to a particular topic.  
In this example, the topic is "test"; It lets user input the message to send.    
	
	message = raw_input("Input message to send: ")
	client.publish(topic = "test", payload = message)

At last, producer will stop looping and disconnect with the [robomq.io](http://www.robomq.io) broker.    

	client.loop_stop()
	client.disconnect()

### Consumer
The same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker and start looping. The difference is consumer loops forever.  

	client.loop_forever()

After connecting, consumer will subscribe a topic, so that consumer knows where to listen to.    

	client.subscribe("test")

Once it receives a message from the queue bound by the topic, it will call the callback function `onMessage()` to print the topic and message payload.  

	def onMessage(client, userdata, message):
		print("Topic: " + message.topic + ", Message: " + message.payload)

The callback functions should be preset before connecting to [robomq.io](http://www.robomq.io) broker.  

	client.on_message = onMessage

### Putting it all together

> Before testing the example code, replace hostname, yourvhost, username and password with the real variables in your network environment.  

**producer.py**

	import sys, paho.mqtt.client as mqtt
		
	client = mqtt.Client()
	client.username_pw_set(yourvhost + ":" + username, password)
	client.connect(hostname, 1883)
	client.loop_start()
	
	message = raw_input("Input message to send: ")
	client.publish(topic = "test", payload = message)

	client.loop_stop()
	client.disconnect()

**consumer.py**

	import sys, paho.mqtt.client as mqtt
	
	def onMessage(client, userdata, message):
		print("Topic: " + message.topic + ", Message: " + message.payload)
	
	client = mqtt.Client()
	client.username_pw_set(yourvhost + ":" + username, password)
	client.on_message = onMessage
	client.connect(hostname, 1883)
	client.subscribe("test")
	client.loop_forever()

##STOMP client
Now we are going to build our first STOMP application.

###Prerequisite
The Python library we use for this example can be found at <https://pypi.python.org/pypi/stompest/>. Its GitHub repository is at <https://github.com/nikipore/stompest>.  
It supports STOMP version 1.0, 1.1 and 1.2.  

You can install it through `sudo pip install stompest`.  
The full documentation of this library is at <http://nikipore.github.io/stompest/>.

###Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
> In STOMP, username is called login and password is called passcode. 

	client = Stomp(StompConfig("tcp://" + hostname + ":61613", login = username, passcode = password, version = "1.2"))
	client.connect(versions = ["1.2"], host = yourvhost)

After that, producer can send messages to a particular destination. In this example, it is a queue bound to the default exchange, but it can be replaced by other types of destinations to perform the corresponding messaging. The *Message destinations* section in *STOMP* chapter elaborates it. 

	client.send(destination = "/queue/test", body = "Hello World!", headers = None)

At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker.

	client.disconnect()

###Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Next step is to subscribe a destination, so that consumer knows where to listen to. Once it receives a message from the destination, it will print the message body.  

	subscription = client.subscribe("/queue/test", {StompSpec.ACK_HEADER: StompSpec.ACK_AUTO, StompSpec.ID_HEADER: '0'})
	
	while True:
		frame = client.receiveFrame()
		print frame.body

###Putting it together

> Before testing the example code, replace hostname, yourvhost, username and password with the real variables in your network environment.  

**producer.py**
	
	import sys
	from stompest.config import StompConfig
	from stompest.sync import Stomp
	
	client = Stomp(StompConfig("tcp://" + hostname + ":61613", login = username, passcode = password, version = "1.2"))
	client.connect(versions = ["1.2"], host = yourvhost)
	
	client.send(destination = "/queue/test", body = "Hello World!", headers = None)
	
	client.disconnect()

**consumer.py**
	
	import sys
	from stompest.config import StompConfig
	from stompest.protocol import StompSpec
	from stompest.sync import Stomp
	
	client = Stomp(StompConfig("tcp://" + hostname + ":61613", login = username, passcode = password, version = "1.2"))
	client.connect(versions = ["1.2"], host = yourvhost)
	
	subscription = client.subscribe("/queue/test", {StompSpec.ACK_HEADER: StompSpec.ACK_AUTO, StompSpec.ID_HEADER: '0'})
			
	while True:
		frame = client.receiveFrame()
		print frame.body
