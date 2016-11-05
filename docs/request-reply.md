#Request - Reply

This is a two-way message communication also using direct exchange but unlike the RPC pattern, the reply queue is bound to an exchange allowing more than one client to subscribe to and process the replies asynchronously.  In addition any service application can process a request from any client.
In this situation, both producer and consumer are capable of publishing and consuming messages.

![Diagram of Request - Reply messaging](./images/request-reply.png)

> Browse the chapter of AMQP Introduction first if you're new to AMQP.  
> Read the chapter of *Key based message routing* before reading this chapter.  

----------

## Python

###Prerequisites

**Python client AMQP library**

The Python library we use for this example can be found at <a href="https://github.com/pika/pika" target="_blank">https://github.com/pika/pika</a>.  

You can install it through `sudo pip install pika`.  

Finally, import this library in your program.

```python
import pika
```

The full documentation of this library is at <a href="https://pika.readthedocs.org/en/0.9.14/" target="_blank">https://pika.readthedocs.org/en/0.9.14/</a>.

> pika library is not thread safe. Do not use a connection or channel across threads.

###Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
Set heartbeat to 60 seconds, so that client will confirm the connectivity with broker.  

```python
credentials = pika.PlainCredentials(username, password)
connection = pika.BlockingConnection(pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, heartbeat_interval = 60))
channel = connection.channel()
```

Then producer will do what consumer does, listen on the replyQueue on its side.  

```python
channel.queue_declare(queue = replyQueue, exclusive = True, auto_delete = True)
channel.queue_bind(exchange = exchangeName, queue = replyQueue, routing_key = replyKey)
channel.basic_consume(consumer_callback = onMessage, queue = replyQueue, no_ack = True)
channel.start_consuming()
```

After that producer can publish messages to the exchange through routing key of the requestQueue on consumer side.  
The message carries a reply-to property to indicate consumer where to reply to. It's the routing key of producer's replyQueue.  

```python 
properties = pika.spec.BasicProperties(content_type = "text/plain", delivery_mode = 1, reply_to = replyKey)
channel.basic_publish(exchange = exchangeName, routing_key = requestKey, body = "Hello World!", properties = properties)
```

Once producer has received the reply, the callback function will disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

```python
def onMessage(channel, method, properties, body):
	print body
	channel.stop_consuming()
	connection.close()
```

###Consumer
The same as producer, consumer needs to first connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

Then consumer will listen on its requestQueue.  

```python
channel.exchange_declare(exchange = exchangeName, exchange_type = "direct", auto_delete = True)
channel.queue_declare(queue = requestQueue, exclusive = True, auto_delete = True)
channel.queue_bind(exchange = exchangeName, queue = requestQueue, routing_key = requestKey)
channel.basic_consume(consumer_callback = onMessage, queue = requestQueue, no_ack = False)
channel.start_consuming()
```

When requests are received, a callback function will be invoked to print the message content and reply according to the reply-to property of request message.  
This time, we have set `no_ack` to false. If reply succeeds, ACK the request message; otherwise, NACK it, so it will be re-queued.  

```python
def onMessage(channel, method, properties, body):
	print body
	try:
		replyProp = pika.BasicProperties(content_type = "text/plain", delivery_mode = 1)
		channel.basic_publish(exchange = exchangeName, routing_key = properties.reply_to, properties = replyProp, body = "Reply to %s" % (body))
		channel.basic_ack(delivery_tag = method.delivery_tag)
	except:
		channel.basic_nack(delivery_tag = method.delivery_tag)
```

###Putting it all together

**producer.py**

```python 
import pika
import thread
import time
	
server = "hostname"
port = 5672
vhost = "yourvhost"
username = "username"
password = "password"
exchangeName = "testEx"
replyQueue = "replyQ"
requestKey = "request"
replyKey = "reply"
	
#callback funtion on receiving reply messages
def onMessage(channel, method, properties, body):
	print body
	#close connection once receives the reply
	channel.stop_consuming()
	connection.close()
	
#listen for reply messages
def listen():
	channel.queue_declare(queue = replyQueue, exclusive = True, auto_delete = True)
	channel.queue_bind(exchange = exchangeName, queue = replyQueue, routing_key = replyKey)
	channel.basic_consume(consumer_callback = onMessage, queue = replyQueue, no_ack = True)
	channel.start_consuming()
	
try:
	#connect
	credentials = pika.PlainCredentials(username, password)
	connection = pika.BlockingConnection(pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, heartbeat_interval = 60))
	channel = connection.channel()
	
	thread.start_new_thread(listen, ())
	time.sleep(1) #give time for it to start consuming
	
	#send request message
	properties = pika.spec.BasicProperties(content_type = "text/plain", delivery_mode = 1, reply_to = replyKey)
	channel.basic_publish(exchange = exchangeName, routing_key = requestKey, body = "Hello World!", properties = properties)
	
	#block until receives reply message
	while connection.is_open:
		pass
except Exception, e:
	print e
```
**consumer.py**

```python
import pika
import time
	
server = "hostname"
port = 5672
vhost = "yourvhost"
username = "username"
password = "password"
exchangeName = "testEx"
requestQueue = "requestQ"
requestKey = "request"
	
#callback funtion on receiving request messages, reply to the reply_to header
def onMessage(channel, method, properties, body):
	print body
	try:
		replyProp = pika.BasicProperties(content_type = "text/plain", delivery_mode = 1)
		channel.basic_publish(exchange = exchangeName, routing_key = properties.reply_to, properties = replyProp, body = "Reply to %s" % (body))
		channel.basic_ack(delivery_tag = method.delivery_tag)
	except:
		channel.basic_nack(delivery_tag = method.delivery_tag)
		
while True:
	try:
		#connect
		credentials = pika.PlainCredentials(username, password)
		connection = pika.BlockingConnection(pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, heartbeat_interval = 60))
		channel = connection.channel()
	
		#declare exchange and queue, bind them and consume messages
		channel.exchange_declare(exchange = exchangeName, exchange_type = "direct", auto_delete = True)
		channel.queue_declare(queue = requestQueue, exclusive = True, auto_delete = True)
		channel.queue_bind(exchange = exchangeName, queue = requestQueue, routing_key = requestKey)
		channel.basic_consume(consumer_callback = onMessage, queue = requestQueue, no_ack = False)
		channel.start_consuming()
	except Exception, e:
		#reconnect on exception
		print "Exception handled, reconnecting...\nDetail:\n%s" % e
		try:
			connection.close()
		except:
			pass
		time.sleep(5)
```

## Node.js

###Prerequisites

**Node.js client AMQP library**

The Node.js library we use for this example can be found at <a href="https://github.com/squaremo/amqp.node" target="_blank">https://github.com/squaremo/amqp.node</a>.    

You can install the library through `sudo npm install amqplib`.  

Finally, require this library in your program.

