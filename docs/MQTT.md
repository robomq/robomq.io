# Introduction

> Before reading this chapter, we assume that you already have the basic concepts of message queue, e.g broker, exchange, queue, producer, consumer, etc. Knowing AMQP protocol would very much facilitate understanding MQTT.  

<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> supports MQTT 3.1 as an extension to the AMQP broker. Its port is **1883**, SSL port is **8883**.

MQTT stands for Message Queue Telemetry Transport. It is a publish / subscribe, extremely simple and lightweight messaging protocol, designed for constrained devices and low-bandwidth, high-latency or unreliable networks. The design principles are to minimize network bandwidth and device resource requirements whilst also attempting to ensure reliability and some degree of assurance of delivery. These principles also turn out to make the protocol ideal of the emerging "machine-to-machine" (M2M) or "Internet of Things" (IoT) world of connected devices, and for mobile applications where bandwidth and battery power are at a premium. 
<a href="https://mqtt.org" target="_blank">Full documentation of MQTT</a>

<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> builds MQTT adapter on top of AMQP exchanges and queues. Messages published to MQTT topics use a topic exchange (amq.topic by default) internally. Subscribers consume from queues bound to the topic exchange. This both enables interoperability with other protocols and makes it possible to use the Management GUI to inspect queue sizes, message rates, and so on.  

# Vhost specification

MQTT protocol itself does not have the concept of vhost and so all MQTT libraries do not provide vhost argument.  
However, <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker supplemented this feature. You can optionally specify a vhost while connecting, by prepending the vhost to the username and separating with a colon. For example, `/:guest`. If no vhost is specified, it will use the default vhost "/".   

# Durability and Persistence

<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> MQTT adapter assumes two primary usage scenarios:

> QoS stands for quality of service in MQTT. <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> supports QoS up to 1.

* Transient clients that use transient messages (non-persistent, QoS=0). It uses non-durable, auto-delete queues that will be deleted when the client disconnects.  
* Stateful clients that use durable subscriptions (non-clean sessions, QoS=1). It uses durable queues. Whether the queues are auto-deleted is controlled by the client's clean session flag. Clients with clean sessions use auto-deleted queues, others use non-auto-deleted ones.   

For transient (QoS=0) publishes, <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> will publish messages as transient (non-persistent). Naturally, for durable (QoS=1) publishes, persistent messages will be used internally.

Queues created for MQTT subscribers will have names starting with mqtt-subscription-, one per subscription QoS level.  

# MQTT use cases

We will provide examples in five languages, including Python, Node.js, PHP, Java and C++.  

In the examples, MQTT producer will first ask user for the quantity of messages, then publish the certain number of test messages to a particular topic through MQTT broker. MQTT consumer will subscribe the same topic and print the topic and payload as it receives messages.  

All examples have implemented automatic reconnecting, which is crucial in real production.

The example code provided bellow could be the short version, it might have omitted some advanced details. For full version code, please go to our SDK <a href="https://github.com/robomq/robomq.io/tree/master/sdk/MQTT" target="_blank">repository</a> on GitHub.  

> Before testing the example code, replace hostname, yourvhost, username and password with the real variables in your network environment.  
> Always run consumer first to create the exchange and queue for producer to send messages to.   

## Python

### Prerequisite
The Python library we use for this example can be found at <a href="https://eclipse.org/paho/clients/python/" target="_blank">https://eclipse.org/paho/clients/python/</a>. Its source code is at <a href="https://git.eclipse.org/c/paho/org.eclipse.paho.mqtt.python.git/" target="_balnk">https://git.eclipse.org/c/paho/org.eclipse.paho.mqtt.python.git/</a>.  

You can install it through `sudo pip install paho-mqtt`.  

Finally, import this library in your program.

```python
import paho.mqtt.client as mqtt
```

The full documentation of this library is at <a href="https://pypi.python.org/pypi/paho-mqtt" target="_blank">https://pypi.python.org/pypi/paho-mqtt</a>.  

