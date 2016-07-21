# Introduction

> Before reading this chapter, we assume that you already know AMQP protocol. Knowing MQTT and STOMP would be great too. If not, please go through at least the *Key based message routing* section in User Guide.

<a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> has obtained certificate from a certificate authority (CA) and supports SSL (secure socket layer) connection for all available protocols, including AMQP, MQTT, STOMP and WebSTOMP. The SSL ports of those four protocols are respectively AMQP: 5671, MQTT: 8883, STOMP: 61614, WebSTOMP: 15673.  

This chapter intends to introduce you the method to establish SSL connection between <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker and your client program, except for WebSTOMP.  

The certificate of our root CA can be downloaded at <a href="http://www.tbs-x509.com/AddTrustExternalCARoot.crt" target="_blank">http://www.tbs-x509.com/AddTrustExternalCARoot.crt</a>. It is needed to verify the leaf certificate of <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> because the latter one was granted by the root CA through a chain of trust.  

In most cases, your device or application trying to connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker are not like Web browsers. Browsers come with all CAs' certificates so  they're inherently able to verify the <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> certificate. That is why WebSTOMP client running inside a browser doesn't need any extra work to connect over SSL. In contrast, your device or application typically don't have the CA certificate to verify <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> certificate.  

Therefore, if you choose to or have to verify the leaf certificate of <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> in your client program, you will be required to download the root CA certificate at <a href="https://www.tbs-x509.com/AddTrustExternalCARoot.crt" target="_blank">https://www.tbs-x509.com/AddTrustExternalCARoot.crt</a> and import it in your program to achieve the verification. Otherwise, if you optionally ignore the certificate verification, it's not a necessity.  

An unfortunate fact is that not all message queue client libraries support SSL connection. Actually, only a small portion of them do. Hence, pick a capable library before you develop your SSL clients.  

# SSL use cases

We will provide examples of AMQP SSL clients of key based message routing scenario in Python. They are variants of the Python example in *Key based message routing* section. The only difference is that they connect over SSL, so we're going to focus on the connecting part of the code.  

The first example verifies <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> certificate, while the second one doesn't. You may choose to follow any of them according to your specific demands.  

Please refer to the *Key based message routing* section in User Guide for library dependency, program logic, code comments and everything irrelevant with connecting itself.  

The example code provided bellow could be the short version, it might have omitted some advanced details. For full version code, please go to our SDK <a href="https://github.com/robomq/robomq.io/tree/master/sdk/SSL" target="_blank">repository</a> on GitHub. 

> Before testing the example code, replace hostname, yourvhost, username and password with the real variables in your network environment.  
> Always run consumer first to create the exchange and queue for producer to send messages to.   

## Certificate verified

### Connect
Compared to non-SSL connect method recapped bellow,  

```python
credentials = pika.PlainCredentials(username, password)
connection = pika.BlockingConnection(pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, heartbeat_interval = 60))
```

SSL connect method adds two parameters. It sets `ssl = True` and passes SSL options. The `"cert_reqs": ssl.CERT_REQUIRED` in SSL options implies the client requires to verify server's certificate.  

```python
credentials = pika.PlainCredentials(username, password)
sslOptions = {"cert_reqs": ssl.CERT_REQUIRED, "ca_certs": caCert}
parameters = pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, heartbeat_interval = 60, ssl = True, ssl_options = sslOptions)
connection = pika.BlockingConnection(parameters)
```

If the root CA certificate file isn't provided or isn't the one downloaded at <http://www.tbs-x509.com/AddTrustExternalCARoot.crt>, client will fail to verify <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> certificate thus fail to connect.  

### Putting it together

**producer.py**

```python
import pika
import ssl
	
server = "hostname"
port = 5671
vhost = "yourvhost" 
username = "username"
password = "password"
caCert = "./AddTrustExternalCARoot.crt" #change it to the actual path to CA certificate
exchangeName = "testEx"
routingKey = "test"
	
try:
	#connect
	credentials = pika.PlainCredentials(username, password)
	sslOptions = {"cert_reqs": ssl.CERT_REQUIRED, "ca_certs": caCert}
	parameters = pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, heartbeat_interval = 60, ssl = True, ssl_options = sslOptions)
	connection = pika.BlockingConnection(parameters)
	channel = connection.channel()
	
	#send message
	properties = pika.spec.BasicProperties(content_type = "text/plain", delivery_mode = 1)
	channel.basic_publish(exchange = exchangeName, routing_key = routingKey, body = "Hello World!", properties = properties)
	
	#disconnect
	connection.close()
except Exception, e:
	print e
```