```javascript
var amqp = require("amqplib");
```

The full documentation of this library is at <a href="https://www.squaremobius.net/amqp.node/doc/channel_api.html" target="_blank">https://www.squaremobius.net/amqp.node/doc/channel_api.html</a>.

###Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
Set heartbeat to 60 seconds, so that client will confirm the connectivity with broker.  

```javascript
producer = amqp.connect("amqp://" + username + ":" + password + "@" + server + ":" + port + "/" + vhost + "?heartbeat=60");
producer.then(function(conn) {
	return conn.createConfirmChannel().then(successCallback);
}).then(null, failureCallback);
```

Then producer will do what consumer does, listen on the replyQueue on its side.  
Once producer has received the reply, it will disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

```javascript
ch.assertQueue(replyQueue, {durable: false, autoDelete: true, exclusive: true});
ch.bindQueue(replyQueue, exchangeName, replyKey);
ch.consume(replyQueue, function(message) {
	console.log(message.content.toString());
	conn.close();
}, {noAck: true});
```

After that producer can publish messages to the exchange through routing key of the requestQueue on consumer side.  
The message carries a reply-to property to indicate consumer where to reply to. It's the routing key of producer's replyQueue.  

```javascript
ch.publish(exchangeName, requestKey, content = new Buffer("Hello World!"), options = {contentType: "text/plain", deliveryMode: 1, replyTo: replyKey}, callback);
```

###Consumer
The same as producer, consumer needs to first connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

Then consumer will listen on its requestQueue.  
When requests are received, a callback function will be invoked to print the message content and reply according to the reply-to property of request message.  
This time, we have set `noAck` to false. If reply succeeds, ACK the request message; otherwise, NACK it, so it will be re-queued.  

```javascript
ch.assertExchange(exchangeName, "direct", {durable: false, autoDelete: true});
ch.assertQueue(requestQueue, {durable: false, autoDelete: true, exclusive: true});
ch.bindQueue(requestQueue, exchangeName, requestKey);
ch.consume(requestQueue, function(message) {
	console.log(message.content.toString());
	ch.publish(exchangeName, message.properties.replyTo, new Buffer("Reply to " + message.content.toString()), options = {contentType: "text/plain", deliveryMode: 1}, function(err, ok) {
		if (err != null) {
			ch.nack(message);
		}
		else {
			ch.ack(message);
		}
	});
}, {noAck: false});
```

###Putting it all together

**producer.js**

```javascript
var amqp = require("amqplib");
	
var server = "hostname";
var port = "5672";
var vhost = "yourvhost"; //for "/" vhost, use "%2f" instead
var username = "username";
var password = "password";
var exchangeName = "testEx";
var replyQueue = "replyQ";
var requestKey = "request";
var replyKey = "reply";
	
producer = amqp.connect("amqp://" + username + ":" + password + "@" + server + ":" + port + "/" + vhost + "?heartbeat=60");
producer.then(function(conn) {
	return conn.createChannel().then(function(ch) {
		//listen for reply messages
		ch.assertQueue(replyQueue, {durable: false, autoDelete: true, exclusive: true});
		ch.bindQueue(replyQueue, exchangeName, replyKey);
		ch.consume(replyQueue, function(message) {
			//callback funtion on receiving reply messages
			console.log(message.content.toString());
			//close connection once receives the reply
			conn.close();
		}, {noAck: true});
		//send the request message after 1 second
		setTimeout(function() {
			ch.publish(exchangeName, requestKey, content = new Buffer("Hello World!"), options = {contentType: "text/plain", deliveryMode: 1, replyTo: replyKey}, function(err, ok) {
				if (err != null) {
					console.error("Error: failed to send message\n" + err);
				}
			});
		}, 1000);
	});
}).then(null, function(err) {
	console.error(err);
});
```

**consumer.js**

```javascript
var amqp = require("amqplib");
var domain = require("domain");
	
var server = "hostname";
var port = "5672";
var vhost = "yourvhost"; //for "/" vhost, use "%2f" instead
var username = "username";
var password = "password";
var exchangeName = "testEx";
var requestQueue = "requestQ";
var requestKey = "request";
	
//use domain module to handle reconnecting
var consumer = null;
var dom = domain.create();
dom.on("error", relisten);
dom.run(listen);
	
function listen() {
	consumer = amqp.connect("amqp://" + username + ":" + password + "@" + server + ":" + port + "/" + vhost + "?heartbeat=60");
	consumer.then(function(conn) {
		return conn.createChannel().then(function(ch) {
			ch.assertExchange(exchangeName, "direct", {durable: false, autoDelete: true});
			ch.assertQueue(requestQueue, {durable: false, autoDelete: true, exclusive: true});
			ch.bindQueue(requestQueue, exchangeName, requestKey);
			ch.consume(requestQueue, function(message) {
				//callback funtion on receiving messages, reply to the reply_to header
				console.log(message.content.toString());
				ch.publish(exchangeName, message.properties.replyTo, new Buffer("Reply to " + message.content.toString()), options = {contentType: "text/plain", deliveryMode: 1}, function(err, ok) {
					if (err != null) {
						ch.nack(message);
					}
					else {
						ch.ack(message);
					}
				});
			}, {noAck: false});
		});
	}).then(null, function(err) {
		console.error("Exception handled, reconnecting...\nDetail:\n" + err);
		setTimeout(listen, 5000);
	});
}
	
function relisten() {
	consumer.then(function(conn) {
		conn.close();
	});	
	setTimeout(listen, 5000);
}
```

## PHP

### Prerequisite

**PHP client AMQP library**

The PHP library we use for this example can be found at <a href="https://github.com/videlalvaro/php-amqplib" target="_blank">https://github.com/videlalvaro/php-amqplib</a>.  

It uses composer to install in a few steps.  

1. Add a `composer.json` file to your project:

```json
{
	"require": {
		"videlalvaro/php-amqplib": "2.2.*"
	}
}
```

2. Download the latest composer in the same path:

```bash
	curl -sS https://getcomposer.org/installer | php
```

3. Install the library through composer:

```bash
./composer.phar install
```

Finally, require this library in your program and use the classes.

```php
require_once __DIR__ . '/../vendor/autoload.php'; //directory of library folder
use PhpAmqpLib\Connection\AMQPConnection;
use PhpAmqpLib\Message\AMQPMessage;
```

###Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
Set heartbeat to 60 seconds, so that client will confirm the connectivity with broker.  

```php
$connection = new AMQPConnection($server, $port, $username, $password, $vhost, $heartbeat = 60);
$channel =  $connection->channel();	
```

Then producer will do what consumer does, listen on the replyQueue on its side.  

```php
$channel->queue_declare($replyQueue, false, false, $exclusive = true, $auto_delete = true);
$channel->queue_bind($replyQueue, $exchangeName, $replyKey);
$consumerTag = $channel->basic_consume($replyQueue, "", false, $no_ack = true, false, false, $callback = $onMessage);
```