> This library is built on the basis of a C++ library mosquitto. The documentation of mosquitto is at <a href="https://mosquitto.org" target="_blank">https://mosquitto.org</a>.  

### Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
Set keep alive to 60 seconds, so that client will confirm the connectivity with broker.  
Many MQTT libraries, including this one, require network looping to complete and maintain the connection with broker. There could be several loop functions for you to choose. If none of them are called, incoming network data will not be processed and outgoing network data may not be sent in a timely fashion.  

```python
client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
client.username_pw_set(vhost + ":" + username, password)
client.connect(server, port, keepalive=60, bind_address="")
client.loop_start()
```

After that, producer can send messages to a particular topic.   

```python
client.publish(topic, payload=message, qos=1, retain=False)
```

At last, producer will stop loop and disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.    

```python
client.loop_stop()
client.disconnect()
```

### Consumer
The same as producer, consumer needs to connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker and start loop.  Not as the producer, this consumer loops forever.

```python
client.loop_forever()
```

The callback function of connecting is to subscribe a topic, so that consumer knows where to listen to.  
The second argument in `subscribe()` function is QoS.  

```python
def onConnect(client, userdata, rc):
	client.subscribe([(topic, 1)])
```

Once it receives a message from the queue bound by the topic, it will trigger the callback function `onMessage()` to print the topic and message payload.  

```python
def onMessage(client, userdata, message):
	print("Topic: " + message.topic + ", Message: " + message.payload)
```

The callback functions should be preset before connecting to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

```python
client.on_connect = onConnect
client.on_message = onMessage
```

When you no longer need it, you can also unsubscribe a topic.

```python
client.unsubscribe(topic)
```

### Putting it together

**producer.py**

```python
import time
import paho.mqtt.client as mqtt
	
server = "hostname"
port = 1883
vhost = "yourvhost"
username = "username"
password = "password"
topic = "test/any"
	
try:
	client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
	client.username_pw_set(vhost + ":" + username, password)
	client.connect(server, port, keepalive=60, bind_address="")	#connect
	client.loop_start()	#start loop
	msgNum = int(input("Quantity of test messages: "))
	for i in range(msgNum):
		message = "test msg " + str(i + 1)
		client.publish(topic, payload=message, qos=1, retain=False)	#publish
		time.sleep(1)
	client.loop_stop()	#stop loop
	client.disconnect()
except Exception, e:
	print e
```

**consumer.py**

```python
import time
import paho.mqtt.client as mqtt
	
server = "hostname"
port = 1883
vhost = "yourvhost"
username = "username"
password = "password"
topic = "test/#"
	
"""
 * This method is the callback on connecting to broker.
 * @ It subscribes the target topic.
"""
def onConnect(client, userdata, rc):	#event on connecting
	client.subscribe([(topic, 1)])	#subscribe
	
"""
 * This method is the callback on receiving messages.
 * @ It prints the message topic and payload on console.
"""
def onMessage(client, userdata, message):	#event on receiving message
	print("Topic: " + message.topic + ", Message: " + message.payload)
	
while True:
	try:
		client = mqtt.Client(client_id="", clean_session=True, userdata=None, protocol="MQTTv31")
		client.username_pw_set(vhost + ":" + username, password)
		client.on_connect = onConnect
		client.on_message = onMessage
		client.connect(server, port, keepalive=60, bind_address="")	#connect
		client.loop_forever()	#automatically reconnect once loop forever
	except Exception, e:
		#when initialize connection, reconnect on exception
		print "Exception handled, reconnecting...\nDetail:\n%s" % e 
		time.sleep(5)
```

## Node.js

### Prerequisite
The Node.js library we use for this example can be found at <a href="https://github.com/adamvr/MQTT.js" target="_blank">https://github.com/adamvr/MQTT.js</a>.    

You can install the library through `sudo npm install mqtt`.  

Finally, require this library in your program.

```javascript
var mqtt = require("mqtt");
```

The full documentation of this library is at <a href="https://github.com/mqttjs/MQTT.js/wiki" target="_blank">https://github.com/mqttjs/MQTT.js/wiki</a>.

### Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
Set keep alive to 60 seconds, so that client will confirm the connectivity with broker.  

```javascript
var client = mqtt.connect("mqtt://" + server + ":" + port, {username: vhost + ":" + username, password: password, keepalive: 60, clean: true, will: null});
```

Using this library, you will probably incorporate most other functions in the callback on connect.  

```javascript
client.on("connect", callback); 
```

After that, producer can send messages to a particular topic.  

```javascript
client.publish(topic, message, {qos: 1, retain: false});
```

At last, producer will disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker. The `end()` function contains disconnecting.

```javascript
client.end();
```

### Consumer
The first step is the same as producer, consumer needs to connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

In the callback function on connect, next step is to subscribe a topic, so that consumer knows where to listen to. It uses a callback function to handle incoming messages. Once it receives a message from the queue bound by the topic, it will print the topic and message payload. 

```javascript
client.subscribe(topic, {qos: 1, dup: false})
.on("message", function(topic, payload, packet) {
	console.log("Topic: " + topic + ", Message: " + payload);
});
```

When you no longer need it, you can also unsubscribe a topic.

```javascript
client.unsubscribe(topic, callback);
```

### Putting it together

**producer.js**

```javascript
var mqtt = require("mqtt");
	
var server = "hostname";
var port = "1883";
var vhost = "yourvhost";
var username = "username";
var password = "password";
var topic = "test/any";
	
var client = mqtt.connect("mqtt://" + server + ":" + port, {username: vhost + ":" + username, password: password, keepalive: 60, clean: true, will: null});
client.on("connect", function() {	//this library automatically reconnects on errors
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
			console.log(ex);
			process.exit(-1);
		}
		//shut down producer after messages sent
		setTimeout(function() {
			client.end();	//includes disconnect()
			process.exit(0);
		}, msgNum);
	});
});
```

**consumer.js**

```javascript
var mqtt = require("mqtt");
	
var server = "hostname";
var port = "1883";
var vhost = "yourvhost";
var username = "username";
var password = "password";
var topic = "test/#";
	
var client = mqtt.connect("mqtt://" + server + ":" + port, {username: vhost + ":" + username, password: password, keepalive: 60, clean: true, will: null});
client.on("connect", function() {	//this library automatically reconnects on errors
	try {
		client.subscribe(topic, {qos: 1, dup: false})	//chainable API
		.on("message", function(topic, payload, packet) {	//event handling
			console.log("Topic: " + topic + ", Message: " + payload);
		});
	} catch(ex) {
		console.log(ex);
	}
});
```

## PHP

### Prerequisite
The PHP library we use for this example can be found at <https://github.com/mgdm/Mosquitto-PHP/>.  

This library depends on php 5.3+ and <a href="https://mosquitto.org/" target="_blank">libmosquitto</a>, so first ensure that your have them installed.  
You may obtain the package using PECL `sudo pecl install Mosquitto-alpha`.  
Now you should see `mosquitto.so` in your php shared library directory, e.g `/usr/lib/php5/20121212/`. Finally, edit your `php.ini`. In *Dynamic Extensions* section, add one line `extension=mosquitto.so`.  

After installation, you don't need to explicitly require this library in your PHP script. Your PHP interpreter will integrate it for you.  

### Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
In the constructor of client, first parameter is client ID, second is boolean flag for clean session.  
The third parameter of connect function is keep alive the in seconds. Set keep alive to 60 seconds, so that client will confirm the connectivity with broker.  
   
```php
$client = new Mosquitto\Client("1", true);
$client->setCredentials($vhost.":".$username, $password);
$client->connect($server, $port, 60);
```

After that, producer can send messages to a particular topic.   
The third parameter is QoS, fourth is boolean flag for retain.  

```php
$client->publish($topic, $message, 1, false);
```

Many MQTT libraries, including this one, require network looping to complete and maintain the connection with broker. There could be several loop functions for you to choose. If none of them are called, incoming network data will not be processed and outgoing network data may not be sent in a timely fashion.  
It is strongly recommended that you call `loop()` each time you send a message.  

