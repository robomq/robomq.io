# File: producer.py
# Description: This is the AMQP SSL producer publishes outgoing AMQP
#     communication to  clients consuming messages from a broker server.
#     This example applies routing-key pattern out of 5 patterns.
#     It doesn't verify the certificate of robomq.io.
#
# Author: Eamin Zhang
# robomq.io (http://www.robomq.io)

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
	parameters = pika.ConnectionParameters(host = server, port = port, virtual_host = vhost, credentials = credentials, ssl = True, ssl_options = sslOptions)
	connection = pika.BlockingConnection(parameters)
	channel = connection.channel()

	#send message
	properties = pika.spec.BasicProperties(content_type = "text/plain", delivery_mode = 1)
	channel.basic_publish(exchange = exchangeName, routing_key = routingKey, body = "Hello World!", properties = properties)

	#disconnect
	connection.close()
except Exception, e:
	print e