After that producer can publish messages to the exchange through routing key of the requestQueue on consumer side.  
The message carries a reply-to property to indicate consumer where to reply to. It's the routing key of producer's replyQueue.  

```php 
$message = new AMQPMessage("Hello World!", array("content_type" => "text/plain", "delivery_mode" => 1, "reply_to" => $replyKey));
$channel->basic_publish($message, $exchangeName, $requestKey);
```

Once producer has received the reply, the callback function will disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

```php
$onMessage = function ($message) {
	echo $message->body.PHP_EOL;
	$channel->basic_cancel($consumerTag);
};
```

###Consumer
The same as producer, consumer needs to first connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

Then consumer will listen on its requestQueue.  

```php
$channel->exchange_declare($exchangeName, $type = "direct", false, false, $auto_delete = true);
$channel->queue_declare($requestQueue, false, false, $exclusive = true, $auto_delete = true);
$channel->queue_bind($requestQueue, $exchangeName, $requestKey);
$channel->basic_consume($requestQueue, "", false, $no_ack = false, false, false, $callback = $onMessage);
```

When requests are received, a callback function will be invoked to print the message content and reply according to the reply-to property of request message.  
This time, we have set `no_ack` to false. If reply succeeds, ACK the request message; otherwise, NACK it, so it will be re-queued.  

```php
$onMessage = function ($message) {
	echo $message->body.PHP_EOL;
	try {
		$replyMessage = new AMQPMessage("Reply to ".$message->body, array("content_type" => "text/plain", "delivery_mode" => 1));
		$channel->basic_publish($replyMessage, $exchangeName, $message->get("reply_to"));
		$channel->basic_ack($message->delivery_info["delivery_tag"]);
	} catch (Exception $e) {
		$channel->basic_nack($message->delivery_info["delivery_tag"]);
	}
};
```

### Putting it together

**producer.php**

```php
<?php
require_once __DIR__ . '/../vendor/autoload.php'; //directory of library folder
use PhpAmqpLib\Connection\AMQPConnection;
use PhpAmqpLib\Message\AMQPMessage;
	
$GLOBALS["channel"] = $channel;
$GLOBALS["consumerTag"] = $consumerTag;
	
$server = "hostname";
$port = 5672;
$vhost = "yourvhost";
$username = "username";
$password = "password";
$exchangeName = "testEx";
$replyQueue = "replyQ";
$requestKey = "request";
$replyKey = "reply";
	
//callback funtion on receiving reply messages
$onMessage = function ($message) {
	echo $message->body.PHP_EOL;
	//stop consuming once receives the reply
	$GLOBALS["channel"]->basic_cancel($GLOBALS["consumerTag"]);
};
	
try {
	//connect
	$connection = new AMQPConnection($server, $port, $username, $password, $vhost, $heartbeat = 60);
	$channel =  $connection->channel();	
	
	//listen for reply messages
	$channel->queue_declare($replyQueue, false, false, $exclusive = true, $auto_delete = true);
	$channel->queue_bind($replyQueue, $exchangeName, $replyKey);
	$consumerTag = $channel->basic_consume($replyQueue, "", false, $no_ack = true, false, false, $callback = $onMessage);
	
	//send request message
	$message = new AMQPMessage("Hello World!", array("content_type" => "text/plain", "delivery_mode" => 1, "reply_to" => $replyKey));
	$channel->basic_publish($message, $exchangeName, $requestKey);
	
	//start consuming
	while(count($channel->callbacks)) {
		$channel->wait();
	}
	
	//disconnect
	$connection->close();
} catch(Exception $e) {
	echo $e.PHP_EOL;
}
?>
```

**consumer.php**

```php
<?php
require_once __DIR__."/../vendor/autoload.php"; //directory of library folder
use PhpAmqpLib\Connection\AMQPConnection;
use PhpAmqpLib\Message\AMQPMessage;
	
$GLOBALS["channel"] = $channel;
$GLOBALS["exchangeName"] = $exchangeName;
	
$server = "hostname";
$port = 5672;
$vhost = "yourvhost";
$username = "username";
$password = "password";
$exchangeName = "testEx";
$requestQueue = "requestQ";
$requestKey = "request";
	
//callback funtion on receiving request messages, reply to the reply_to header
$onMessage = function ($message) {
	echo $message->body.PHP_EOL;
	try {
		$replyMessage = new AMQPMessage("Reply to ".$message->body, array("content_type" => "text/plain", "delivery_mode" => 1));
		$GLOBALS["channel"]->basic_publish($replyMessage, $GLOBALS["exchangeName"], $message->get("reply_to"));
		$GLOBALS["channel"]->basic_ack($message->delivery_info["delivery_tag"]);
	} catch (Exception $e) {
		$GLOBALS["channel"]->basic_nack($message->delivery_info["delivery_tag"]);
	}
};
	
while (true) {
	try {
		//connect
		$connection = new AMQPConnection($server, $port, $username, $password, $vhost, $heartbeat = 60);
		$channel = $connection->channel();
	
		//declare exchange and queue, bind them and consume messages
		$channel->exchange_declare($exchangeName, $type = "direct", false, false, $auto_delete = true);
		$channel->queue_declare($requestQueue, false, false, $exclusive = true, $auto_delete = true);
		$channel->queue_bind($requestQueue, $exchangeName, $requestKey);
		$channel->basic_consume($requestQueue, "", false, $no_ack = false, false, false, $callback = $onMessage);
	
		//start consuming
		while(count($channel->callbacks)) {
			$channel->wait();
		}
	} catch(Exception $e) {
		//reconnect on exception
		echo "Exception handled, reconnecting...\nDetail:\n".$e.PHP_EOL;
		if ($connection != null) {
			try {
				$connection->close();
			} catch (Exception $e1) {}
		}
		sleep(5);
	}
}
?>
```

## Ruby

###Prerequisites

**Ruby client AMQP library**

The Ruby library we use for this example can be found at <a href="http://rubybunny.info/" target="_blank">http://rubybunny.info/</a>.  

With Ruby version >= 2.0, you can install it through `sudo gem install bunny`.  

Finally, import this library in your program.  

```ruby
require "bunny"
```

The full documentation of this library is at <a href="http://rubybunny.info/articles/guides.html" target="_blank">http://rubybunny.info/articles/guides.html</a>.

> We recommend combining the documentation with the source code of this library when you use it because some of the documentation out there is not being updated timely from our observation.  

###Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
Set heartbeat to 60 seconds, so that client will confirm the connectivity with broker.  
Although the library provides a connection property named `recover_from_connection_close`, we discourage you to use it. The reason will be explained in the Consumer section.  

```ruby
connection = Bunny.new(:host => server, :port => port, :vhost => vhost, :user => username, :pass => password, :heartbeat => 60, :recover_from_connection_close => false)
connection.start
channel = connection.create_channel
```