```php
$client->loop();
```

At last, producer will disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.    

```php
$client->disconnect();
```

### Consumer
The first step is the same as producer, consumer needs to connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker. Not as the producer, this consumer loops forever.  

```php
$client->loopForever();
```

The next step is to subscribe a topic, so that consumer knows where to listen to.  
The second argument in `subscribe()` function is QoS.  

```php
client->subscribe(topic, 1);
```
Once it receives a message from the queue bound by the topic, it will trigger the callback function `onMessage()` to print the topic and message payload.  

```php
function onMessage($message) {
    printf("Topic: %s, Message: %s\n", $message->topic, $message->payload);
}
```

The callback functions should be preset before connecting to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker. Foe example,  

```php
$client->onMessage("onMessage");
```

When you no longer need it, you can also unsubscribe a topic.

```php
client->unsubscribe(topic, qos);
```

### Putting it together

**producer.php**

```php
<?php
$server = "hostname";
$port = "1883";
$vhost = "yourvhost";
$username = "username";
$password = "password";
$topic = "test/any";
	
try {
	$client = new Mosquitto\Client("1", true); //clientid="1", clean_session=true
	$client->setCredentials($vhost.":".$username, $password);
	$client->connect($server, $port, 60); //keepalive=60
	echo "Quantity of test messages: ";
	$msgNum = rtrim(fgets(STDIN), PHP_EOL);
	for ($i = 1; $i <= $msgNum; $i++) {
		$message = "test msg ".$i;
		$client->publish($topic, $message, 1, false); //publish test messages to the topic
		$client->loop(); //frequently loop to to keep communications with broker
		sleep(1);
	}
	$client->disconnect();
} catch (Exception $e) {
	echo $e;
}
?>
```

**consumer.php**

```php
<?php
$GLOBALS["client"] = $client;
$GLOBALS["topic"] = $topic;
	
$server = "hostname";
$port = "1883";
$vhost = "yourvhost";
$username = "username";
$password = "password";
$topic = "test/#";
	
function subscribe() {
	$GLOBALS["client"]->subscribe($GLOBALS["topic"], 1); //qos=1
}
	
/**
 * This method is the callback on receiving messages.
 * @ It prints the message topic and payload on console.
 */
function onMessage($message) {
    printf("Topic: %s, Message: %s\n", $message->topic, $message->payload);
}
	
while (true) {
	try {
		$client = new Mosquitto\Client("0", true); //clientid="0", clean_session=true
		$client->setCredentials($vhost.":".$username, $password);
		$client->onConnect("subscribe");
		$client->onMessage("onMessage");
		$client->connect($server, $port, 60); //keepalive=60
		$client->loopForever(); //automatically reconnect when loopForever
	} catch (Exception $e) {
		//when initialize connection, reconnect on exception
		echo "Exception handled, reconnecting...\nDetail:\n".$e."\n";
		sleep(5);
	}
}
?>
```

## Ruby

### Prerequisite
The Ruby gem we use for this example can be found at <a href="https://rubygems.org/gems/mqtt" target="_blank">https://rubygems.org/gems/mqtt</a>. Its source code is at <a href="https://github.com/njh/ruby-mqtt" target="_blank">https://github.com/njh/ruby-mqtt</a>  

You can install it through `gem install mqtt`.  

Finally, require this gem in your program.

```ruby
require 'mqtt'
```

The full documentation of this gem is at <a href="https://www.rubydoc.info/gems/mqtt/" target="_blank">https://www.rubydoc.info/gems/mqtt/</a>.  

### Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
Set keep alive to 60 seconds, so that client will confirm the connectivity with broker.  

```ruby
client = MQTT::Client.connect(
  :host => server,
  :port => port,
  :username => "#{vhost}:#{username}",
  :password => password,
  :version => "3.1.0",
  :keep_alive => 60,
  :clean_session => true,
  :client_id => "",
  :will_qos => 1,
  :will_retain => false
)
```