**consumer.py**

```python
import pika
import ssl
import time
	
server = "hostname"
port = 5671
vhost = "yourvhost" 
username = "username"
password = "password"
caCert = "./AddTrustExternalCARoot.crt" #change it to the actual path to CA certificate
exchangeName = "testEx"
queueName = "testQ1"
routingKey = "test"
	
#callback funtion on receiving messages
def onMessage(channel, method, properties, body):
	print body
	
while True:
	try:
		#connect
		credentials = pika.PlainCredentials(username, password)
		sslOptions = {"cert_reqs": ssl.CERT_REQUIRED, "ca_certs": caCert}
		parameters = pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, heartbeat_interval = 60, ssl = True, ssl_options = sslOptions)
		connection = pika.BlockingConnection(parameters)
		channel = connection.channel()
	
		#declare exchange and queue, bind them and consume messages
		channel.exchange_declare(exchange = exchangeName, exchange_type = "direct", auto_delete = True)
		channel.queue_declare(queue = queueName, exclusive = True, auto_delete = True)
		channel.queue_bind(exchange = exchangeName, queue = queueName, routing_key = routingKey)
		channel.basic_consume(consumer_callback = onMessage, queue = queueName, no_ack = True)
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

## Certificate not verified

### Connect
Compared to certificate-verified connect method above, certificate-not-verified connect method changes `"cert_reqs": ssl.CERT_REQUIRED` to `"cert_reqs": ssl.CERT_NONE` in SSL options. That implies the client doesn't require to verify server's certificate.  

```python
credentials = pika.PlainCredentials(username, password)
sslOptions = {"cert_reqs": ssl.CERT_NONE}
parameters = pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, heartbeat_interval = 60, ssl = True, ssl_options = sslOptions)
connection = pika.BlockingConnection(parameters)
```

Even if the root CA certificate is provided, it will be ignored.  

You can safely use this method to connect to <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> broker over SSL without verification because <a href="https://www.robomq.io" target="_blank">RoboMQ.io</a> is a trustworthy service provider. However, this way is generally not recommended for unknown services.  

### Putting it together

**producer.py**

```python
import pika
import ssl
	
server = "hostname"
port = 5671
vhost = "yourvhost" 
username = "username"
password = "password"
exchangeName = "testEx"
routingKey = "test"
	
try:
	#connect
	credentials = pika.PlainCredentials(username, password)
	sslOptions = {"cert_reqs": ssl.CERT_NONE}
	parameters = pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, heartbeat_interval = 60, ssl = True, ssl_options = sslOptions)
	connection = pika.BlockingConnection(parameters)
	channel = connection.channel()
	
	#send message
	properties = pika.spec.BasicProperties(content_type = "text/plain", delivery_mode = 1)
	channel.basic_publish(exchange = exchangeName, routing_key = routingKey, body = "Hello World!", properties = properties)
	
	#disconnect
	connection.close()
except Exception, e:
	print e
```

**consumer.py**

```python
import pika
import ssl
import time
	
server = "hostname"
port = 5671
vhost = "yourvhost" 
username = "username"
password = "password"
exchangeName = "testEx"
queueName = "testQ1"
routingKey = "test"
	
#callback funtion on receiving messages
def onMessage(channel, method, properties, body):
	print body
	
while True:
	try:
		#connect
		credentials = pika.PlainCredentials(username, password)
		sslOptions = {"cert_reqs": ssl.CERT_NONE}
		parameters = pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, heartbeat_interval = 60, ssl = True, ssl_options = sslOptions)
		connection = pika.BlockingConnection(parameters)
		channel = connection.channel()
	
		#declare exchange and queue, bind them and consume messages
		channel.exchange_declare(exchange = exchangeName, exchange_type = "direct", auto_delete = True)
		channel.queue_declare(queue = queueName, exclusive = True, auto_delete = True)
		channel.queue_bind(exchange = exchangeName, queue = queueName, routing_key = routingKey)
		channel.basic_consume(consumer_callback = onMessage, queue = queueName, no_ack = True)
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