Then producer will do what consumer does, listen on the replyQueue on its side.  

```ruby
exchange = channel.direct(exchangeName, :auto_delete => true)
queue = channel.queue(replyQueue, :exclusive => true, :auto_delete => true)
queue.bind(exchange, :routing_key => replyKey)
isReplied = false
consumer = queue.subscribe(:block => false, :manual_ack => false) do |delivery_info, metadata, payload|
  puts payload
isReplied = true
end
```

After that producer can publish messages to the exchange through routing key of the requestQueue on consumer side.  
The message carries a reply-to property to indicate consumer where to reply to. It's the routing key of producer's replyQueue.  

```ruby 
exchange.publish("Hello World!", :routing_key => requestKey, :content_type => "text/plain", :delivery_mode => 1, :reply_to => replyKey)
```

In this example, producer is blocked until it receives the reply, then it will disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

```ruby
while !isReplied
end

cancel_ok = consumer.cancel
connection.close
```

###Consumer
The same as producer, consumer needs to first connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

Then consumer will listen on its requestQueue.  
When requests are received, a callback function will be invoked to print the message content and reply according to the reply-to property of request message.  
This time, we have set `manual_ack` to true. If reply succeeds, ACK the request message; otherwise, NACK it, so it will be re-queued.  
As we mentioned in the Producer section, `recover_from_connection_close` is set to false when connecting to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker. It matters for consumers because `recover_from_connection_close` will only recover the connection, it won't recreate exchange and queue in case they are gone. Therefore, a more robust approach is  letting your code handle reconnecting on its own and keep checking the existence of the subscribed queue.  

```ruby
exchange = channel.direct(exchangeName, :auto_delete => true)
queue = channel.queue(requestQueue, :exclusive => true, :auto_delete => true)
queue.bind(exchange, :routing_key => requestKey)
queue.subscribe(:block => false, :manual_ack => true) do |delivery_info, metadata, payload|
  puts payload
  #reply according to the reply_to header
  begin
    exchange.publish("Reply to %s" % payload, :routing_key => metadata.reply_to, :content_type => "text/plain", :delivery_mode => 1)
    channel.basic_ack(delivery_info.delivery_tag, false)
  rescue
    channel.basic_nack(delivery_info.delivery_tag, false, false)
  end
end
#keep checking the existence of the subscribed queue
while true
  raise "Lost the subscribed queue %s" % requestQueue unless connection.queue_exists?(requestQueue)
  sleep 1
end
```

###Putting it all together

**producer.rb**

```ruby
require "bunny"

server = "hostname"
port = 5672
vhost = "yourvhost"
username = "username"
password = "password"
exchangeName = "testEx"
replyQueue = "replyQ"
requestKey = "request"
replyKey = "reply"

begin
  #connect
  connection = Bunny.new(:host => server, :port => port, :vhost => vhost, :user => username, :pass => password, :heartbeat => 60, :recover_from_connection_close => false)
  connection.start
  channel = connection.create_channel

  #listen for reply message
  exchange = channel.direct(exchangeName, :auto_delete => true)
  queue = channel.queue(replyQueue, :exclusive => true, :auto_delete => true)
  queue.bind(exchange, :routing_key => replyKey)
  isReplied = false
  consumer = queue.subscribe(:block => false, :manual_ack => false) do |delivery_info, metadata, payload|
      puts payload
    isReplied = true
  end 

  #send request message
  exchange.publish("Hello World!", :routing_key => requestKey, :content_type => "text/plain", :delivery_mode => 1, :reply_to => replyKey)

  #wait until receives the reply
  while !isReplied
  end

  #close connection once receives the reply
  cancel_ok = consumer.cancel
  connection.close
rescue Exception => e
  puts e
end
```

**consumer.rb**

```ruby
require "bunny"

server = "hostname"
port = 5672
vhost = "yourvhost"
username = "username"
password = "password"
exchangeName = "testEx"
requestQueue = "requestQ"
requestKey = "request"

while true
  begin
    #connect, disable auto-reconnect so as to manually reconnect
    connection = Bunny.new(:host => server, :port => port, :vhost => vhost, :user => username, :pass => password, :heartbeat => 60, :recover_from_connection_close => false)
    connection.start
    channel = connection.create_channel

    #declare exchange and queue, bind them and consume messages
    exchange = channel.direct(exchangeName, :auto_delete => true)
    queue = channel.queue(requestQueue, :exclusive => true, :auto_delete => true)
    queue.bind(exchange, :routing_key => requestKey)
    queue.subscribe(:block => false, :manual_ack => true) do |delivery_info, metadata, payload|
      puts payload
      #reply according to the reply_to header
      begin
        exchange.publish("Reply to %s" % payload, :routing_key => metadata.reply_to, :content_type => "text/plain", :delivery_mode => 1)
        channel.basic_ack(delivery_info.delivery_tag, false)
      rescue
        channel.basic_nack(delivery_info.delivery_tag, false, false)
      end
    end
    #keep checking the existence of the subscribed queue
    while true
      raise "Lost the subscribed queue %s" % requestQueue unless connection.queue_exists?(requestQueue)
      sleep 1
    end
  rescue Exception => e
    #reconnect on exception
    puts "Exception handled, reconnecting...\nDetail:\n%s" % e
    #blindly clean old connection
    begin
      connection.close
    end
    sleep 5
  end
end
```

## Java

###Prerequisites

**Java client AMQP library**

The Java library we use for this example can be found at <a href="https://www.rabbitmq.com/java-client.html" target="_blank">https://www.rabbitmq.com/java-client.html</a>.  

Download the library jar file, then import this library in your program `import com.rabbitmq.client.*;` and compile your source code with the jar file. For example,  

```bash
javac -cp ".:./rabbitmq-client.jar" Producer.java Consumer.java 
```

Run the producer and consumer classes. For example,  

```bash
java -cp ".:./rabbitmq-client.jar" Consumer
java -cp ".:./rabbitmq-client.jar" Producer
```

Of course, you can eventually compress your producer and consumer classes into jar files.

###Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
Set heartbeat to 60 seconds, so that client will confirm the connectivity with broker.  

```java
ConnectionFactory factory = new ConnectionFactory();
factory.setHost(server);
factory.setPort(port);
factory.setVirtualHost(vhost);
factory.setUsername(username);
factory.setPassword(password);
factory.setRequestedHeartbeat(60);
connection = factory.newConnection();
channel = connection.createChannel();
```

Then producer will do what consumer does, listen on the replyQueue on its side.  

```java
String message = "Hello World!";
channel.queueDeclare(replyQueue, false, true, true, null);
channel.queueBind(replyQueue, exchangeName, replyKey, null);
QueueingConsumer qc = new QueueingConsumer(channel);
channel.basicConsume(replyQueue, true, qc);
```