After that, producer can send messages to a particular topic.

```ruby
client.publish(topic, msg)
```

At last, producer will disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.

```ruby
client.disconnect
```

### Consumer
The first step is the same as producer, consumer needs to connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

Next step is to subscribe a topic, so that consumer knows where to listen to.

```ruby
subscription = client.subscribe([topic,1])
```

To receive a message, use the `get` method. This method will block until a message is available. If you give it a block, then the block will be executed for every message received.

```ruby
client.get do |topic, message|
  onMessage(topic, message)
end
```

When you no longer need it, you can also unsubscribe a topic.

```ruby
client.unsubscribe(topic)
```

### Putting it together

**producer.rb**

```ruby
require "mqtt"

# connection options
server = "hostname"
port = 1883
vhost = "yourvhost"
username = "username"
password = "password"
topic = "test/any"

print "Quantity of test messages: "
msgNum = gets.to_i

# create connection
begin
  client = MQTT::Client.connect(
      :host => server,
      :port => port,
      :username => "#{vhost}:#{username}",
      :password => password,
      :version => "3.1.0",
      :keep_alive => 60,
      :clean_session => true,
      :client_id => "",
      :will_qos => 1,
      :will_retain => false
  )

  # publish messages
  (1..msgNum).each do |counter|
    msg = "test msg  #{counter}"
    client.publish(topic, msg)
    sleep 1
  end

  client.disconnect
end
```

**consumer.rb**

```ruby
require "mqtt"

# connection options
server = "hostname"
port = 1883
vhost = "yourvhost"
username = "username"
password = "password"
topic = "test/any"

# event on receiving message
def onMessage(topic, message)
  puts "Topic: #{topic}, Message: #{message}"
end

# create connection and keep getting messages
loop do
  begin
    # connect
    client = MQTT::Client.connect(
      :host => server,
      :port => port,
      :username => "#{vhost}:#{username}",
      :password => password,
      :version => "3.1.0",
      :keep_alive => 60,
      :clean_session => true,
      :client_id => "",
      )

    # subscribe
    client.subscribe([topic,1])

    client.get do |topic, message|
      onMessage(topic, message)
    end
  rescue MQTT::ProtocolException => pe
    puts "Exception handled, reconnecting...\nDetail:\n#{pe.message}"
    sleep 5
  end
end
```

## Java

### Prerequisite
The Java library we use for this example can be found at <a href="https://www.eclipse.org/paho/clients/java/" target="_blank">https://www.eclipse.org/paho/clients/java/</a>.  

Download the library jar file at <a href="https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/mqtt-client/0.4.0/mqtt-client-0.4.0.jar" target="_blank">https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/mqtt-client/0.4.0/mqtt-client-0.4.0.jar</a>, import this library in your program `import org.eclipse.paho.client.mqttv3.*;` and compile your source code with the jar file. For example,  

```bash
javac -cp ".:./mqtt-client-0.4.0.jar" Producer.java Consumer.java 
```
Run the producer and consumer classes. For example,  

```bash
java -cp ".:./mqtt-client-0.4.0.jar" Consumer
java -cp ".:./mqtt-client-0.4.0.jar" Producer
```
Of course, you can eventually compress your producer and consumer classes into jar files.

The full documentation of this library is at <http://www.eclipse.org/paho/files/javadoc/index.html>.

### Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
Set keep alive to 60 seconds, so that client will confirm the connectivity with broker.  

```java
private String broker = "tcp://" + server + ":" + port;
private String clientId = MqttClient.generateClientId();
private MemoryPersistence persistence = new MemoryPersistence();

client = new MqttClient(broker, clientId, persistence);
MqttConnectOptions connOpts = new MqttConnectOptions();
connOpts.setUserName(vhost + ":" + username);
connOpts.setPassword(password.toCharArray());
connOpts.setKeepAliveInterval(60);
connOpts.setCleanSession(true);
client.connect(connOpts);
```

