# Introduction

> Before reading this chapter, we assume that you already have the basic concepts of message queue, e.g broker, exchange, queue, producer, consumer, etc. Knowing AMQP protocol would very much facilitate understanding STOMP.  

[robomq.io](http://www.robomq.io) supports STOMP 1.0, STOMP 1.1 and STOMP 1.2 as an extension to the AMQP broker. Its port is **61613**, SSL port is **61614**.  

STOMP is the Simple (or Streaming) Text Orientated Messaging Protocol. It is much simpler than AMQP and so more handy for message queue novices. STOMP provides an interoperable wire format so that STOMP clients can communicate with any STOMP message broker to provide easy and widespread messaging interoperability among many languages, platforms and brokers. We would recommend STOMP if you are implementing a simple message queuing application without very complex demands on combination of exchanges and queues.  
> [Full documentation of STOMP](http://stomp.github.io/)

The STOMP specification does not prescribe what kinds of destinations a broker must support, instead the value of the destination header in SEND and MESSAGE frames is broker-specific. Therefore, [robomq.io](http://www.robomq.io) enriches STOMP with more destination types so it is now capable of most basic jobs AMQP can do.  

# Message destinations

[robomq.io](http://www.robomq.io) gives its STOMP adapter the flexibility to support the destination types as bellow:

* /exchange -- SEND to arbitrary routing keys and SUBSCRIBE to arbitrary binding patterns; 
* /queue -- SEND and SUBSCRIBE to queues managed by the STOMP gateway; 
* /amq/queue -- SEND and SUBSCRIBE to queues created outside the STOMP gateway; 
* /topic -- SEND and SUBSCRIBE to transient and durable topics; 
* /temp-queue/ -- create temporary queues (in reply-to headers only). 

> See more explanation regarding this topic at <https://www.rabbitmq.com/stomp.html>

Thus, with STOMP, you can easily implement messaging clients in one-on-one, broadcast, routing key, routing filter or request-reply scenario by just specifying different types of destination. In the rest of this section, we are going to discuss how to switch among those scenarios with minimal change of code. Most times, it only needs to change one line.  

> To know more about the differences among those scenarios, read first paragraph of the previous five pages introducing AMQP implementation of those scenarios.  

**One-to-One**  

This scenario is the most basic application of STOMP. If your destination in subscribe and send functions is in the format of `/queue/queueName` or `/amq/queue/queueName`, the consumers will receive the messages in a round-robin manner because this type of destination is mapped into exchange.default.queueName on [robomq.io](http://www.robomq.io) broker.  

The default exchange has one special property that makes it very useful for simple applications: every queue that is created is automatically bound to it with a routing key which is the same as the queue name.  Therefore, no matter how many consumers subscribe a same queueName, there will be only one queue created. Its name and routing key are both the queueName, and all consumers have subscribed it will receive messages from the queue in turn.  

`/queue/queueName` and `/amq/queue/queueName` behave almost the same. The only difference is that the former one is manged by the STOMP gateway, while the latter one is created outside the STOMP gateway.  

> All example programs on this page are implemented for one-on-one scenario, but you will learn how to transform it into other scenarios quickly.  

**Broadcast**  

If your destination in subscribe and send functions is `/exchange/amq.fanout`, all the consumers will receive every message at the same time because this type of destination is mapped into exchange.fanout on [robomq.io](http://www.robomq.io) broker.  

A fanout exchange routes messages to all of the queues that are bound to it and the routing key is ignored. In this case, each consumer will have its own queue. The queue names are auto-generated and they all are bound to the fanout exchange.  

**Routing key**  

If your destination in subscribe and send functions is `/exchange/amq.direct/routingKey`,   messages will be broadcast to all queues bound to the direct exchange with that routingKey and consumers subscribing those queues will receive every message at the same time because this type of destination is mapped into exchange.direct.routingKey on [robomq.io](http://www.robomq.io) broker.  

The way direct exchange works is as bellow:    
1. A queue binds to the exchange with a routing key K;  
2. When a new message with routing key R arrives at the direct exchange, the exchange routes it to the queue if K = R.

In this case, each consumer will have its own queue. The queue names are auto-generated and they are bound to the direct exchange by their particular routing keys.  

**Routing filter (Topic)**  

You can implement the topic scenario by providing a destination started by `/topic/` or `/exchange/amq.topic/`. The essential difference between normal routing key and topic is that consumer can subscribe a topic with wild cards inside. In AMQP protocol, a message sent with a particular routing key will be delivered to all the queues that are bound with a matching binding key with or without wild cards.  

i.e. In STOMP, if your destination in send function is `/topic/routingKey` or `/exchange/amq.topic/routingKey` and in subscribe function is `/topic/routingPattern` or `/exchange/amq.topic/routingPattern`, messages will be delivered to all queues bound to the topic exchange with the routingPattern which matches the routingKey in send destination because this type of destination is mapped into exchange.topic.routingPattern on [robomq.io](http://www.robomq.io) broker.  

There are 2 wild cards available as bellow:  

* \* (star) can substitute for exactly one word.  
* \# (hash) can substitute for zero or more words.   

For instance, publish key `a.b.c` matches subscribe key `a.b.*` or `a.#`, but doesn't match `a.*`.  
Specially, you can implement broadcast scenario by subscribing `/topic/#`, implement routing key scenario by making routingKey in subscribe function the same as routingPattern in send function.  

**Request reply**  

You can implement request-reply scenario with any destination type. In this case, all clients are both producer and consumer.  
One thing requester needs to do is adding a "reply-to" header to the message. The value of "reply-to" header will be the subscribing destination of requester. When replier receives a message, it will handle the message and send reply to the destination in "reply-to" header.  
STOMP protocol itself doesn't define "reply-to" header, but [robomq.io](http://www.robomq.io) allows you to define any extra header by yourself.  

**More scenarios**  

The scenarios you can implement with [robomq.io](http://www.robomq.io) STOMP adapter are more than the five ones above.  

For example, if you use destination type `/temp-queue/routingKey`, it will creates transient queues bound to the direct exchange. A transient queue will be automatically deleted once it receives a message. It can be used to implement RPC (remote procedure call), a variant of request-reply scenario. In RPC scenario, requester creates a transient queue to listen for reply as it sends a request. The queue will be automatically deleted once it receives the reply.  

You can also add your own exchanges in your vhost and incorporate them in STOMP destination, such as `/exchange/user-added-exchange/routingKey`. It will create an auto-named queue bound to user-added-exchange by the routingKey. This feature significantly extends [robomq.io](http://www.robomq.io) STOMP adapter's capacity.  

Although we have talked so much about how our STOMP message destinations are lightweight but powerful, there's still things it can't do. For example, if you want to bind one queue with a non-default exchange and let multiple consumers subscribe the queue, you would have to ask for help from the AMQP protocol.  

# STOMP use cases

We will provide examples of one-to-one scenario in five languages, including Python, Node.js, PHP, Java and C.  

In the examples, STOMP producer will first ask user for the quantity of messages, then publish the certain number of test messages to a particular destination through STOMP broker. STOMP consumer will subscribe the same destination and print the message body as it receives messages.  

All examples have implemented automatic reconnecting, which is crucial in real production.  

The example code provided bellow could be the short version, it might have omitted some advanced details. For full version code, please go to our SDK [repository](https://github.com/robomq/robomq.io/tree/master/sdk/STOMP) on GitHub. 

Follow the *Message destinations* section and you will be able to switch it to other scenario by changing only the destination argument.  

> Before testing the example code, replace hostname, yourvhost, username and password with the real variables in your network environment.  
> Always run consumer first to create the exchange and queue for producer to send messages to.   

## Python

### Prerequisite
The Python library we use for this example can be found at <https://pypi.python.org/pypi/stompest/>. Its GitHub repository is at <https://github.com/nikipore/stompest>.  
It supports STOMP version 1.0, 1.1 and 1.2.  

You can install it through `sudo pip install stompest`.  

Finally, import this library in your program.

```python
from stompest.config import StompConfig
from stompest.protocol import StompSpec
from stompest.sync import Stomp
```

The full documentation of this library is at <http://nikipore.github.io/stompest/>.

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
> In STOMP, username is called login and password is called passcode.  

Set the outgoing heartbeat to 60000 milliseconds, so that client will confirm the connectivity with broker; but disable the incoming heartbeat because [robomq.io](http://www.robomq.io) broker won't send heartbeat to client.  
> Notice that stompest library reverses the order of outgoing and incoming heartbeats.  

```python
client = Stomp(StompConfig("tcp://" + server + ":" + port, login = login, passcode = passcode, version = "1.2"))
client.connect(versions = ["1.2"], host = vhost, heartBeats = (0, 60000))
```

After that, producer can send messages to a particular destination. In this example, it is a queue bound to the default exchange, but it can be replaced by other types of destinations to perform the corresponding messaging. The *Message destinations* section elaborates it.  

```python
client.send(destination, body = message, headers = {"content-type": "text/plain"}, receipt = None)
```

At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker.

```python
client.disconnect()
```

### Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Next step is to subscribe a destination, so that consumer knows where to listen to. Once it receives a message from the destination, it will print the message body.  
If you set `"ack": "auto"`, you don't need `client.ack(frame)`.  
The `"id"` must be different for multiple subscriptions because `client.receiveFrame()` receives messages from any subscription and client needs to distinguish them by subscription ID.  

```python
subscription = client.subscribe(destination, {"ack": "client", "id": "0"})
	
while True:
	frame = client.receiveFrame()
	print frame.body
	client.ack(frame)
```

When you no longer need it, you can also unsubscribe a destination with its unique token.

```python
client.unsubscribe(subscription)
```

### Putting it together

**producer.py**

```python
import time
from stompest.config import StompConfig
from stompest.sync import Stomp
	
server = "hostname"
port = "61613"
vhost = "yourvhost"
login = "username"
passcode = "password"
destination = "/queue/test"	#There're more options other than /queue/...
	
try:
	client = Stomp(StompConfig("tcp://" + server + ":" + port, login = login, passcode = passcode, version = "1.2"))
	client.connect(versions = ["1.2"], host = vhost, heartBeats = (0, 60000))	#CONNECT
	msgNum = int(input("Quantity of test messages: "))
	for i in range(msgNum):	
		message = "test msg " + str(i + 1)
		client.send(destination, body = message, headers = {"content-type": "text/plain"}, receipt = None)	#SEND
		time.sleep(1)	
	client.disconnect()	#DISCONNECT
except Exception, e:
	print e
```

**consumer.py**
	
```python
import time
from stompest.config import StompConfig
from stompest.sync import Stomp
	
server = "hostname"
port = "61613"
vhost = "yourvhost"
login = "username"
passcode = "password"
destination = "/queue/test"	#There're more options other than /queue/...
	
while True:
	try:
		client = Stomp(StompConfig("tcp://" + server + ":" + port, login = login, passcode = passcode, version = "1.2"))
		client.connect(versions = ["1.2"], host = vhost, heartBeats = (0, 60000))	#CONNECT
		subscription = client.subscribe(destination, {"ack": "client", "id": "0"})	#SUBSCRIBE
		while True:
			frame = client.receiveFrame()
			try:
				print frame.body
				client.ack(frame)	#ACK
			except:
				print "Error: Can't handle message received, NACKing"
				client.nack(frame)	#NACK
	except Exception, e:
		#reconnect on exception
		print "Exception handled, reconnecting...\nDetail:\n%s" % e
		try:
			client.disconnect()
		except:
			pass
		time.sleep(5)
```

## Node.js

### Prerequisite
The Node.js library we use for this example can be found at <https://github.com/jmesnil/stomp-websocket>.  
It supports STOMP version 1.0 and 1.1.  

You can install the library through `sudo npm install stompjs`.  

Finally, require this library in your program.  

```javascript
var Stomp = require("stompjs");
```
The full documentation of this library is at <http://jmesnil.net/stomp-websocket/doc/>.

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker. 
> In STOMP, username is called login and password is called passcode.  

Set the outgoing heartbeat to 60000 milliseconds, so that client will confirm the connectivity with broker; but disable the incoming heartbeat because [robomq.io](http://www.robomq.io) broker won't send heartbeat to client.  

```javascript
var client = Stomp.overTCP(server, port);
client.heartbeat.outgoing = 60000;
client.heartbeat.incoming = 0;
client.connect(login, passcode, success_callback, fail_callback, vhost);
```

After that, producer can send messages to a particular destination. In this example, it is a queue bound to the default exchange, but it can be replaced by other types of destinations to perform the corresponding messaging. The *Message destinations* section elaborates it. 

```javascript
client.send(destination, {"content-type": "text/plain"}, message);
```

At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker.

```javascript
client.disconnect(callback);
```

### Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Next step is to subscribe a destination, so that consumer knows where to listen to. Once it receives a message from the destination, it will call the callback function to print the message body.  
If you set `ack: "auto"`, you don't need `message.ack();`.

```javascript
client.subscribe(destination, function(message) {
	console.log(message.body);
	message.ack();
},
{ack: "client"});
```

When you no longer need it, you can also unsubscribe a destination with its unique token. If so, you need to save the token when you subscribe.  

```javascript
	var subscription = client.subscribe(...);
	subscription.unsubscribe();
```

### Putting it together

**producer.js**

```javascript
var Stomp = require("stompjs");
	
var server = "hostname";
var port = 61613; //It takes either string or int argument
var login = "username";
var passcode = "password";
var vhost = "yourvhost";
var destination = "/queue/test";	//There're more options other than /queue/...
	
var client = Stomp.overTCP(server, port);
client.heartbeat.outgoing = 60000;
client.heartbeat.incoming = 0;
client.connect(login, passcode
	, function() {
		process.stdout.write("Quantity of test messages: ");
		process.stdin.on("data", function (msgNum) {
			for(var i = 1; i <= msgNum; i++){	
				var message = "test msg " + i;
				client.send(destination, {"content-type": "text/plain"}, message);
			}
			client.disconnect(function() {
				process.exit(0);
			});
		});		
	}
	//callback function of connection failure
	, function(ex) {
		console.log(ex);
		process.exit(-1);
	}
	, vhost);
```

**consumer.js**

```javascript
var Stomp = require("stompjs");
var domain = require("domain");
	
var server = "hostname";
var port = 61613; //It takes either string or int argument
var login = "username";
var passcode = "password";
var vhost = "yourvhost";
var destination = "/queue/test";	//There're more options other than /queue/...
	
//use domain module to handle reconnecting
var client = null;
var dom = domain.create();
dom.on("error", consume);
dom.run(consume);
	
function consume() {
	client = Stomp.overTCP(server, port);
	client.heartbeat.outgoing = 60000;
	client.heartbeat.incoming = 0;
	client.connect(login, passcode
		, function() {
			//the callback for subscribe() function is actually the callback on message 
			client.subscribe(destination, function(message) {
				try {
					console.log(message.body);
					message.ack();
				} catch(ex) {
					console.log("Error: Can't handle message received, NACKing");
					message.nack();
				}
			},
			{ack: "client"}); //if ack:"auto", no need to ack in code
		}
		//callback function of connection failure
		, function(ex) {
			console.log("Exception handled, reconnecting...\nDetail:\n" + ex);
			client.disconnect(function() {setTimeout(consume, 5000);});
		}
		, vhost);
}
```

## PHP

### Prerequisite
The PHP library we use for this example can be found at <http://php.net/manual/en/book.stomp.php>.  
It supports STOMP version 1.0 and 1.1.  

This library depends on OpenSSL, if you want to use STOMP over SSL. In that case, first ensure that your have OpenSSL installed.  
Download the library from <http://pecl.php.net/package/stomp> and uncompress the tarball, enter `stomp-x.x.x/` and install it by

```bash
	phpize
	./configure
	make
	sudo make install
```

Now you should see `stomp.so` in your php shared library directory, e.g `/usr/lib/php5/20121212/`. Finally, edit your `php.ini`. In *Dynamic Extensions* section, add one line `extension=stomp.so`.

You may see more installation approaches at <http://php.net/manual/en/stomp.setup.php>.  

>Notice: this library is different with php5-stomp extension, do not mix them up.

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
> In STOMP, username is called login and password is called passcode.  

Set the outgoing heartbeat to 60000 milliseconds, so that client will confirm the connectivity with broker; but disable the incoming heartbeat because [robomq.io](http://www.robomq.io) broker won't send heartbeat to client.  

```php
$client = new Stomp("tcp://".$server.":".$port, $login, $passcode, array("host" => $vhost, "accept-version" => "1.0,1.1", "heart-beat" => "60000,0"));
```

After that, producer can send messages to a particular destination. In this example, it is a queue bound to the default exchange, but it can be replaced by other types of destinations to perform the corresponding messaging. The *Message destinations* section elaborates it.  

```php
$client->send($destination, $message, array("content-type" => "text/plain"));
```
At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker. This library contains disconnect function in client class's destructor.  

```php
unset($client);
```

### Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Next step is to subscribe a destination, so that consumer knows where to listen to. Once it receives a message from the destination, it will print the message body.  
If you set `"ack"=>"auto"`, you don't need `$client->ack($frame);`.    

```php
$client->subscribe($destination, array("ack" => "client"));

while(true) {
	if ($frame = $client->readFrame()) {
		echo $frame->body.PHP_EOL;
		$client->ack($frame);
	}
}
```

When you no longer need it, you can also unsubscribe a destination.

```php
$client->unsubscribe($destination);
```

### Putting it together

**producer.php**

```php
<?php	
$server = "hostname";
$port = "61613";
$vhost = "yourvhost";
$login = "username";
$passcode = "password";
$destination = "/queue/test";	//There're more options other than /queue/...
	
try {
	$client = new Stomp("tcp://".$server.":".$port, $login, $passcode, array("host" => $vhost, "accept-version" => "1.0,1.1", "heart-beat" => "60000,0"));
	echo "Quantity of test messages: ";
	$msgNum = rtrim(fgets(STDIN), PHP_EOL);
	for ($i = 1; $i <= $msgNum; $i++) {
		$message = "test msg ".$i;
		$client->send($destination, $message, array("content-type" => "text/plain"));
		sleep(1);
	}
	unset($client);
} catch (StompException $e) {
	die($e->getMessage());
}
?>
```

**consumer.php**

```php
<?php	
$server = "hostname";
$port = "61613";
$vhost = "yourvhost";
$login = "username";
$passcode = "password";
$destination = "/queue/test";	//There're more options other than /queue/...
	
while (true) {
	try {
		$client = new Stomp("tcp://".$server.":".$port, $login, $passcode, array("host" => $vhost, "accept-version" => "1.0,1.1", "heart-beat" => "60000,0"));
		$client->subscribe($destination, array("ack" => "client")); //if "ack"=>"auto", no need to ack in code
		while (true) {
			if ($frame = $client->readFrame()) {
				try {
					echo $frame->body.PHP_EOL;
					$client->ack($frame);
				} catch (Exception $e) {
					echo "Error: Can't handle message received, NACKing";
					$client->nack($frame);
				}
			}
		}
	} catch (StompException $e) {
		echo "Exception handled, reconnecting...\nDetail:\n".$e->getMessage().PHP_EOL;
		unset($client);
		sleep(5);
	}
}
?>
```

## Ruby

### Prerequisite
The Ruby gem we use for this example can be found at <https://rubygems.org/gems/stomp>. Its GitHub repository is at <https://github.com/stompgem/stomp>.  
It supports STOMP version 1.0, 1.1 and 1.2.  

You can install it through `gem install stomp`.  

Finally, require this gem in your program.  

	require 'stomp'

The full documentation of this library is at <http://www.rubydoc.info/github/stompgem/stomp/index>.  

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
> In STOMP, username is called login and password is called passcode.  

Set the outgoing heartbeat to 60000 milliseconds, so that client will confirm the connectivity with broker; but disable the incoming heartbeat because [robomq.io](http://www.robomq.io) broker won't send heartbeat to client.  

```ruby
hash = { :hosts => [
  {:login => login, :passcode => passcode, :host => server, :port => port},
  ],
  :connect_headers => {"host" => vhost, "accept-version" => "1.2", "heart-beat" => "60000,0"}
}

connection = Stomp::Connection.new(hash)
```
After that, producer can send messages to a particular destination. In this example, it is a queue bound to the default exchange, but it can be replaced by other types of destinations to perform the corresponding messaging. The *Message destinations* section elaborates it.  

```ruby
connection.publish(destination, message, headers = {"content-type" => "text/plain"})
```
At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker.  

```ruby
connection.disconnect
```

### Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Next step is to subscribe a destination, so that consumer knows where to listen to. Once it receives a message from the destination, it will print the message body.  
If you set `"ack": "auto"`, you don't need `connection.ack(message_id)`.  
The `"id"` must be different for multiple subscriptions because `connection.receive` receives messages from any subscription and client needs to distinguish them by subscription ID.  

```ruby
subscription = connection.subscribe(destination, {"ack" => "client-individual", "id" => "0"})
while msg = connection.receive
  puts msg.body
  # ack current message
  connection.ack(msg.headers['message-id'])
end
```

When you no longer need it, you can also unsubscribe a destination with its unique token.

```ruby
connection.unsubscribe(subscription)
```

### Putting it together

**producer.rb**

```ruby
require 'stomp'

# connection options
server = "hostname"
port = "61613"
vhost = "yourvhost"
login = "username"
passcode = "password"
destination = "/queue/test"

print "Quantity of test messages: "
msgNum = gets.to_i

# stomp gem connect hash
hash = { :hosts => [
  {:login => login, :passcode => passcode, :host => server, :port => port},
  ],
  :connect_headers => {"host" => vhost, "accept-version" => "1.2", "heart-beat" => "60000,0"}
}

begin
  # connect
  connection = Stomp::Connection.new(hash)

  # send messages
  (1..msgNum).each do |counter|
    message = "test msg  #{counter}"
    connection.publish(destination, message, headers = {"content-type" => "text/plain"})
    sleep 1
  end

  # disconnect
  connection.disconnect
end
```

**consumer.rb**

```ruby
require 'stomp'

# connection options
server = "hostname"
port = "61613"
vhost = "yourvhost"
login = "username"
passcode = "password"
destination = "/queue/test"

# stomp gem connect hash
hash = { :hosts => [
  {:login => login, :passcode => passcode, :host => server, :port => port},
  ],
  :connect_headers => {"host" => vhost, "accept-version" => "1.2", "heart-beat" => "60000,0", "content-type" => "text/plain"}
}

loop do
  begin
    # connect
    connection = Stomp::Connection.new(hash)

    # subscribe
    connection.subscribe(destination, {"ack" => "client-individual", "id" => "0"})
    while msg = connection.receive
      puts msg.body
      # ack current message
      connection.ack(msg.headers['message-id'])
    end
  rescue => e
    puts "Exception handled, reconnecting...\nDetail:\n#{e.message}"
    sleep 5
  end
end
```

## Java

### Prerequisite
The Java library we use for this example can be found at <https://github.com/robomq/Gozirra>.  
It supports STOMP version 1.0.  

You may clone the repository by `git clone https://github.com/robomq/Gozirra.git`.

Import this library in your program	`import net.ser1.stomp.*;` and compile your source code along with gozirra-robomq.jar. For example,  

```bash
javac -cp ".:./gozirra-robomq.jar" Producer.java Consumer.java 
```

Run the producer and consumer classes. For example,  

```bash
java -cp ".:./gozirra-robomq.jar" Consumer
java -cp ".:./gozirra-robomq.jar" Producer
```
Of course, you can eventually compress your producer and consumer classes into jar files.

> Java7+ is required to compile with this library.

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
> In STOMP, username is called login and password is called passcode.  

The library will automatically set the outgoing heartbeat to 60000 milliseconds and disable the incoming heartbeat, i.e. set it to 0.  

```java
client = new Client(server, port, login, passcode, vhost);
```
After that, producer can send messages to a particular destination.  
The third parameter of `send()` function is message headers.  
In this example, it is a queue bound to the default exchange, but it can be replaced by other types of destinations to perform the corresponding messaging. The *Message destinations* section elaborates it.  

```java
HashMap headers = new HashMap();
headers.put("content-type", "text/plain");
client.send(destination, message, headers);
```

At last, producer will disconnect with the [robomq.io](http://www.robomq.io) broker.

```java
client.disconnect();
```

### Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Next step is to subscribe a destination, so that consumer knows where to listen to. Once it receives a message from the destination, it will call the overridden function `message()` to print the message body.   

```java
client.subscribe(destination, new Listener() {
	public void message( Map headers, String body ) {
		System.out.println(body);
	}
}); 
```

When you no longer need it, you can also unsubscribe a destination.

```java
	client.unsubscribe(destination);
```

### Putting it together

**Producer.java**

```java
import net.ser1.stomp.*;
import java.util.HashMap;
import java.util.Scanner;
	
class Producer {
	private Client client;
	private String server = "hostname";
	private int port = 61613;
	private String vhost = "yourvhost";
	private String destination = "/queue/test"; //There're more options other than /queue/...
	private String login = "username";
	private String passcode = "password";
	
	private void produce() {
		try {
			client = new Client(server, port, login, passcode, vhost);
			System.out.print("Quantity of test messages: ");
			Scanner scanner = new Scanner(System.in);
			int msgNum = scanner.nextInt();
			HashMap headers = new HashMap();
			headers.put("content-type", "text/plain");
			for (int i = 0; i < msgNum; i ++) {
				String message = "test msg " + Integer.toString(i + 1);
				client.send(destination, message, null);
				Thread.sleep(1000);
			}
			client.disconnect();
		} catch(Exception e) {
			System.out.println(e);
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
import net.ser1.stomp.*;
import java.util.Map;
	
class Consumer {
	private Client client;
	private String server = "hostname";
	private int port = 61613;
	private String vhost = "yourvhost";
	private String destination = "/queue/test"; //There're more options other than /queue/...
	private String login = "username";
	private String passcode = "password";
	
	private void consume() {
		while (true) {
			try {
				client = new Client(server, port, login, passcode, vhost);
				client.subscribe(destination, new Listener() {
					/**
					 * This method is the overridden callback on receiving messages.
					 * @ It is event-driven. You don't call it in your code.
					 * @ It prints the message body on console.
					 * @ There're other callback functions provided by this library.
					 */
					public void message(Map headers, String body) {
						System.out.println(body);
					}
	  			});
				client.addErrorListener(new Listener() {
					public void message(Map header, String body) {
						System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", body);
						//after connected, disconnect on error
						try {
							client.disconnect();
						} catch(Exception e) {}
					}
				});
				while (true) {
					//after connected, reconnect on connection lost
					if (!client.isSockConnected()) {
						break;
					}
					Thread.sleep(2000); //check interval must be short enough
				}
			} catch(Exception e) {
				//when initializing connection, reconnect on exception
				System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", e); 
				try {
					Thread.sleep(5000); 
				} catch(Exception es) {}
			}
		}	
	}
	
	public static void main(String[] args) {
		Consumer c = new Consumer();
		c.consume();
	}
}
```

## C

### Prerequisite
The C library we use for this example can be found at <https://github.com/evgenido/stomp>.  
It supports STOMP version 1.0, 1.1 and 1.2.  

You may clone it by `git clone https://github.com/evgenido/stomp.git`.  
Extract the library source code from `/src/` and place it in your project directory.  

Include `/path/to/stomp.h` in your code, depending on where you place the library. For example, if your project structure is  
./producer.c  
./consumer.c  
./stomp/frame.c  
./stomp/frame.h  
./stomp/hdr.c  
./stomp/hdr.h  
./stomp/stomp.c  
./stomp/stomp.h  
Include this library in your program, for example `#include "./stomp/stomp.h"` and compile it by

```bash
gcc -o producer producer.c stomp/*
gcc -o consumer consumer.c stomp/*
```

### Producer
The first thing we need to do is to establish a connection with [robomq.io](http://www.robomq.io) broker.  
Using this library, you always construct the headers before sending a STOMP frame.  
>In STOMP, username is called login and password is called passcode.  

Set the outgoing heartbeat to 60000 milliseconds, so that client will confirm the connectivity with broker; but disable the incoming heartbeat because [robomq.io](http://www.robomq.io) broker won't send heartbeat to client.  

```c
struct ctx client;
stomp_session_t *session;

session = stomp_session_new(&client);

struct stomp_hdr conn_hdrs[] = {
	{"login", login},
	{"passcode", passcode},
    {"vhost", vhost},
	{"accept-version", "1.0,1.1,1.2"},
	{"heart-beat", "60000,0"},
};

err = stomp_connect(session, server, port, sizeof(conn_hdrs)/sizeof(struct stomp_hdr), conn_hdrs);
```

After that, producer can send messages to a particular destination. In this example, it is a queue bound to the default exchange, but it cae an be replaced by other types of destinations to perform the corresponding messaging. The *Message destinations* section elaborates it.  
Notice that length of the message char array, content-length in headers and last argument of `stomp_send()` must be identical.  

```c
char body[20] = "test message";
struct stomp_hdr send_hdrs[] = {
	{"destination", destination},
	{"content-type", "text/plain"},
	{"content-length", "20"},
};

err = stomp_send(session, sizeof(send_hdrs)/sizeof(struct stomp_hdr), send_hdrs, body, 20);
```
When all messages have been sent, producer will disconnect with the [robomq.io](http://www.robomq.io) broker. This example just force disconnect, but you could use receipt attribute in headers to gracefully disconnect.

```c
struct stomp_hdr disconn_hdrs[] = {
};
err = stomp_disconnect(session, sizeof(disconn_hdrs)/sizeof(struct stomp_hdr), disconn_hdrs);
```

Finally, to start running the whole process above, you have to call `stomp_run()` before the end of your program. The process won't stop until `stomp_disconnect()` is called.  
>This is a special feature of this C library, most STOMP libraries don't need it.  

```c
err = stomp_run(session);
```

To cleanly close the client, you still need to free the session and exit at the very end.

```c
stomp_session_free(session);
exit(EXIT_SUCCESS);
```

### Consumer
The first step is the same as producer, consumer needs to connect to [robomq.io](http://www.robomq.io) broker.  

Then you need to set a few callback functions. They play an significant role in this library. For example, callback on message and error.  

```c
static void _message(stomp_session_t *s, void *ctx, void *session_ctx)
{
	struct stomp_ctx_message *e = ctx;
	fprintf(stdout, "%s\n", (const char *)e->body);
}
	
static void _error(stomp_session_t *session, void *ctx, void *session_ctx)
{
	struct stomp_ctx_error *e = ctx;
	dump_hdrs(e->hdrc, e->hdrs);
	fprintf(stderr, "Exception handled, reconnecting...\nDetail:\n%s\n", (const char *)e->body);
	
	struct stomp_hdr disconn_hdrs[] = {
	};
	stomp_disconnect(session, sizeof(disconn_hdrs)/sizeof(struct stomp_hdr), disconn_hdrs);
}

stomp_callback_set(session, SCB_ERROR, _error);
stomp_callback_set(session, SCB_MESSAGE, _message);
```

Subsequently, subscribe a destination, so that consumer knows where to listen to. Once it receives a message from the destination, it will call `_message()` function to print the message body.  
If you set `"ack": "client"` in headers, you need to add `stomp_ack()` or `stomp_nack()` in `_message()` function.  
The id attribute in headers and the subscription token will be used when unsubscribe the destination.  
By the way, you can also see how to handle error using this library in the following code.  

```c
struct stomp_hdr sub_hdrs[] = {
	{"destination", destination},
	{"ack", "auto"},
	{"id", "0"},
};

err = stomp_subscribe(session, sizeof(sub_hdrs)/sizeof(struct stomp_hdr), sub_hdrs);
if (err<0) {
	perror("stomp");
	stomp_session_free(session);
}
else {
	subscription = err;
}
```

When you no longer need it, you can also unsubscribe a destination by the subscription ID and token.

```c
struct stomp_hdr unsub_hdrs[] = {
	{"id", "0"},
};

err = stomp_unsubscribe(session, subscription, sizeof(unsub_hdrs)/sizeof(struct stomp_hdr), unsub_hdrs);
```

Finally, always remember to call `stomp_run()` at the end of your program; otherwise, nothing mentioned above will be actually executed. This functions is the driving force behind the client.  
Because this consumer example never calls `stomp_disconnect()` function, so it will be running forever after `stomp_run()`.  

### Putting it together

**producer.c**
	
```c
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
	
#include "./stomp/stomp.h" //depends on where you place the library
	
struct ctx {
	const char *destination;
};
	
/**
 * This is the method to print headers.
 */
static void dump_hdrs(int hdrc, const struct stomp_hdr *hdrs)
{
	int i;
	for (i=0; i < hdrc; i++) {
		fprintf(stdout, "%s:%s\n", hdrs[i].key, hdrs[i].val);
	}
}
	
/**
 * This is the callback method on error.
 * @It prints the error information.
 */
static void _error(stomp_session_t *session, void *ctx, void *session_ctx)
{
	struct stomp_ctx_error *e = ctx;
	dump_hdrs(e->hdrc, e->hdrs);
	fprintf(stderr, "%s\n", (const char *)e->body);
}
	
/**
 * This is the main method which creates and runs producer instance.
 * @Exceptions on connection and publish error.
 */
int main(int argc, char *argv[]) 
{
	char* server = "hostname";
	char* port = "61613";
	char* login = "username";
	char* passcode = "password";
	char* vhost = "yourvhost";
	char* destination = "/queue/test"; //There're more options other than /queue/...
	int err;
	struct ctx client;
	stomp_session_t *session;
	
	session = stomp_session_new(&client);
	if (!session) {
		perror("stomp");
		exit(EXIT_FAILURE);
	}
	
	stomp_callback_set(session, SCB_ERROR, _error);
	
	struct stomp_hdr conn_hdrs[] = {
		{"login", login},
		{"passcode", passcode},
        {"vhost", vhost},
		{"accept-version", "1.0,1.1,1.2"},
		{"heart-beat", "60000,0"},
	};
	
	err = stomp_connect(session, server, port, sizeof(conn_hdrs)/sizeof(struct stomp_hdr), conn_hdrs);
	if (err) {
		perror("stomp");
		stomp_session_free(session);
		exit(EXIT_FAILURE);
	}
	
	struct stomp_hdr send_hdrs[] = {
		{"destination", destination},
		{"content-type", "text/plain"},
		{"content-length", "20"},
	};
	int msgNum, i;
	char body[20];
	printf("Quantity of test messages: ");
	scanf("%d", &msgNum);
	for(i = 1; i <= msgNum; i++) {
		sprintf(body, "test msg %d", i);
		do {	//in case sending failed, keep retrying
			err = stomp_send(session, sizeof(send_hdrs)/sizeof(struct stomp_hdr), send_hdrs, body, 20);
			sleep(1);
		} while(err);
	}
	
	struct stomp_hdr disconn_hdrs[] = {
	};	//could use receipt to gracefully disconnect
	err = stomp_disconnect(session, sizeof(disconn_hdrs)/sizeof(struct stomp_hdr), disconn_hdrs);
	if (err) {
		perror("stomp");
		stomp_session_free(session);
		exit(EXIT_FAILURE);
	}
	
	err = stomp_run(session);	//necessary to actually run the process, stop when stomp_disconnect() called
	if (err) {
		perror("stomp");
		stomp_session_free(session);
		exit(EXIT_FAILURE);
	}
	
	stomp_session_free(session);
	exit(EXIT_SUCCESS);
	
	return 0;
}
```

**consumer.c**

```c
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
	
#include "./stomp/stomp.h" //depends on where you place the library
	
struct ctx {
	const char *destination;
};
	
/**
 * This is the method to print headers.
 */
static void dump_hdrs(int hdrc, const struct stomp_hdr *hdrs)
{
	int i;
	for (i=0; i < hdrc; i++) {
		fprintf(stdout, "%s:%s\n", hdrs[i].key, hdrs[i].val);
	}
}
	
/**
 * This is the callback method on receiving message.
 * @It prints the message body.
 */
static void _message(stomp_session_t *s, void *ctx, void *session_ctx)
{
	struct stomp_ctx_message *e = ctx;
	fprintf(stdout, "%s\n", (const char *)e->body);
}
	
/**
 * This is the callback method on error.
 * @It prints the error information and disconnect.
 */
static void _error(stomp_session_t *session, void *ctx, void *session_ctx)
{
	struct stomp_ctx_error *e = ctx;
	dump_hdrs(e->hdrc, e->hdrs);
	fprintf(stderr, "Exception handled, reconnecting...\nDetail:\n%s\n", (const char *)e->body);
	
	//disconnect for clean reconnecting later
	struct stomp_hdr disconn_hdrs[] = {
	};
	stomp_disconnect(session, sizeof(disconn_hdrs)/sizeof(struct stomp_hdr), disconn_hdrs);
}
	
/**
 * This is the main method which creates and sets consumer instance.
 * @Exceptions on connection and subscription error.
 */
int main(int argc, char *argv[]) 
{
	char* server = "hostname";
	char* port = "61613";
	char* login = "username";
	char* passcode = "password";
	char* vhost = "yourvhost";
	char* destination = "/queue/test"; //There're more options other than /queue/...
	int err;
	int subscription;
	struct ctx client;
	stomp_session_t *session;
	
	while (1) {
		session = stomp_session_new(&client);
		if (!session) {
			perror("stomp");
		}
		else {
			stomp_callback_set(session, SCB_ERROR, _error);
			stomp_callback_set(session, SCB_MESSAGE, _message);
	
			struct stomp_hdr conn_hdrs[] = {
				{"login", login},
				{"passcode", passcode},
				{"vhost", vhost},
				{"accept-version", "1.0,1.1,1.2"},
				{"heart-beat", "60000,0"},
			};
	
			err = stomp_connect(session, server, port, sizeof(conn_hdrs)/sizeof(struct stomp_hdr), conn_hdrs);
			if (err) {
				perror("stomp");
				stomp_session_free(session);
			}
			else {
				struct stomp_hdr sub_hdrs[] = {
					{"destination", destination},
					{"ack", "auto"},	//could set "ack" header to "client" and manually stomp_ack() / stomp_nack()
					{"id", "0"},
				};
	
				err = stomp_subscribe(session, sizeof(sub_hdrs)/sizeof(struct stomp_hdr), sub_hdrs);
				if (err<0) {
					perror("stomp");
					stomp_session_free(session);
				}
				else {
					subscription = err;	//if success, return sub token for unsubscribing later
	
					err = stomp_run(session);	//necessary to actually run the process, stop when stomp_disconnect() called
					if (err) {
						perror("stomp");
						stomp_session_free(session);
					}
				}
			}
		}
		sleep(5);
	}
	return 0;
}
```