After that producer can publish messages to the exchange through routing key of the requestQueue on consumer side.  
The message carries a reply-to property to indicate consumer where to reply to. It's the routing key of producer's replyQueue.  

```java 
BasicProperties properties = new BasicProperties.Builder().
		contentType("text/plain").
		deliveryMode(1).
		replyTo(replyKey).
		build();
channel.basicPublish(exchangeName, requestKey, properties, message.getBytes());
```

Once producer has received the reply, the callback function will disconnect with the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

```java
QueueingConsumer.Delivery delivery = qc.nextDelivery();
String replyMessage = new String(delivery.getBody());
System.out.println(replyMessage);

connection.close();
```

###Consumer
The same as producer, consumer needs to first connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

Then consumer will listen on its requestQueue.  

```java
channel.exchangeDeclare(exchangeName, "direct", false, true, false, null);
channel.queueDeclare(requestQueue, false, true, true, null);
channel.queueBind(requestQueue, exchangeName, requestKey, null);
QueueingConsumer qc = new QueueingConsumer(channel);
channel.basicConsume(requestQueue, false, qc);
```

When requests are received, it will print the message content and reply according to the reply-to property of request message.  
This time, we have set no-ack to false in `basicConsume()`. If reply succeeds, ACK the request message; otherwise, NACK it, so it will be re-queued.  

```java
while (true) {
	QueueingConsumer.Delivery delivery = qc.nextDelivery();
	String message = new String(delivery.getBody());
	System.out.println(message);

	//when receives messages, reply to the reply_to header
	String replyMessage = "Reply to " + message;
	BasicProperties properties = new BasicProperties.Builder().
			contentType("text/plain").
			deliveryMode(1).
			build();
	try {
		channel.basicPublish(exchangeName, delivery.getProperties().getReplyTo(), properties, replyMessage.getBytes());
		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	} catch(Exception e) {
		channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);	
	}
}
```

###Putting it all together

**Producer.java**

```java
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
	
public class Producer {
	
	private Connection connection;
	private Channel channel;
	private static String server = "hostname";
	private static int port = 5672;
	private static String vhost = "yourvhost";
	private static String username = "username";
	private static String password = "password";
	private String exchangeName = "testEx";
	private String replyQueue = "replyQ";
	private String requestKey = "request";
	private String replyKey = "reply";
	
	private void produce() {
		try {
			//connect
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(server);
			factory.setPort(port);
			factory.setVirtualHost(vhost);
			factory.setUsername(username);
			factory.setPassword(password);
			factory.setRequestedHeartbeat(60);
			connection = factory.newConnection();
			channel = connection.createChannel();
	
			//listen for reply messages
			String message = "Hello World!";
			channel.queueDeclare(replyQueue, false, true, true, null);
			channel.queueBind(replyQueue, exchangeName, replyKey, null);
			QueueingConsumer qc = new QueueingConsumer(channel);
			channel.basicConsume(replyQueue, true, qc);
	
			//send request message
			BasicProperties properties = new BasicProperties.Builder().
					contentType("text/plain").
					deliveryMode(1).
					replyTo(replyKey).
					build();
			channel.basicPublish(exchangeName, requestKey, properties, message.getBytes());
	
			//receive the reply message
			QueueingConsumer.Delivery delivery = qc.nextDelivery();
			String replyMessage = new String(delivery.getBody());
			System.out.println(replyMessage);
	
			//disconnect
			connection.close();
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
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
	
public class Consumer {
	
	private Connection connection;
	private Channel channel;
	private static String server = "hostname";
	private static int port = 5672;
	private static String vhost = "yourvhost";
	private static String username = "username";
	private static String password = "password";
	private String exchangeName = "testEx";
	private String requestQueue = "requestQ";
	private String requestKey = "request";
	
	private void consume() {
		while (true) {
			try {
				//connect
				ConnectionFactory factory = new ConnectionFactory();
				factory.setHost(server);
				factory.setPort(port);
				factory.setVirtualHost(vhost);
				factory.setUsername(username);
				factory.setPassword(password);
				factory.setRequestedHeartbeat(60);
				connection = factory.newConnection();
				channel = connection.createChannel();
			
				//declare exchange and queue, bind them and consume messages
				channel.exchangeDeclare(exchangeName, "direct", false, true, false, null);
				channel.queueDeclare(requestQueue, false, true, true, null);
				channel.queueBind(requestQueue, exchangeName, requestKey, null);
				QueueingConsumer qc = new QueueingConsumer(channel);
				channel.basicConsume(requestQueue, false, qc);
				while (true) {
					QueueingConsumer.Delivery delivery = qc.nextDelivery();
					String message = new String(delivery.getBody());
					System.out.println(message);
	
					//when receives messages, reply to the reply_to header
					String replyMessage = "Reply to " + message;
					BasicProperties properties = new BasicProperties.Builder().
							contentType("text/plain").
							deliveryMode(1).
							build();
					try {
						channel.basicPublish(exchangeName, delivery.getProperties().getReplyTo(), properties, replyMessage.getBytes());
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					} catch(Exception e) {
						channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);	
					}
				}
			} catch(Exception e) {
				//reconnect on exception
				System.out.printf("Exception handled, reconnecting...\nDetail:\n%s\n", e);
				try {
					connection.close();
				} catch (Exception e1) {}
				try {
					Thread.sleep(5000); 
				} catch(Exception e2) {}
			}
		}
	}
	
	public static void main(String[] args) {
		Consumer c = new Consumer();
		c.consume();
	}
}
```

## Go

###Prerequisites

**Go client AMQP library**

The Go library we use for this example can be found at <a href="https://github.com/streadway/amqp" target="_blank">https://github.com/streadway/amqp</a>.  

You can install it through `go get github.com/streadway/amqp`.  

Finally, import this library in your program.  

```go
import "github.com/streadway/amqp"
```

The full documentation of this library is at <a href="https://godoc.org/github.com/streadway/amqp" target="_blank">https://godoc.org/github.com/streadway/amqp</a>.  

###Producer
The first thing we need to do is to establish a connection with <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  
Set heartbeat to 60 seconds, so that client will confirm the connectivity with broker.  

```go
connection, err := amqp.DialConfig(fmt.Sprintf("amqp://%s:%s@%s:%d/%s", username, password, server, port, vhost), amqp.Config{Heartbeat: 60 * time.Second})
channel, err := connection.Channel()
```

Then producer will do what consumer does, listen on the replyQueue on its side.  

```go
queue, err := channel.QueueDeclare(replyQueue, false, true, true, false, nil)
err = channel.QueueBind(replyQueue, replyKey, exchangeName, false, nil)
messageChan, err := channel.Consume(queue.Name, "replyConsumer", true, true, false, false, nil)

message := <-messageChan
fmt.Println(string(message.Body))
```