After that, producer can send messages to a particular topic.  
It is remarkable that the message argument of `publish()` function isn't a String. Instead, it is a instance of MqttMessage class. Message payload text is the argument of the constructor of MqttMessage class. It has some public methods to set the headers, e.g. `setQos()`, `setRetained()`, etc.

```java
client.publish(topic, message);
```

At last, producer will disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.

```java
client.disconnect();
```

### Consumer
The first step is the same as producer, consumer needs to connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

Next step is to subscribe a topic, so that consumer knows where to listen to. You need to set the callback on message before subscribe. Once it receives a message from queue bound by the topic, it will call the overridden function `messageArrived()` to print the topic and message payload.  
The second parameter of `subscribe()` function is QoS.  

```java
private class onMessage implements MqttCallback {
	public void messageArrived(String topic, MqttMessage message) {		
		System.out.println("Topic: " + topic + ", Message: " + (new String(message.getPayload())));
	}
	public void connectionLost(Throwable cause) {}
	public void deliveryComplete(IMqttDeliveryToken token) {}
}

onMessage callback = new onMessage();
client.setCallback(callback);
client.subscribe(topic, 1);
```

When you no longer need it, you can also unsubscribe a topic.

```java
client.unsubscribe(topic);
```

### Putting it together

**Producer.java**

```java
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.Scanner;
	
public class Producer {
	
	private MqttClient client;
	private String server = "hostname";
	private String port = "1883";
	private String broker = "tcp://" + server + ":" + port;
	private String vhost = "yourvhost";
	private String username = "username";
	private String password = "password";
	private String topic = "test/any";
	private String clientId = MqttClient.generateClientId();
	private MemoryPersistence persistence = new MemoryPersistence();
	
	private void produce() {
		try {
			client = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setUserName(vhost + ":" + username);
			connOpts.setPassword(password.toCharArray());
			connOpts.setKeepAliveInterval(60);
			connOpts.setCleanSession(true);
			client.connect(connOpts);
			System.out.print("Quantity of test messages: ");
			Scanner scanner = new Scanner(System.in);
			int msgNum = scanner.nextInt();
			for (int i = 0; i < msgNum; i ++) {
				MqttMessage message = new MqttMessage(("test msg " + Integer.toString(i + 1)).getBytes());
				message.setQos(1);
				message.setRetained(false);
				client.publish(topic, message);
				try {
					Thread.sleep(1000);
				} catch (Exception e) {}
			}
			client.disconnect();
		} catch(MqttException me) {
			System.out.println(me);
			System.exit(-1);			
		}
	}
	
	public static void main(String[] args) {
		Producer p = new Producer();
		p.produce();
	}
}
```

**Consumer.java**
	
```java
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
	
public class Consumer {
	
	private MqttClient client;
	private String server = "hostname";
	private String port = "1883";
	private String broker = "tcp://" + server + ":" + port;
	private String vhost = "yourvhost";
	private String username = "username";
	private String password = "password";
	private String topic = "test/#";
	private String clientId = MqttClient.generateClientId();
	private MemoryPersistence persistence = new MemoryPersistence();
	private boolean connected = false;
	
	/**
	 * This method is the overridden callback on receiving messages.
	 * @ It is event-driven. You don't call it in your code.
	 * @ It prints the message topic and payload on console.
	 * @ There're other callback functions provided by this library.
	 */
	private class onMessage implements MqttCallback {
	
		public void messageArrived(String topic, MqttMessage message) {		
			System.out.println("Topic: " + topic + ", Message: " + (new String(message.getPayload())));
		}
	
		public void connectionLost(Throwable cause) {
			System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", cause.getMessage());
			connected = false; //reconnect on exception
		}
	
		public void deliveryComplete(IMqttDeliveryToken token) {
		}
	}
	
	private void consume() {
		while (true) {
			try {
				client = new MqttClient(broker, clientId, persistence);
				MqttConnectOptions connOpts = new MqttConnectOptions();
				connOpts.setUserName(vhost + ":" + username);
				connOpts.setPassword(password.toCharArray());
				connOpts.setKeepAliveInterval(60);
				connOpts.setCleanSession(true);
				client.connect(connOpts);
				onMessage callback = new onMessage();
				client.setCallback(callback);
				client.subscribe(topic, 1);	//qos=1
				connected = true;
				while (connected) { //check connection status
					try {
						Thread.sleep(5000);
					} catch (Exception e) {}
				} 
			} catch(MqttException me) {
				//reconnect on exception
				System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", me); 
				try {
					Thread.sleep(5000); 
				} catch(Exception e) {}
			}
		}
	}
	
	public static void main(String[] args) {
		Consumer c = new Consumer();
		c.consume();
	}
}
```

