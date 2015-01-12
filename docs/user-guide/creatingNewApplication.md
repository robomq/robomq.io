# Sign up for robomq.io

This guide cover the basics of creating messaging applications using robomq.io. You need to have the robomq.io service account created before proceeding with client application development - please see the [Getting Started](gettingStarted.md).

#First application in under 10 lines!

##AMQP client
Now we are going to build our first AMQP application.

### Prerequisites
In robomq.io, first need to register for your username and password. After that, we are going to build your first application. 

Get the pika library installed, you can follow this [tutorial](http://pika.readthedocs.org/en/0.9.14/). 

### Producer
We need to create a file name producer.py. This code going sending the messages to the robomq.io platform. 

In producer.py, first we need to declare a connection to the robomq.io

	import pika

	connection = pika.BlockingConnection(pika.ConnectionParameters(
	               host="your_robomqio_host", port="port#", virtual_host="vhost", ))
	channel = connection.channel()


Second, we should declare a queue for holding message.

	channel.queue_declare(queue="hello");


Now we can send messages. 

	channel.basic_publish(exchange='',
	                      routing_key='hello',
	                      body='Hello World!')


Finally, we can close the connection. 

	connection.close()

### Consumer
Then we need to create a file name consumer.py. 
In this file, we need program for handling the messages leaving in the robomq.io. 

First, we need to declare a connection to robomq.io.

	import pika
	
	connection = pika.BlockingConnection(pika.ConnectionParameters(
	               host="your_robomqio_host", port="port", virtual_host="vhost", ))
	channel = connection.channel()

After that, we need to create a queue. Make sure the name of the queue equals to the routing key used by producer. 

	channel.queue_declare(queue='hello')
	

Then we need define a callback function for processing the messages consumer received. We applied a print method which going print out all the arriving messages. 


	def callback(ch, method, properties, body):
	    print " Received %r" % (body,)


Now we can start consume the messages. 

	channel.basic_consume(callback,
	                      queue='hello',
	                      no_ack=True)
	print 'waiting for messages'
	channel.start_consuming()


### Putting it all together
Full listing of producer and consumer code.. 


**producer.py**

	import pika
	
	connection = pika.BlockingConnection(pika.ConnectionParameters(
	        host='your_robomqio_host'))
	channel = connection.channel()
	channel.queue_declare(queue='hello')
	channel.basic_publish(exchange='',
	                      routing_key='hello',
	                      body='Hello World!')
	print "message sent "
	connection.close()

**consumer.py**

	import pika

	connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='your_robomqio_host'))
	channel = connection.channel()
	channel.queue_declare(queue='hello')
	print ' waiting for messages '
	def callback(ch, method, properties, body):
       print " Received %r" % (body,)
	channel.basic_consume(callback,
                      queue='hello',
                      no_ack=True)
	channel.start_consuming()

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

	client.send("/queue/test", "test message")

At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker.

	client.disconnect()

###Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Next step is to subscribe a destination, so that consumer knows where to listen to. Once it receives a message from the destination, it will print the message body.  

	subscription = client.subscribe(destination, {StompSpec.ACK_HEADER: StompSpec.ACK_AUTO, StompSpec.ID_HEADER: '0'})
	
	while True:
		frame = client.receiveFrame()
		print "%s" % frame.body

###Putting it together

> Before testing the example code, replace hostname, yourvhost, username and password with the real variables in your network environment.  

**producer.py**
	
	import sys
	from stompest.config import StompConfig
	from stompest.sync import Stomp
	
	client = Stomp(StompConfig("tcp://" + hostname + ":61613", login = username, passcode = password, version = "1.2"))
	client.connect(versions = ["1.2"], host = yourvhost)
	
	client.send("/queue/test", "test message")
	
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
		print "%s" % frame.body

##MQTT client
Now we are going to build our first MQTT application.

### Prerequisites
The Python library we use for this example can be found at <https://eclipse.org/paho/clients/python/>. Its source code is at <http://git.eclipse.org/c/paho/org.eclipse.paho.mqtt.python.git/>.  

You can install it through `sudo pip install paho-mqtt`.  

Finally, import this library in your program.

	import paho.mqtt.client as mqtt

The full documentation of this library is at <https://pypi.python.org/pypi/paho-mqtt>.  

This library is built on the basis of a C++ library mosquitto. The documentation of mosquitto is at <http://mosquitto.org>.  

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker and start looping then.  
[Robomq.io](http://www.robomq.io) allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
Many MQTT libraries, including this one, require network looping to complete and maintain the connection with broker. There could be several loop functions for you to choose. If none of them are called, incoming network data will not be processed and outgoing network data may not be sent in a timely fashion.  

	client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
	client.username_pw_set(yourvhost + ":" + username, password)
	client.connect(hostname, 1883, keepalive=60, bind_address="")
	client.loop_start()

After that, producer can send messages to a particular topic.  
In this example, the topic is "test"; It lets user input the message to send.    
	
	message = raw_input("Input message: ")
	client.publish("test", payload=message, qos=1, retain=False)

At last, producer will stop looping and disconnect with the [robomq.io](http://www.robomq.io) broker.    

	client.loop_stop()
	client.disconnect()

### Consumer
The same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker and start looping. The difference is consumer loops forever.  

	client.loop_forever()

The callback function of connecting is to subscribe a topic, so that consumer knows where to listen to.    

	def on_connect(client, userdata, rc):
		client.subscribe([("test", 1)])

Once it receives a message from the queue bound by the topic, it will call the overridden function `on_message()` to print the topic and message payload.  

	def on_message(client, userdata, message):
		print("Topic: " + message.topic + ", Message: " + message.payload)

The callback functions should be preset before connecting to [robomq.io](http://www.robomq.io) broker.  

	client.on_connect = on_connect
	client.on_message = on_message

### Putting it all together

> Before testing the example code, replace hostname, yourvhost, username and password with the real variables in your network environment.  

**producer.py**

	import sys, paho.mqtt.client as mqtt
		
	client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
	client.username_pw_set(yourvhost + ":" + username, password)
	client.connect(hostname, 1883, keepalive=60, bind_address="")
	client.loop_start()
	
	message = raw_input("Input message: ")
	client.publish("test", payload=message, qos=1, retain=False)

	client.loop_stop()
	client.disconnect()

**consumer.py**

	import sys, paho.mqtt.client as mqtt

	def on_connect(client, userdata, rc):
		client.subscribe([("test", 1)])
	
	def on_message(client, userdata, message):
		print("Topic: " + message.topic + ", Message: " + message.payload)
	
	client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
	client.username_pw_set(yourvhost + ":" + username, password)
	client.on_connect = on_connect
	client.on_message = on_message
	client.connect(hostname, 1883, keepalive=60, bind_address="")
	client.loop_forever()