After that producer can publish a message to the exchange through routing key of the requestQueue on consumer side.  
The message carries a reply-to property to indicate consumer where to reply to. It's the routing key of producer's replyQueue.  

```go 
err = channel.Publish(exchangeName, requestKey, false, false, amqp.Publishing{ContentType:  "text/plain", DeliveryMode: 1, ReplyTo: replyKey, Body: []byte("Hello World!")})
```

Producer should be blocked until it receives the reply before exiting.  

###Consumer
The same as producer, consumer needs to first connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker.  

Then consumer will listen on its requestQueue.  

```go
err = channel.ExchangeDeclare(exchangeName, "direct", false, true, false, false, nil)
queue, err := channel.QueueDeclare(requestQueue, false, true, true, false, nil)
err = channel.QueueBind(requestQueue, requestKey, exchangeName, false, nil)
messageChan, err := channel.Consume(queue.Name, "requestConsumer", false, true, false, false, nil)
``` 

When requests are received, it will print the message content and reply according to the reply-to property of request message.  
Note that auto-ack has been set to false above. If reply succeeds, ACK the request message; otherwise, NACK it, so it will be re-queued.  

```go
for message := range messageChan {
	fmt.Println(string(message.Body))

	err = channel.Publish(exchangeName, message.ReplyTo, false, false,
		amqp.Publishing{ContentType: "text/plain", DeliveryMode: 1, Body: append([]byte("Reply to "), message.Body...)})
	if err != nil {
		err = message.Nack(false, true)
	} else {
		err = message.Ack(false)
	}
}
```

###Putting it all together

**producer.go**

```go
package main

import (
	"fmt"
	"github.com/streadway/amqp"
	"os"
	"time"
)

var server = "hostname"
var port = 5672
var vhost = "yourvhost"
var username = "username"
var password = "password"
var exchangeName = "testEx"
var replyQueue = "replyQ"
var requestKey = "request"
var replyKey = "reply"

func main() {
	connection, err := amqp.DialConfig(fmt.Sprintf("amqp://%s:%s@%s:%d/%s", username, password, server, port, vhost),
		amqp.Config{Heartbeat: 60 * time.Second})
	if err != nil {
		fmt.Printf("Failed to connect, err: %v\n", err)
		os.Exit(1)
	}
	defer connection.Close()

	channel, err := connection.Channel()
	if err != nil {
		fmt.Printf("Failed to create channel, err: %v\n", err)
		os.Exit(1)
	}
	defer channel.Close()

	queue, err := channel.QueueDeclare(
		replyQueue, // name
		false,      // durable
		true,       // auto-delete
		true,       // exclusive
		false,      // no-wait
		nil,        // args
	)
	if err != nil {
		fmt.Printf("Failed to declare reply queue, err: %v\n", err)
		os.Exit(1)
	}

	err = channel.QueueBind(
		replyQueue,   // queue
		replyKey,     // key
		exchangeName, // exchange
		false,        // no-wait
		nil,          // args
	)
	if err != nil {
		fmt.Printf("Failed to bind reply queue with exchange, err: %v\n", err)
		os.Exit(1)
	}

	messageChan, err := channel.Consume(
		queue.Name,      // queue
		"replyConsumer", // consumer tag
		true,            // auto-ack
		true,            // exclusive
		false,           // no-local
		false,           // no-wait
		nil,             // args
	)
	if err != nil {
		fmt.Printf("Failed to consume reply messages, err: %v\n", err)
		os.Exit(1)
	}

	// use a channel to communicate between goroutines
	gotReply := make(chan bool)

	// listen for reply message
	go func(messageChan <-chan amqp.Delivery, gotReply chan bool) {
		message := <-messageChan
		fmt.Println(string(message.Body))

		// notify main goroutine it has got the reply
		gotReply <- true
	}(messageChan, gotReply)

	err = channel.Publish(
		exchangeName, // exchange
		requestKey,   // routing key
		false,        // mandatory
		false,        // immediate
		amqp.Publishing{
			ContentType:  "text/plain",
			DeliveryMode: 1,
			ReplyTo:      replyKey,
			Body:         []byte("Hello World!"),
		})
	if err != nil {
		fmt.Printf("Failed to publish request message, err: %v\n", err)
		os.Exit(1)
	}

	// block until it has got the reply
	_ = <-gotReply
}
```

**consumer.go**

```go
package main

import (
	"fmt"
	"github.com/streadway/amqp"
	"time"
)

var server = "hostname"
var port = 5672
var vhost = "yourvhost"
var username = "username"
var password = "password"
var exchangeName = "testEx"
var requestQueue = "requestQ"
var requestKey = "request"

func main() {
	// Infinite loop to auto-reconnect on failure
Loop:
	for {
		fmt.Println("Starting in 5 seconds...")
		time.Sleep(5 * time.Second)

		connection, err := amqp.DialConfig(fmt.Sprintf("amqp://%s:%s@%s:%d/%s", username, password, server, port, vhost),
			amqp.Config{Heartbeat: 60 * time.Second})
		if err != nil {
			fmt.Printf("Failed to connect, err: %v\n", err)
			continue Loop
		}
		defer connection.Close()

		channel, err := connection.Channel()
		if err != nil {
			fmt.Printf("Failed to create channel, err: %v\n", err)
			continue Loop
		}
		defer channel.Close()

		err = channel.ExchangeDeclare(
			exchangeName, // name
			"direct",     // type
			false,        // durable
			true,         // audo-delete
			false,        // internal
			false,        // no-wait
			nil,          // args
		)
		if err != nil {
			fmt.Printf("Failed to declare exchange, err: %v\n", err)
			continue Loop
		}

		queue, err := channel.QueueDeclare(
			requestQueue, // name
			false,        // durable
			true,         // auto-delete
			true,         // exclusive
			false,        // no-wait
			nil,          // args
		)
		if err != nil {
			fmt.Printf("Failed to declare request queue, err: %v\n", err)
			continue Loop
		}

		err = channel.QueueBind(
			requestQueue, // queue
			requestKey,   // key
			exchangeName, // exchange
			false,        // no-wait
			nil,          // args
		)
		if err != nil {
			fmt.Printf("Failed to bind request queue with exchange, err: %v\n", err)
			continue Loop
		}

		messageChan, err := channel.Consume(
			queue.Name,        // queue
			"requestConsumer", // consumer tag
			false,             // auto-ack
			true,              // exclusive
			false,             // no-local
			false,             // no-wait
			nil,               // args
		)
		if err != nil {
			fmt.Printf("Failed to consume request messages, err: %v\n", err)
			continue Loop
		}

		fmt.Println("Started consuming messages.")
		for message := range messageChan {
			fmt.Println(string(message.Body))

			// on receiving request messages, reply to the reply_to header
			err = channel.Publish(
				exchangeName,    // exchange
				message.ReplyTo, // routing key
				false,           // mandatory
				false,           // immediate
				amqp.Publishing{
					ContentType:  "text/plain",
					DeliveryMode: 1,
					Body:         append([]byte("Reply to "), message.Body...),
				})
			if err != nil {
				fmt.Printf("Failed to publish reply message, err: %v\n", err)
				err = message.Nack(
					false, // multiple
					true,  // requeued
				)
				if err != nil {
					fmt.Printf("Failed to NACK request message, err: %v\n", err)
					break
				}
			} else {
				err = message.Ack(
					false, // multiple
				)
				if err != nil {
					fmt.Printf("Failed to ACK request message, err: %v\n", err)
					break
				}
			}
		}
	}
}
```