## C++

### Prerequisite
The C++ library we use for this example can be found at <a href="https://mosquitto.org/" target="_blank">http://mosquitto.org/</a>.   

You will find elaborate installation guide at <a href="https://mosquitto.org/download/" target="_blank">https://mosquitto.org/download/</a>. Install the library according to your operating system. The recommended approach is installing from the source. First, download the latest source package, uncompress it and enter its root directory; Then, run the following two commands:

```bash
make
sudo make install
```

Include this library in your program `#include <mosquitto.h>` and compile it by

```bash
g++ producer.cpp -o producer -lmosquitto
g++ consumer.cpp -o consumer -lmosquitto
```

See the full documentation of this library at <a href="https://mosquitto.org/documentation/" target="_blank">https://mosquitto.org/documentation/</a>.

### Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> allows you to specify vhost along with username. See *Vhost specification* section for the detail.  
Remember to `mosquitto_lib_init();` before creating the mosquitto instance.  
Many MQTT libraries, including this one, require network looping to complete and maintain the connection with broker. There could be several loop functions for you to choose. If none of them are called, incoming network data will not be processed and outgoing network data may not be sent in a timely fashion. Using this library, you usually starts loop right after connecting.  
The second parameter of `mosquitto_new()` function is boolean flag for clean session.  
The fourth parameter of `mosquitto_connect()` function is keep alive time in seconds. Set keep alive to 60 seconds, so that client will confirm the connectivity with broker.  

```cpp
string vhusn = vhost + ":" + usn;
const char *username = vhusn.c_str();

struct mosquitto *mosq = NULL;

mosquitto_lib_init();
mosq = mosquitto_new(NULL, true, NULL);
mosquitto_username_pw_set(mosq,	username, password);	 
mosquitto_connect(mosq, host, port, 60));
mosquitto_loop_start(mosq); 
```

After that, producer can send messages to a particular topic.  
The fourth argument is length of payload char array; The sixth argument is QoS; The seventh argument is boolean flag for retain.    

```cpp
mosquitto_publish(mosq, NULL, topic, 20, payload, 1, false);
```

At last, producer will stop loop and disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

```cpp
mosquitto_loop_stop(mosq, true); 
mosquitto_disconnect(mosq);
mosquitto_destroy(mosq);
mosquitto_lib_cleanup();
```

### Consumer
The first step is the same as producer, consumer needs to connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker and start loop. Not as the producer, this consumer loops forever.  

```cpp
while(!mosquitto_loop_forever(mosq, 0, 1)){
} 
```

Then you need to set some callback functions. They play an significant role when using this library. Callback on receiving message is indispensable.  

```cpp
void onMessage(struct mosquitto *mosq, void *userdata, const struct mosquitto_message *message) {
	if(message->payloadlen) {
		printf("Topic: %s, Message: %s\n", (char*)message->topic, (char*)message->payload);
	} else {
		printf("Topic: %s, Message: (null)\n", message->topic);
	}
	fflush(stdout);
}

mosquitto_message_callback_set(mosq, onMessage);
```

Finally, you need to subscribe a topic, so that consumer knows where to listen to. Once it receives a message from the queue bound by the topic, it will call `onMessage()` function to print the topic and message payload.  

```cpp
mosquitto_subscribe(mosq, NULL, topic, 1);
```

When you no longer need it, you can also unsubscribe a topic.

```cpp
mosquitto_unsubscribe(mosq, NULL, topic);
```

### Putting it together

