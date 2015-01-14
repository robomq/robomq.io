# Introduction

> Before reading this chapter, we assume that you already have the basic concepts of message queue, e.g broker, exchange, queue, producer, consumer, etc. Knowing AMQP protocol would very much facilitate understanding MQTT.  

[Robomq.io](http://www.robomq.io) supports MQTT 3.1 as an extension to the AMQP broker.

MQTT stands for Message Queue Telemetry Transport. It is a publish / subscribe, extremely simple and lightweight messaging protocol, designed for constrained devices and low-bandwidth, high-latency or unreliable networks. The design principles are to minimize network bandwidth and device resource requirements whilst also attempting to ensure reliability and some degree of assurance of delivery. These principles also turn out to make the protocol ideal of the emerging "machine-to-machine" (M2M) or "Internet of Things" (IoT) world of connected devices, and for mobile applications where bandwidth and battery power are at a premium. 
> [Full documentation of MQTT](http://mqtt.org)

[Robomq.io](http://www.robomq.io) builds MQTT adapter on top of AMQP exchanges and queues. Messages published to MQTT topics use a topic exchange (amq.topic by default) internally. Subscribers consume from queues bound to the topic exchange. This both enables interoperability with other protocols and makes it possible to use the Management GUI to inspect queue sizes, message rates, and so on.  

# Vhost specification

MQTT protocol itself does not have the concept of vhost and so all MQTT libraries do not provide vhost argument.  
However, [robomq.io](http://www.robomq.io) broker supplemented this feature. You can optionally specify a vhost while connecting, by prepending the vhost to the username and separating with a colon. For example, `/:guest`. If no vhost is specified, it will use the default vhost "/".   

# Durability and Persistence

[Robomq.io](http://www.robomq.io) MQTT adapter assumes two primary usage scenarios:

> QoS stands for quality of service in MQTT. 

* Transient clients that use transient messages (non-persistent, QoS=0). It uses non-durable, auto-delete queues that will be deleted when the client disconnects.
* Stateful clients that use durable subscriptions (non-clean sessions, QoS=1). It uses durable queues. Whether the queues are auto-deleted is controlled by the client's clean session flag. Clients with clean sessions use auto-deleted queues, others use non-auto-deleted ones.

For transient (QoS=0) publishes, the plugin will publish messages as transient (non-persistent). Naturally, for durable (QoS=1) publishes, persistent messages will be used internally.

Queues created for MQTT subscribers will have names starting with mqtt-subscription-, one per subscription QoS level. The queues will have queue TTL depending on MQTT plugin configuration.

# MQTT use cases

We will provide examples in five languages, including Python, Node.js, PHP, Java and C++.  

In the examples, MQTT producer will first ask user for the quantity of messages, then publish the certain number of test messages to a particular topic through MQTT broker. MQTT consumer will subscribe the same topic and print the topic and payload as it receives messages.

The example code provided bellow is the short version, it might have omitted some advanced details. For full version code, please go to our use case [repository](https://github.com/robomq/robomq.io) on GitHub.  

> Before testing the example code, replace hostname, yourvhost, username and password with the real variables in your network environment.  

## Python

### Prerequisite
The Python library we use for this example can be found at <https://eclipse.org/paho/clients/python/>. Its source code is at <http://git.eclipse.org/c/paho/org.eclipse.paho.mqtt.python.git/>.  

You can install it through `sudo pip install paho-mqtt`.  

Finally, import this library in your program.

	import paho.mqtt.client as mqtt

The full documentation of this library is at <https://pypi.python.org/pypi/paho-mqtt>.  

This library is built on the basis of a C++ library mosquitto. The documentation of mosquitto is at <http://mosquitto.org>.  

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
[Robomq.io](http://www.robomq.io) allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
Many MQTT libraries, including this one, require network looping to complete and maintain the connection with broker. There could be several loop functions for you to choose. If none of them are called, incoming network data will not be processed and outgoing network data may not be sent in a timely fashion.  

	client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
	client.username_pw_set(vhost + ":" + username, password)
	client.connect(server, port, keepalive=60, bind_address="")
	client.loop_start()

After that, producer can send messages to a particular topic.   

	client.publish(topic, payload=message, qos=1, retain=False)

At last, producer will stop looping and disconnect with the [robomq.io](http://www.robomq.io) broker.    

	client.loop_stop()
	client.disconnect()

### Consumer
The same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker and start looping.  Not as the producer, this consumer loops forever.

	client.loop_forever()

The callback function of connecting is to subscribe a topic, so that consumer knows where to listen to.  
The second argument of `subscribe()` function is QoS.  

	def on_connect(client, userdata, rc):
		client.subscribe([(topic, 1)])

Once it receives a message from the queue bound by the topic, it will call the overridden function `on_message()` to print the topic and message payload.  

	def on_message(client, userdata, message):
		print("Topic: " + message.topic + ", Message: " + message.payload)

The callback functions should be preset before connecting to [robomq.io](http://www.robomq.io) broker.  

	client.on_connect = on_connect
	client.on_message = on_message

### Putting it together

**producer.py**

	import sys
	import paho.mqtt.client as mqtt
	
	server = "hostname"
	port = 1883
	vhost = "yourvhost"
	username = "username"
	password = "password"
	topic = "test"
	
	try:
		client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
		client.username_pw_set(vhost + ":" + username, password)
		client.connect(server, port, keepalive=60, bind_address="")	#connect
		client.loop_start()	#start loop
	except:
		print("Error: Failed to connect and start loop")
		sys.exit(-1)
	
	msgNum = int(input("Quantity of test messages: "))
	for i in range(msgNum):
		message = "test msg " + str(i + 1)
		try:
			client.publish(topic, payload=message, qos=1, retain=False)	#publish
		except:
			print("Error: Failed to publish message")
			sys.exit(-1)		
	
	try:
		client.loop_stop()	#stop loop
		client.disconnect()
	except:
		print("Error: Failed to stop loop and disconnect")
		sys.exit(-1)

**consumer.py**

	import sys
	import paho.mqtt.client as mqtt
	
	server = "hostname"
	port = 1883
	vhost = "yourvhost"
	username = "username"
	password = "password"
	topic = "test"
	
	def on_connect(client, userdata, rc):	#event on connecting
		try:
			client.subscribe([(topic, 1)])	#subscribe
		except:
			print("Error: Failed to subscribe")
			sys.exit(-1)

	def on_message(client, userdata, message):	#event on receiving message
		print("Topic: " + message.topic + ", Message: " + message.payload)
	
	try:
		client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
		client.username_pw_set(vhost + ":" + username, password)
		client.on_connect = on_connect
		client.on_message = on_message
		client.connect(server, port, keepalive=60, bind_address="")	#connect
		client.loop_forever()	#loop forever
	except:
		print("Error: Failed to connect and start loop")
		sys.exit(-1)

## Node.js

### Prerequisite
The Node.js library we use for this example can be found at <https://github.com/adamvr/MQTT.js>.    

You can install the library through `sudo npm -g install mqtt`.  

Finally, require this library in your program.

	var mqtt = require("mqtt");

The full documentation of this library is at <https://github.com/mqttjs/MQTT.js/wiki>.

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
[Robomq.io](http://www.robomq.io) allows you to specify vhost along with username. See *Vhost specification* section for the detail.  

	var client = mqtt.createClient(port, server, {username: vhost + ":" + username, password: password, keepalive: 10, clean: true, will: null});

Using this library, you will probably incorporate most other functions in the callback of connecting.  

	client.on("connect", callback); 

After that, producer can send messages to a particular topic.  

	client.publish(topic, message, {qos: 1, retain: false});

At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker. The `end()` function contains disconnecting.

	client.end();

### Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

In the callback function of connecting, next step is to subscribe a topic, so that consumer knows where to listen to. It uses a callback function to keep listening. Once it receives a message from the queue bound by the topic, it will print the topic and message payload. 

	client.subscribe(topic, {qos: 1, dup: false})
	.on("message", function(topic, payload, packet) {
		console.log("Topic: " + topic + ", Message: " + payload);
	});

### Putting it together

**producer.js**

	var mqtt = require("mqtt");
	
	var server = "hostname";
	var port = "1883";
	var vhost = "yourvhost";
	var username = "username";
	var password = "password";
	var topic = "test";
	
	var client = mqtt.createClient(port, server, {username: vhost + ":" + username, password: password, keepalive: 10, clean: true, will: null});
	client.on("connect", function() {	//library handles connection errors
		//ask user to input the number of test messages
		process.stdout.write("Quantity of test messages: ");
		process.stdin.on("data", function (msgNum) {
			//send certain number of messages
			try {
				for(var i = 1; i <= msgNum; i++){	
					var message = "test msg " + i;
					client.publish(topic, message, {qos: 1, retain: false});
				}
			} catch(ex) {
				console.log("Error: Failed to send message");
				process.exit(-1);
			}
			//shut down producer after messages sent
			setTimeout(function() {
				client.end();	//includes disconnect()
				process.exit(0);
			}, msgNum * 3);
		});
	});

**consumer.js**

	var mqtt = require("mqtt");
	
	//configuration
	var server = "hostname";
	var port = "1883";
	var vhost = "yourvhost";
	var username = "username";
	var password = "password";
	var topic = "test";
	
	var client = mqtt.createClient(port, server, {username: vhost + ":" + username, password: password, keepalive: 60, clean: true, will: null});
	client.on("connect", function() {	//library handles connection errors
		try {
			client.subscribe(topic, {qos: 1, dup: false})	//chainable API
			.on("message", function(topic, payload, packet) {	//event handling
				console.log("Topic: " + topic + ", Message: " + payload);
			});
		} catch(ex) {
			console.log("Error: Failed to subscribe and receive message");
			process.exit(-1);
		}
	});

## PHP

### Prerequisite
The PHP library we use for this example can be found at <https://github.com/sskaje/mqtt>.  

This library depends on php 5.3+ and php-socket, so first ensure that your have them installed.  
Clone the library through `git clone https://github.com/sskaje/mqtt.git` and move file `spMQTT.class.php` to your project directory.  

Finally require this library in your program. For example,

	require(__DIR__."/spMQTT.class.php");

### Producer
Optionally, to enable the built-in debug function of this library, add a line in the beginning.  

	spMQTTDebug::Enable();

The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
[Robomq.io](http://www.robomq.io) allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
If you set `$clientid = null`, it will be randomly assigned a value.  
`$client->connect()` returns a Boolean value indicating whether the connection is established.   

	$client = new spMQTT("tcp://".$server.":".$port, $clientid=null);	//clientid auto-assigned
	$client->setAuth($vhost.":".$username, $password);
	$client->setConnectClean(true);
	$client->setKeepalive(60);
	$client->connect()

After that, producer can send messages to a particular topic.   
If you set `$msgid=null`, it will be randomly assigned a value.  

	$client->publish($topic, $message, $dup=0, $qos=1, $retain=0, $msgid=null);	//msgid auto-assigned

At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker.    

	$client->disconnect();

### Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Next step is to subscribe some topics, so that consumer knows where to listen to; Then start looping.  
Using this library, you subscribe one or several topics by subscribing an object. Each key-value pair in that object is a topic-qos pair.  
This library requires network looping to complete and maintain the subscription.  
Once it receives a message from the queue bound by the topic, it will call the overridden function `message_callback()` to print the topic and message payload. 

	function message_callback($client, $topic, $payload) {
	    printf("Topic: %s, Message: %s\n", $topic, $payload);
	}

	$topics[$topic] = 1;	//$topics['sepcific topic'] = qos of the subscription to that topic
	$client->subscribe($topics);
	$client->loop("message_callback");	//start looping with message_callback

When you no longer need it, you can also unsubscribe an batch of topics by unsubscribing the array of their names.  

	$client->unsubscribe(array_keys($topics));

### Putting it together

**producer.php**

	<?php
	require(__DIR__ . "/spMQTT.class.php");
	
	$server = "hostname";
	$port = "1883";
	$vhost = "yourvhost";
	$username = "username";
	$password = "password";
	$topic = "test";
	
	$client = new spMQTT("tcp://".$server.":".$port, $clientid=null);	//clientid auto-assigned
	$client->setAuth($vhost.":".$username, $password);
	$client->setConnectClean(true);
	$client->setKeepalive(60);
	
	if ($client->connect()) {
		echo "Quantity of test messages: ";
		$msgNum = rtrim(fgets(STDIN), PHP_EOL);
		for ($i = 1; $i <= $msgNum; $i++) {
			//publish test messages to the topic
			$message = "test msg ".$i;
			try {
				$client->publish($topic, $message, $dup=0, $qos=1, $retain=0, $msgid=null);	//msgid auto-assigned
			} catch(Exception $ex) {
				echo "Error: Failed to send message".PHP_EOL;
				exit(-1);
			}
		}
		sleep($msgNum / 1000);	//to allow all the messages get through
		$client->disconnect();
	}
	else {
		echo "Error: Failed to connect".PHP_EOL;
		exit(-1);
	}
	?>

**consumer.php**

	<?php
	require(__DIR__."/spMQTT.class.php");
	
	$server = "hostname";
	$port = "1883";
	$vhost = "yourvhost";
	$username = "username";
	$password = "password";
	$topic = "test";
	
	$client = new spMQTT("tcp://".$server.":".$port, $clientid=null);	//clientid auto-assigned
	$client->setAuth($vhost.":".$username, $password);
	$client->setConnectClean(true);
	$client->setKeepalive(60);
	
	function message_callback($client, $topic, $payload) {
	    printf("Topic: %s, Message: %s\n", $topic, $payload);
	}
	
	if ($client->connect()) {
		try {
			$topics[$topic] = 1;	//$topics['sepcific topic'] = qos of the subscription to that topic
			$client->subscribe($topics);
			$client->loop("message_callback");	//start looping with message_callback
		} catch(Exception $ex) {
			echo "Error: Failed to subscribe".PHP_EOL;	
			exit(-1);
		}
	}
	else {
		echo "Error: Failed to connect".PHP_EOL;
		exit(-1);
	}
	?>

## Java

### Prerequisite
The Java library we use for this example can be found at <http://www.eclipse.org/paho/clients/java/>.  

Download the library jar file at <http://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/mqtt-client/0.4.0/mqtt-client-0.4.0.jar>, import this library in your program `import org.eclipse.paho.client.mqttv3.*;` and compile your source code with the jar file. For example,  

	javac -cp ".:./mqtt-client-0.4.0.jar" producer.java consumer.java 

Run the producer and consumer classes. For example,  

	java -cp ".:./mqtt-client-0.4.0.jar" consumer
	java -cp ".:./mqtt-client-0.4.0.jar" producer

Of course, you can eventually compress your producer and consumer classes into jar files.

The full documentation of this library is at <http://www.eclipse.org/paho/files/javadoc/index.html>.

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
[Robomq.io](http://www.robomq.io) allows you to specify vhost along with username. See *Vhost specification* section for the detail.  

	private String broker = "tcp://" + server + ":" + port;

	client = new MqttClient(broker, clientId, persistence);
	MqttConnectOptions connOpts = new MqttConnectOptions();
	connOpts.setUserName(vhost + ":" + username);
	connOpts.setPassword(password.toCharArray());
	connOpts.setKeepAliveInterval(60);
	connOpts.setCleanSession(true);
	client.connect(connOpts);

After that, producer can send messages to a particular topic.  
It is remarkable that the message argument of `publish()` function isn't a String. Instead, it is a instance of MqttMessage class. Message payload text is the argument of the constructor of MqttMessage class. It has some public methods to set the headers, e.g. `setQos()`, `setRetained()`, etc.

	client.publish(topic, message);

At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker.

	client.disconnect();

### Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Next step is to subscribe a topic, so that consumer knows where to listen to. Once it receives a message from queue bound by the topic, it will call the overridden function `messageArrived()` to print the topic and message payload.  

	private class onMessage implements MqttCallback {
		public void messageArrived(String topic, MqttMessage message) {		
			System.out.println("Topic: " + topic + ", Message: " + (new String(message.getPayload())));
		}
	}

	onMessage callback = new onMessage();
	client.setCallback(callback);
	client.subscribe(topic, 1);	//qos=1

When you no longer need it, you can also unsubscribe a topic.

	client.unsubscribe(topic);

### Putting it together

**producer.java**

	import org.eclipse.paho.client.mqttv3.MqttClient;
	import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
	import org.eclipse.paho.client.mqttv3.MqttException;
	import org.eclipse.paho.client.mqttv3.MqttMessage;
	import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
	import java.util.Scanner;
	
	public class producer {
	
		private MqttClient client;
		private String server = "hostname";
		private String port = "1883";
		private String broker = "tcp://" + server + ":" + port;
		private String vhost = "yourvhost";
		private String username = "username";
		private String password = "password";
		private String topic = "test";
		private String clientId = MqttClient.generateClientId();
		private MemoryPersistence persistence = new MemoryPersistence();
	
		private void connect() {
			try {
				client = new MqttClient(broker, clientId, persistence);
				MqttConnectOptions connOpts = new MqttConnectOptions();
				connOpts.setUserName(vhost + ":" + username);
				connOpts.setPassword(password.toCharArray());
				connOpts.setKeepAliveInterval(60);
				connOpts.setCleanSession(true);
				client.connect(connOpts);
			} catch(MqttException me) {
				System.out.println("Error: "+me);
				System.exit(-1);
			}
		}
	
		private void publish(int n) {
			for (int i = 0; i < n; i ++) {
				MqttMessage message = new MqttMessage(("test msg " + Integer.toString(i + 1)).getBytes());
				message.setQos(1);
				message.setRetained(false);
				try {
					client.publish(topic, message);
				} catch(MqttException me) {
					System.out.println("Error: "+me);
					System.exit(-1);
				}	
			}
		}
	
		private void disconnect() {
			try {
				client.disconnect();
				System.exit(0);
			} catch(MqttException me) {
				System.out.println("Error: "+me);
				System.exit(-1);			
			}
		}
	
		public static void main(String[] args) {
			producer p = new producer();
			p.connect();
			System.out.print("Quantity of test messages: ");
			Scanner scanner = new Scanner(System.in);
			int msgNum = scanner.nextInt();
			p.publish(msgNum);
			p.disconnect();
		}
	}

**consumer.java**
	
	import org.eclipse.paho.client.mqttv3.MqttClient;
	import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
	import org.eclipse.paho.client.mqttv3.MqttException;
	import org.eclipse.paho.client.mqttv3.MqttMessage;
	import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
	import org.eclipse.paho.client.mqttv3.MqttCallback;
	import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
	
	public class consumer {
	
		private MqttClient client;
		private String server = "hostname";
		private String port = "1883";
		private String broker = "tcp://" + server + ":" + port;
		private String vhost = "yourvhost";
		private String username = "username";
		private String password = "password";
		private String topic = "test";
		private String clientId = MqttClient.generateClientId();
		private MemoryPersistence persistence = new MemoryPersistence();
	
		private class onMessage implements MqttCallback {
			public void messageArrived(String topic, MqttMessage message) {		
				System.out.println("Topic: " + topic + ", Message: " + (new String(message.getPayload())));
			}
			public void connectionLost(Throwable cause) {
			}
			public void deliveryComplete(IMqttDeliveryToken token) {
			}
		}
	
		private void connect() {
			try {
				client = new MqttClient(broker, clientId, persistence);
				MqttConnectOptions connOpts = new MqttConnectOptions();
				connOpts.setUserName(vhost + ":" + username);
				connOpts.setPassword(password.toCharArray());
				connOpts.setKeepAliveInterval(60);
				connOpts.setCleanSession(true);
				client.connect(connOpts);
			} catch(MqttException me) {
				System.out.println("Error: "+me);
				System.exit(-1);
			}
		}
	
		private void subscribe() {
			try {
				onMessage callback = new onMessage();
				client.setCallback(callback);
				client.subscribe(topic, 1);	//qos=1
			} catch(MqttException me) {
				System.out.println("Error: "+me);
				System.exit(-1);		
			}
		}
	
		public static void main(String[] args) {
			consumer c = new consumer();
			c.connect();
			c.subscribe();
		}
	}

## C++

### Prerequisite
The C++ library we use for this example can be found at <http://mosquitto.org/>.   

You will find elaborate installation guide at <http://mosquitto.org/download/>. Install the library according to your operating system. The recommended approach is installing from the source. First, download the latest source package, uncompress it and enter its root directory; Then, run the following two commands:

	make
	sudo make install

Include this library in your program `#include <mosquitto.h>` and compile it by

	g++ producer.cpp -o producer -lmosquitto
	g++ consumer.cpp -o consumer -lmosquitto

See the full documentation of this library at <http://mosquitto.org/documentation/>.

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
[Robomq.io](http://www.robomq.io) allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
Remember to `mosquitto_lib_init();` before creating the mosquitto instance.  
Many MQTT libraries, including this one, require network looping to complete and maintain the connection with broker. There could be several loop functions for you to choose. If none of them are called, incoming network data will not be processed and outgoing network data may not be sent in a timely fashion.  

	string vhusn = vhost + ":" + usn;
	const char *username = vhusn.c_str();

	struct mosquitto *mosq = NULL;

	mosquitto_lib_init();
	mosq = mosquitto_new(NULL, clean_session, NULL);
	mosquitto_username_pw_set(mosq,	username, password);	 
	if(mosquitto_connect(mosq, host, port, keepalive)){
		cout << "Error: Failed to connect" << endl;
		return 1;
	}
	//usually start loop right after connecting
	mosquitto_loop_start(mosq); 

After that, producer can send messages to a particular topic.  
The fourth argument is length of payload char array; The sixth argument is QoS; The seventh argument is retain.    

	mosquitto_publish(mosq, NULL, topic, 20, payload, 1, false);

At last, producer will stop looping and disconnect with the [robomq.io](http://www.robomq.io) broker.  

	mosquitto_loop_stop(mosq, true); 
	mosquitto_disconnect(mosq);
	mosquitto_destroy(mosq);
	mosquitto_lib_cleanup();

### Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker and start looping.  

Then you need to set some callback functions. They play an significant role when using this library. Callback on receiving message is indispensable.  

	void my_message_callback(struct mosquitto *mosq, void *userdata, const struct mosquitto_message *message)
	{
		if(message->payloadlen){
			printf("Topic: %s, Message: %s\n", (char*)message->topic, (char*)message->payload);
		}else{
			printf("Topic: %s, Message: (null)\n", message->topic);
		}
		fflush(stdout);
	}

	mosquitto_message_callback_set(mosq, my_message_callback);

You can also choose to enable the library's built-in log function.  

	void my_log_callback(struct mosquitto *mosq, void *userdata, int level, const char *str)
	{
		printf("%s\n", str);
	}

	mosquitto_log_callback_set(mosq, my_log_callback);

Next step is to subscribe a topic, so that consumer knows where to listen to. Once it receives a message from the queue bound by the topic, it will call `my_message_callback()` function to print the topic and message payload.  

	mosquitto_subscribe(mosq, NULL, topic, 1);

When you no longer need it, you can also unsubscribe a topic.  

	mosquitto_unsubscribe(mosq, NULL, topic);

This consumer example calls `mosquitto_loop_forever()` function, so it will be running there forever. Any code beneath `mosquitto_loop_forever()` will never be reached.  

	while(!mosquitto_loop_forever(mosq, 0, 1)){
	} 

### Putting it together

**producer.cpp**

	#include <stdio.h>
	#include <iostream>
	#include <unistd.h>
	#include <mosquitto.h>
	#include <exception>
	#include <stdlib.h>
	
	using namespace std;

	int main(int argc, char *argv[])
	{
		string hst = "hostname";
		const char *host = hst.c_str();
		int port = 1883;
		string vhost = "yourvhost";
		string usn = "username";
		string vhusn = vhost + ":" + usn;
		const char *username = vhusn.c_str();
		string pwd = "password";
		const char *password = pwd.c_str();
		string tpc = "test";
		const char *topic = tpc.c_str();
		int keepalive = 60;
		bool clean_session = true;
		struct mosquitto *mosq = NULL;

		//create and start consumer
		mosquitto_lib_init();
		mosq = mosquitto_new(NULL, clean_session, NULL);
		mosquitto_username_pw_set(mosq,	username, password);	 
		if(mosquitto_connect(mosq, host, port, keepalive)){
			cout << "Error: Failed to connect" << endl;
			return 1;
		}
		//usually start loop right after connecting
		mosquitto_loop_start(mosq); 
	
		//send certain number of test messages
		int msgNum;
		cout << "Quantity of test messages: ";
		cin >> msgNum;
		char payload[20];
		for (int i = 1; i <= msgNum; i++) {
			sprintf(payload, "test msg %d", i);
			try {
				mosquitto_publish(mosq, NULL, topic, 20, payload, 1, false);
			} catch(exception& e) {
				cout << "Error: Failed to publish message\n" << e.what() << endl;
				return 1;
			}
		}
		sleep(msgNum * 0.02);	//to allow async publish have time to be delivered
	
		//stop producer
		mosquitto_loop_stop(mosq, true); 
		mosquitto_disconnect(mosq);
		mosquitto_destroy(mosq);
		mosquitto_lib_cleanup();
		return 0;
	}

**Consumer**

	#include <stdio.h>
	#include <iostream>
	#include <mosquitto.h>
	#include <exception>
	#include <stdlib.h>
	
	using namespace std;
	
	void my_message_callback(struct mosquitto *mosq, void *userdata, const struct mosquitto_message *message)
	{
		if(message->payloadlen){
			printf("Topic: %s, Message: %s\n", (char*)message->topic, (char*)message->payload);
		}else{
			printf("Topic: %s, Message: (null)\n", message->topic);
		}
		fflush(stdout);
	}
	
	int main(int argc, char *argv[])
	{
		string hst = "hostname";
		const char *host = hst.c_str();
		int port = 1883;
		string vhost = "yourvhost";
		string usn = "username";
		string vhusn = vhost + ":" + usn;
		const char *username = vhusn.c_str();
		string pwd = "password";
		const char *password = pwd.c_str();
		string tpc = "test";
		const char *topic = tpc.c_str();
		int keepalive = 60;
		bool clean_session = true;
	
		//create and start consumer
		struct mosquitto *mosq = NULL;
		mosquitto_lib_init();
		mosq = mosquitto_new(NULL, clean_session, NULL);
		mosquitto_username_pw_set(mosq,	username, password);	 
		mosquitto_message_callback_set(mosq, my_message_callback);
		if(mosquitto_connect(mosq, host, port, keepalive)){
			printf("Error: Failed to connect\n");
			return 1;
		}
		try {
			mosquitto_subscribe(mosq, NULL, topic, 1); 
		} catch(exception& e) {
			printf("Error: Failed to subscribe\n%s\n", e.what());
			return 1;
		}
		//looping is essential for consumer to work
		while(!mosquitto_loop_forever(mosq, 0, 1)){
		}
	
		return 0; 
	}