## C

### Prerequisites

**C client AMQP library**

robomq.io is built on AMQP, an open, general-purpose protocol for messaging. There are a number of clients for AMQP in many different languages.  However, we'll choose a simple C-language AMQP client library written for use with v2.0+ of the RabbitMQ broker.

<a href="https://github.com/alanxz/rabbitmq-c/tree/master/librabbitmq" target="_blank">https://github.com/alanxz/rabbitmq-c/tree/master/librabbitmq</a>

You can copy librabbitmq subfolder from latest release located here on GitHub:

<a href="https://github.com/alanxz/rabbitmq-c" target="_blank">https://github.com/alanxz/rabbitmq-c</a>

Alternatively, thanks to Subversion support in GitHub, you can use svn export directly:

```bash
svn export https://github.com/alanxz/rabbitmq-c/trunk/librabbitmq
```

Copy the librabbitmq package into your working directory:

```bash
cp librabbitmq ./
```

Also copy all source files and Makefile from <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> SDK at <https://github.com/robomq/robomq.io/tree/master/sdk/AMQP/C> into the same directory.  

Now your working directory should have the content as bellow:  
*broadcast*  config.h  *librabbitmq*  Makefile  *one-to-one*  *request-reply*  *routing-key* *topic*

Use the Makefile to compile under a Linux terminal.  

* Run `make type={sub-directory}` to compile the producer and consumer under the sub-directory.  
* Before compiling the next sub-directory, run `make clean` to clean up the compiled files.	 

Note that these examples provide a simple client implementation to get started but does not go into detailed description of all flags passed into the AMQP methods. 
A complete reference to RabbitMQ's implementaton of version 0-9-1 of the AMQP specification can be found in this guide.
<a href="https://www.rabbitmq.com/amqp-0-9-1-reference.html" target="_blank">https://www.rabbitmq.com/amqp-0-9-1-reference.html</a>


### Producer
For request-reply messaging pattern, the producer also uses **direct** exchange, however, a the reply queue will be created and bound to an exchange allowing more than one consumer to subscribe to and send replies asynchronously. 
Therefore, after publishing a message, the producer will simply wait on a separate queue bound to with with key **"reply_key"** for replies sent by receiving consumer.

```c
char queue_name[] = "reply-queue";
char binding_key[] = "reply_key";

// Declaring exchange
amqp_exchange_declare(conn, channel, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(exchange_type),
		passive, durable, auto_delete, internal, amqp_empty_table);

// Declaring queue
amqp_queue_declare_ok_t *r = amqp_queue_declare(conn, channel, amqp_cstring_bytes(queue_name),
		passive, durable, exclusive, auto_delete, amqp_empty_table);

reply_queue = amqp_bytes_malloc_dup(r->queue);

// Binding to queue
amqp_queue_bind(conn, channel, reply_queue, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(binding_key),
		amqp_empty_table);


// Now wait for the reply message
amqp_basic_consume(conn, channel, reply_queue, amqp_empty_bytes, no_local, no_ack, exclusive, amqp_empty_table);

while (1) {
	amqp_rpc_reply_t result;
	amqp_envelope_t envelope;

	amqp_maybe_release_buffers(conn);
	result = amqp_consume_message(conn, &envelope, NULL, 0);

	if (AMQP_RESPONSE_NORMAL == result.reply_type) {

		printf("Received reply message size: %d\nbody: %s\n", (int)envelope.message.body.len, (char *)envelope.message.body.bytes);

		amqp_destroy_envelope(&envelope);
	}
}
```

### Consumer
This consumer after successfully receiving message from producer will simply send a reply with routing key **"reply_key"** indicating that exchange will deliver reply directly to the reply queue subscribed to by the producer.

```c
char routing_key[] = "reply_key";
result = amqp_consume_message(conn, &envelope, NULL, 0);

if (AMQP_RESPONSE_NORMAL == result.reply_type) {

	// Now sending reply
	amqp_basic_publish(conn,
			channel,
			amqp_cstring_bytes(exchange_name),
			amqp_cstring_bytes(routing_key),
			0,
			0,
			&props,
			amqp_cstring_bytes("Hello back at you"));

	amqp_destroy_envelope(&envelope);
}
```

At this point, consumer should start consuming messages.

### Putting it all together
The full code below includes some basic AMQP error handling for consumer that is useful when declaring exchanges and queues.  In addition, main receiver loop attempts to reconnect upon network connection failure.

**producer.c**

```c
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
	
#include <amqp_tcp_socket.h>
#include <amqp.h>
#include <amqp_framing.h>
	
amqp_connection_state_t mqconnect() {
	
    amqp_connection_state_t conn = amqp_new_connection();
    amqp_socket_t *socket = NULL;
	char hostname[] = "localhost"; // robomq.io hostname
	int port = 5672; //default
	char user[] = "guest"; // robomq.io username
	char password[] = "guest"; // robomq.io password
	char vhost[] = "/"; // robomq.io account vhost
    amqp_channel_t channel = 1;
    int channel_max = 0;
    int frame_max = 131072;
    int heartbeat = 60;
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
    amqp_boolean_t internal = 0;
    char exchange_name[] = "hello-exchange";
    char exchange_type[] = "direct";
    char queue_name[] = "reply-queue";
    char binding_key[] = "reply_key";
    amqp_rpc_reply_t reply;
	
    // Declaring exchange
    amqp_exchange_declare(conn, channel, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(exchange_type),
            passive, durable, auto_delete, internal, amqp_empty_table);
	
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
    amqp_bytes_t reply_queue;
    amqp_channel_t channel = 1;
    amqp_basic_properties_t props;
    props._flags = AMQP_BASIC_CONTENT_TYPE_FLAG | AMQP_BASIC_DELIVERY_MODE_FLAG;
    props.content_type = amqp_cstring_bytes("text/plain");
    props.delivery_mode = 1; /* non-persistent delivery mode */
    amqp_boolean_t mandatory = 0;
    amqp_boolean_t immediate = 0;
    amqp_boolean_t no_local = 0;
    amqp_boolean_t no_ack = 1;
    amqp_boolean_t exclusive = 0;
    char exchange_name[] = "hello-exchange";
    char routing_key[] = "request_key";
    char *msg_body = "Hello\n";
    int result;
	
    conn = mqconnect();
    reply_queue = mqdeclare(conn);
	
    // Sending message
    result = amqp_basic_publish(conn,
            channel,
            amqp_cstring_bytes(exchange_name),
            amqp_cstring_bytes(routing_key),
            mandatory,
            immediate,
            &props,
            amqp_cstring_bytes(msg_body));
	
    // Now wait for the reply message
    amqp_basic_consume(conn, channel, reply_queue, amqp_empty_bytes, no_local, no_ack, exclusive, amqp_empty_table);
	
    while (1) {
        amqp_rpc_reply_t result;
        amqp_envelope_t envelope;
	
        amqp_maybe_release_buffers(conn);
        result = amqp_consume_message(conn, &envelope, NULL, 0);
	
        if (AMQP_RESPONSE_NORMAL == result.reply_type) {
	
            printf("Received reply message size: %d\nbody: %s\n", (int)envelope.message.body.len, (char *)envelope.message.body.bytes);
	
            amqp_destroy_envelope(&envelope);
        }
    }
	
    // Closing connection
    amqp_connection_close(conn, AMQP_REPLY_SUCCESS);
    amqp_destroy_connection(conn);
	
    return 0;
}
```