**producer.cpp**

```cpp
#include <stdio.h>
#include <iostream>
#include <mosquitto.h>
#include <exception>
#include <stdlib.h>
#include <unistd.h>
	
using namespace std;
	
//The library automatically reconnects to broker
	
string hst = "hostname";
const char *host = hst.c_str();
int port = 1883;
string vhost = "yourvhost";
string usn = "username";
string vhusn = vhost + ":" + usn;
const char *username = vhusn.c_str();
string pwd = "password";
const char *password = pwd.c_str();
string tpc = "test/any";
const char *topic = tpc.c_str();
	
/**
 * This is the main method which creates and runs producer instance.
 * @Looping is essential for this MQTT library to work.
 * @Exceptions on connection and publish error.
 */
int main(int argc, char *argv[]) {
	int keepalive = 60;
	bool clean_session = true;
	struct mosquitto *mosq = NULL;
	
	//create producer and connect to broker
	mosquitto_lib_init();
	mosq = mosquitto_new(NULL, clean_session, NULL);
	mosquitto_username_pw_set(mosq,	username, password);
	if(mosquitto_connect(mosq, host, port, keepalive)) {
		printf("Error: Failed to connect\n");
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
			printf("Error: Failed to publish message\n%s\n", e.what());
			return 1;
		}
		sleep(1);
	}
	
	//stop producer
	mosquitto_loop_stop(mosq, true); 
	mosquitto_disconnect(mosq);
	mosquitto_destroy(mosq);
	mosquitto_lib_cleanup();
	return 0;
}
```

**consumer.cpp**

```cpp
#include <stdio.h>
#include <iostream>
#include <mosquitto.h>
#include <exception>
#include <stdlib.h>
#include <unistd.h>
	
using namespace std;
	
//The library automatically reconnects to broker
	
string hst = "hostname";
const char *host = hst.c_str();
int port = 1883;
string vhost = "yourvhost";
string usn = "username";
string vhusn = vhost + ":" + usn;
const char *username = vhusn.c_str();
string pwd = "passwrod";
const char *password = pwd.c_str();
string tpc = "test/#";
const char *topic = tpc.c_str();
	
/**
 * This method is the callback on connecting broker. 
 * @It is event-driven. You don't call it in your code.
 * @It subscribes the specific topic.
 * @There're other callback functions provided by this library.
 */
void onConnect(struct mosquitto *mosq, void *userdata, int result) {
	if (!result) {
		try {
			mosquitto_subscribe(mosq, NULL, topic, 1); 
		} catch (exception& e) {
			printf("Error: Failed to subscribe\n%s\n", e.what());
		}
	} else {
		printf("Error: Failed to connect\n");
	}
}
	
/**
 * This method is the callback on receiving messages. 
 * @It is event-driven. You don't call it in your code.
 * @It prints the message topic and payload on console.
 * @There're other callback functions provided by this library.
 */
void onMessage(struct mosquitto *mosq, void *userdata, const struct mosquitto_message *message) {
	if(message->payloadlen) {
		printf("Topic: %s, Message: %s\n", (char*)message->topic, (char*)message->payload);
	} else {
		printf("Topic: %s, Message: (null)\n", message->topic);
	}
	fflush(stdout);
}
	
/**
 * This is the main method which creates and sets consumer instance.
 * @Looping is essential for this MQTT library to work.
 * @Exceptions on connection and subscription error.
 */
int main(int argc, char *argv[]) {
	int keepalive = 60;
	bool clean_session = true;
	struct mosquitto *mosq = NULL;
	mosquitto_lib_init();
	mosq = mosquitto_new(NULL, clean_session, NULL);
	mosquitto_username_pw_set(mosq,	username, password);	 
	mosquitto_connect_callback_set(mosq, onConnect);
	mosquitto_message_callback_set(mosq, onMessage);
	mosquitto_connect(mosq, host, port, keepalive);
	//looping is essential for consumer to work
	while(!mosquitto_loop_forever(mosq, 0, 1)){
	}
	return 0;
}
```