**consumer.c**

```c
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
	
#include <amqp_tcp_socket.h>
#include <amqp.h>
#include <amqp_framing.h>
	
amqp_connection_state_t mqconnect() {
	
	amqp_connection_state_t conn = amqp_new_connection();
	amqp_socket_t *socket = NULL;
	char hostname[] = "localhost"; // robomq.io hostname
	int port = 5672; //default
	char user[] = "guest"; // robomq.io username
	char password[] = "guest"; // robomq.io password
	char vhost[] = "/"; // robomq.io account vhost
	amqp_channel_t channel = 1;
	amqp_rpc_reply_t reply;
	int channel_max = 0;
	int frame_max = 131072;
	int heartbeat = 60;
	int status = 0;
	
	// Opening socket
	socket = amqp_tcp_socket_new(conn);
	
	status = amqp_socket_open(socket, hostname, port);
	if (status) {
		printf("Error opening TCP socket, status = %d\n", status);
	}
	
	reply = amqp_login(conn, vhost, channel_max, frame_max, heartbeat, AMQP_SASL_METHOD_PLAIN, user, password);
	if(reply.reply_type != AMQP_RESPONSE_NORMAL) {
		fprintf(stderr, "%s: server connection reply code: %d\n",
				"Error logging in", reply.reply_type);
	}
	
	amqp_channel_open(conn, channel);
	
	return conn;
}
	
amqp_bytes_t mqdeclare(amqp_connection_state_t conn, const char *exchange_name, const char *queue_name) {
	amqp_bytes_t queue;
	amqp_channel_t channel = 1;
	amqp_boolean_t passive = 0;
	amqp_boolean_t durable = 0;
	amqp_boolean_t exclusive = 0;
	amqp_boolean_t auto_delete = 1;
	amqp_boolean_t internal = 0;
	char exchange_type[] = "direct";
	char binding_key[] = "request_key";
	amqp_rpc_reply_t reply;
	
	// Declaring exchange
	amqp_exchange_declare(conn, channel, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(exchange_type),
			passive, durable, auto_delete, internal, amqp_empty_table);
	
	reply = amqp_get_rpc_reply(conn);
	if(reply.reply_type != AMQP_RESPONSE_NORMAL) {
		amqp_connection_close_t *m = (amqp_connection_close_t *) reply.reply.decoded;
		if(NULL != m) {
			fprintf(stderr, "%s: server connection error %d, message: %.*s\n",
					"Error declaring exchange",
					m->reply_code,
					(int) m->reply_text.len, (char *) m->reply_text.bytes);
		}
	}
	
	// Declaring queue
	amqp_queue_declare_ok_t *r = amqp_queue_declare(conn, channel, amqp_cstring_bytes(queue_name),
			passive, durable, exclusive, auto_delete, amqp_empty_table);
	
	reply = amqp_get_rpc_reply(conn);
	if(reply.reply_type != AMQP_RESPONSE_NORMAL) {
		fprintf(stderr, "%s: server connection reply code: %d\n",
				"Error declaring queue", reply.reply_type);
	}
	else {
		queue = amqp_bytes_malloc_dup(r->queue);
	
		// Binding to queue
		amqp_queue_bind(conn, channel, queue, amqp_cstring_bytes(exchange_name), amqp_cstring_bytes(binding_key),
				amqp_empty_table);
	}
	
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
	char exchange_name[] = "hello-exchange";
	char routing_key[] = "reply_key";
	amqp_basic_properties_t props;
	props._flags = AMQP_BASIC_CONTENT_TYPE_FLAG | AMQP_BASIC_DELIVERY_MODE_FLAG;
	props.content_type = amqp_cstring_bytes("text/plain");
	props.delivery_mode = 1; /* non-persistent delivery mode */
	char queue_name[] = "hello-queue";
	int retry_time = 5; // retry time in seconds
	
	conn = mqconnect();
	queue = mqdeclare(conn, &exchange_name[0], &queue_name[0]);
	
	// Consuming the message
	amqp_basic_consume(conn, channel, queue, amqp_empty_bytes, no_local, no_ack, exclusive, amqp_empty_table);
	
	while (1) {
		amqp_rpc_reply_t result;
		amqp_envelope_t envelope;
	
		amqp_maybe_release_buffers(conn);
		result = amqp_consume_message(conn, &envelope, NULL, 0);
	
		if (AMQP_RESPONSE_NORMAL != result.reply_type) {
			printf("Consumer AMQP failure occurred, response code = %d, retrying in %d seconds...\n",
					result.reply_type, retry_time);
	
			// Closing current connection before reconnecting
			amqp_connection_close(conn, AMQP_CONNECTION_FORCED);
			amqp_destroy_connection(conn);
	
			// Reconnecting on exception
			conn = mqconnect();
			queue = mqdeclare(conn, &exchange_name[0], &queue_name[0]);
			amqp_basic_consume(conn, channel, queue, amqp_empty_bytes, no_local, no_ack, exclusive, amqp_empty_table);
			sleep(retry_time);
		}
		else {
			printf("Received message size: %d\nbody: %s\n", (int)envelope.message.body.len, (char *)envelope.message.body.bytes);
	
			// Now sending reply
			amqp_basic_publish(conn,
					channel,
					amqp_cstring_bytes(exchange_name),
					amqp_cstring_bytes(routing_key),
					0,
					0,
					&props,
					amqp_cstring_bytes("Hello back at you"));
	
			amqp_destroy_envelope(&envelope);
		}
	}
	
	return 0;
}
```

